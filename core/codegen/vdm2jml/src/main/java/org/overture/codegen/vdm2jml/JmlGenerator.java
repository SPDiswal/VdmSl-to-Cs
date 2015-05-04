package org.overture.codegen.vdm2jml;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.SFunctionDefinition;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.ClonableString;
import org.overture.codegen.analysis.violations.UnsupportedModelingException;
import org.overture.codegen.cgast.INode;
import org.overture.codegen.cgast.PCG;
import org.overture.codegen.cgast.SDeclCG;
import org.overture.codegen.cgast.SPatternCG;
import org.overture.codegen.cgast.declarations.AClassDeclCG;
import org.overture.codegen.cgast.declarations.AFieldDeclCG;
import org.overture.codegen.cgast.declarations.AFormalParamLocalParamCG;
import org.overture.codegen.cgast.declarations.AMethodDeclCG;
import org.overture.codegen.cgast.declarations.AModuleDeclCG;
import org.overture.codegen.cgast.declarations.ARecordDeclCG;
import org.overture.codegen.cgast.declarations.AStateDeclCG;
import org.overture.codegen.cgast.declarations.ATypeDeclCG;
import org.overture.codegen.cgast.name.ATypeNameCG;
import org.overture.codegen.cgast.patterns.AIdentifierPatternCG;
import org.overture.codegen.cgast.types.ARecordTypeCG;
import org.overture.codegen.ir.IRConstants;
import org.overture.codegen.ir.IREventObserver;
import org.overture.codegen.ir.IRInfo;
import org.overture.codegen.ir.IRSettings;
import org.overture.codegen.ir.IRStatus;
import org.overture.codegen.ir.VdmNodeInfo;
import org.overture.codegen.logging.Logger;
import org.overture.codegen.utils.GeneratedData;
import org.overture.codegen.vdm2java.JavaCodeGen;
import org.overture.codegen.vdm2java.JavaCodeGenUtil;
import org.overture.codegen.vdm2java.JavaFormat;
import org.overture.codegen.vdm2java.JavaSettings;

public class JmlGenerator implements IREventObserver
{
	private static final String JML_STATIC_INV_ANNOTATION = "static invariant";
	private static final String JML_REQ_ANNOTATION = "requires";
	private static final String JML_ENS_ANNOTATION = "ensures";

	private static final String JML_INV_PREFIX = "inv_";
	private static final String JML_OLD_PREFIX = "\\old";

	private static final String JML_SPEC_PUBLIC = "/*@ spec_public @*/";
	private static final String JML_PURE = "/*@ pure @*/";

	private static final String JML_RESULT = "\\result";

	private JavaCodeGen javaGen;

	public JmlGenerator()
	{
		this.javaGen = new JavaCodeGen();
	}

	public JavaCodeGen getJavaGen()
	{
		return javaGen;
	}

	public GeneratedData generateJml(List<AModuleModules> ast)
			throws AnalysisException, UnsupportedModelingException
	{
		javaGen.register(this);

		return javaGen.generateJavaFromVdmModules(ast);
	}

	private Map<String, List<ClonableString>> classInvInfo = new HashedMap<String, List<ClonableString>>();

	@Override
	public List<IRStatus<INode>> initialIRConstructed(
			List<IRStatus<INode>> ast, IRInfo info)
	{
		// In the initial version of the IR the top level containers are both modules and classes
		for (IRStatus<AClassDeclCG> status : IRStatus.extract(ast, AClassDeclCG.class))
		{
			computeClassInvInfo(status.getIrNode());
		}

		for (IRStatus<AModuleDeclCG> status : IRStatus.extract(ast, AModuleDeclCG.class))
		{
			computeModuleInvInfo(status.getIrNode());
		}

		return ast;
	}

	@Override
	public List<IRStatus<INode>> finalIRConstructed(List<IRStatus<INode>> ast,
			IRInfo info)
	{
		// In the final version of the IR, received by the Java code generator, all
		// top level containers are classes

		// Functions are JML pure so we will annotate them as so.
		// Note that @pure is a JML modifier so this annotation should go last
		// to prevent the error: "no modifiers are allowed prior to a lightweight
		// specification case"
		
		// All the record methods are JML pure
		makeRecMethodsPure(ast);
		
		List<IRStatus<INode>> newAst = new LinkedList<IRStatus<INode>>(ast);

		// To circumvent a problem with OpenJML. See documentation of makeRecsOuterClasses
		newAst.addAll(makeRecsOuterClasses(ast));
		
		for (IRStatus<AClassDeclCG> status : IRStatus.extract(ast, AClassDeclCG.class))
		{
			AClassDeclCG clazz = status.getIrNode();
			
			if(clazz.getInvariant() != null)
			{
				// We need to make the class invariant function public so it can be
				// invoked in the invariant annotation
				makeCondPublic(clazz.getInvariant());
				makePure(clazz.getInvariant());
			}

			for (AFieldDeclCG f : clazz.getFields())
			{
				// Make fields JML @spec_public so they can be passed to post conditions
				appendMetaData(f, consMetaData(JML_SPEC_PUBLIC));
			}

			List<ClonableString> inv = classInvInfo.get(status.getIrNodeName());

			if (inv != null)
			{
				clazz.setMetaData(inv);
			}

			// Note that the methods contained in clazz.getMethod() include
			// pre post and invariant methods
			for (AMethodDeclCG m : clazz.getMethods())
			{
				if (m.getPreCond() != null)
				{
					// We need to make pre and post conditions functions public in order to
					// be able to call them in the @requires and @ensures clauses, respectively.
					makeCondPublic(m.getPreCond());
					appendMetaData(m, consMethodCond(m.getPreCond(), m.getFormalParams(), JML_REQ_ANNOTATION));
				}

				if (m.getPostCond() != null)
				{
					makeCondPublic(m.getPostCond());
					appendMetaData(m, consMethodCond(m.getPostCond(), m.getFormalParams(), JML_ENS_ANNOTATION));
				}

				// Some methods such as those in record classes
				// will not have a source node. Thus this check.
				if (m.getSourceNode() != null)
				{
					if (m.getSourceNode().getVdmNode() instanceof SFunctionDefinition)
					{
						makePure(m);
					}
				}
			}
		}

		// Return back the modified AST to the Java code generator
		return newAst;
	}

	private void makeRecMethodsPure(List<IRStatus<INode>> ast)
	{
		List<ARecordDeclCG> records = getRecords(ast);

		for (ARecordDeclCG rec : records)
		{
			for (AMethodDeclCG method : rec.getMethods())
			{
				if (!method.getIsConstructor())
				{
					makePure(method);
				}
			}
		}
	}
	
	private List<ARecordDeclCG> getRecords(List<IRStatus<INode>> ast)
	{
		List<ARecordDeclCG> records = new LinkedList<ARecordDeclCG>();

		for (IRStatus<AClassDeclCG> classStatus : IRStatus.extract(ast, AClassDeclCG.class))
		{
			for (ATypeDeclCG typeDecl : classStatus.getIrNode().getTypeDecls())
			{
				if (typeDecl.getDecl() instanceof ARecordDeclCG)
				{
					records.add((ARecordDeclCG) typeDecl.getDecl());
				}
			}
		}

		return records;
	}

	private void makePure(SDeclCG cond)
	{
		if (cond != null)
		{
			appendMetaData(cond, consMetaData(JML_PURE));
		}
	}

	private void makeCondPublic(SDeclCG cond)
	{
		if (cond instanceof AMethodDeclCG)
		{
			((AMethodDeclCG) cond).setAccess(IRConstants.PUBLIC);
		} else
		{
			Logger.getLog().printErrorln("Expected method declaration but got "
					+ cond + " in makePCondPublic");
		}
	}

	/**
	 * This change to the IR is really only to circumvent a bug in OpenJML with loading of inner classes. The only thing
	 * that the Java code generator uses inner classes for is records. If this problem gets fixed the workaround
	 * introduced by this method should be removed.
	 * 
	 * @param ast
	 *            The IR passed by the Java code generator after the transformation process
	 */
	private List<IRStatus<INode>> makeRecsOuterClasses(List<IRStatus<INode>> ast)
	{
		List<IRStatus<INode>> extraClasses = new LinkedList<IRStatus<INode>>();

		for (IRStatus<AClassDeclCG> status : IRStatus.extract(ast, AClassDeclCG.class))
		{
			AClassDeclCG clazz = status.getIrNode();

			List<ARecordDeclCG> recDecls = new LinkedList<ARecordDeclCG>();

			for (ATypeDeclCG d : clazz.getTypeDecls())
			{
				if (d.getDecl() instanceof ARecordDeclCG)
				{
					recDecls.add((ARecordDeclCG) d.getDecl());
				}
			}

			// Note that we do not remove the type declarations (or records) from the class.

			// For each of the records we will make a top-level class
			for (ARecordDeclCG recDecl : recDecls)
			{
				AClassDeclCG recClass = new AClassDeclCG();

				recClass.setAbstract(false);
				recClass.setAccess(IRConstants.PUBLIC);
				recClass.setSourceNode(recDecl.getSourceNode());
				recClass.setStatic(false);
				recClass.setName(recDecl.getName());

				// Copy the record methods to the class
				List<AMethodDeclCG> methods = new LinkedList<AMethodDeclCG>();
				for (AMethodDeclCG m : recDecl.getMethods())
				{
					methods.add(m.clone());
				}
				recClass.setMethods(methods);

				// Copy the record fields to the class
				List<AFieldDeclCG> fields = new LinkedList<AFieldDeclCG>();
				for (AFieldDeclCG f : recDecl.getFields())
				{
					fields.add(f.clone());
				}
				recClass.setFields(fields);

				// Put the record classes of module M in package <userpackage>.<modulename>types
				// Examples: my.pack.Mtypes
				if (JavaCodeGenUtil.isValidJavaPackage(getJavaSettings().getJavaRootPackage()))
				{
					String recPackage = getJavaSettings().getJavaRootPackage()
							+ "." + clazz.getName()
							+ JavaFormat.TYPE_DECL_PACKAGE_SUFFIX;
					recClass.setPackage(recPackage);
				} else
				{
					recClass.setPackage(clazz.getName() + JavaFormat.TYPE_DECL_PACKAGE_SUFFIX);
				}

				extraClasses.add(new IRStatus<INode>(recClass.getName(), recClass, new HashSet<VdmNodeInfo>()));
			}
		}

		return extraClasses;
	}

	private List<ClonableString> consMethodCond(SDeclCG decl,
			List<AFormalParamLocalParamCG> parentMethodParams, String jmlAnno)
	{
		if (decl instanceof AMethodDeclCG)
		{
			AMethodDeclCG cond = (AMethodDeclCG) decl;

			List<String> fieldNames = new LinkedList<String>();

			// The arguments of a function or an operation are passed to the pre/post condition
			int i;
			for (i = 0; i < parentMethodParams.size(); i++)
			{
				fieldNames.add(getMethodCondArgName(parentMethodParams.get(i).getPattern()));
			}

			// Now compute the remaining argument names which (possibly)
			// include the RESULT and the old state passed
			for (; i < cond.getFormalParams().size(); i++)
			{
				fieldNames.add(getMethodCondArgName(cond.getFormalParams().get(i).getPattern()));
			}

			return consAnno(jmlAnno, cond.getName(), fieldNames);

		} else if (decl != null)
		{
			Logger.getLog().printErrorln("Expected pre/post condition to be a method declaration at this point. Got: "
					+ decl + " in '" + this.getClass().getSimpleName() + "'");
		}

		return null;
	}

	private String getMethodCondArgName(SPatternCG pattern)
	{
		// By now all patterns should be identifier patterns
		if (pattern instanceof AIdentifierPatternCG)
		{
			String paramName = ((AIdentifierPatternCG) pattern).getName();

			if (this.javaGen.getInfo().getExpAssistant().isOld(paramName))
			{
				paramName = toJmlOldExp(paramName);
			} else if (this.javaGen.getInfo().getExpAssistant().isResult(paramName))
			{
				// TODO: This builds on the assumption that RESULT can't really
				// be used as an identifier name. There is an issue open for that
				// Link: https://github.com/overturetool/overture/issues/443
				paramName = JML_RESULT;
			}

			return paramName;

		} else
		{
			Logger.getLog().printErrorln("Expected formal parameter pattern to be an indentifier pattern. Got: "
					+ pattern + " in '" + this.getClass().getSimpleName() + "'");

			return "UNKNOWN";
		}
	}

	private String toJmlOldExp(String paramName)
	{
		// Convert old name to current name (e.g. _St to St)
		String currentArg = this.javaGen.getInfo().getExpAssistant().oldNameToCurrentName(paramName);

		// Note that invoking the copy method on the state should be okay
		// because the state should never be a null pointer
		currentArg += ".copy()";

		// Convert current name to JML old expression (e.g. \old(St)
		return String.format("%s(%s)", JML_OLD_PREFIX, currentArg);
	}

	private void computeModuleInvInfo(AModuleDeclCG module)
	{
		for (SDeclCG decl : module.getDecls())
		{
			if (decl instanceof AStateDeclCG)
			{
				AStateDeclCG state = (AStateDeclCG) decl;
				if (state.getInvDecl() != null)
				{
					List<String> fieldNames = new LinkedList<String>();
					fieldNames.add(state.getName());

					// The static invariant requires that either the state of the module is uninitialized
					// or inv_St(St) holds.
					//
					//@ static invariant St == null || inv_St(St);
					classInvInfo.put(module.getName(), consAnno(JML_STATIC_INV_ANNOTATION,
							String.format("%s == null", state.getName())  + " || " +
							JML_INV_PREFIX + state.getName(), fieldNames));
					// Without the St == null check the initialization of the state field, i.e.
					// St = new my.pack.Mtypes.St(<args>) would try to check that inv_St(St) holds
					// before the state of the module is initialized but that will cause a null
					// pointer exception.
					//
					// If OpenJML did allow the state record to be an inner class (which it does not
					// due to a bug) then the constructor of the state class could have been made private,
					// inv_St(St) would not be checked during initialization of
					// the module. If that was possible, then the null check could have been omitted.
				}
			}
		}
	}

	/**
	 * Computes class invariant information for a class under the assumption that that the dialect is VDMPP
	 * 
	 * @param clazz
	 *            The class to compute class invariant information for
	 */
	private void computeClassInvInfo(AClassDeclCG clazz)
	{
		if (clazz.getInvariant() != null)
		{
			List<String> fieldNames = new LinkedList<String>();
			for (AFieldDeclCG field : clazz.getFields())
			{
				if (!field.getFinal() && !field.getVolatile())
				{
					fieldNames.add(field.getName());
				}
			}

			classInvInfo.put(clazz.getName(), consAnno(JML_STATIC_INV_ANNOTATION, JML_INV_PREFIX
					+ clazz.getName(), fieldNames));
		}
	}

	private List<ClonableString> consAnno(String jmlAnno, String name,
			List<String> fieldNames)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("//@ %s %s", jmlAnno, name));
		sb.append("(");

		String sep = "";
		for (String fName : fieldNames)
		{
			sb.append(sep).append(fName);
			sep = ",";
		}

		sb.append(");");

		return consMetaData(sb);
	}

	private List<ClonableString> consMetaData(StringBuilder sb)
	{
		return consMetaData(sb.toString());
	}

	private List<ClonableString> consMetaData(String str)
	{
		List<ClonableString> inv = new LinkedList<ClonableString>();

		inv.add(new ClonableString(str));

		return inv;
	}

//	private void prependMetaData(PCG node, List<ClonableString> extraMetaData)
//	{
//		addMetaData(node, extraMetaData, true);
//	}
	
	private void appendMetaData(PCG node, List<ClonableString> extraMetaData)
	{
		addMetaData(node, extraMetaData, false);
	}
	
	private void addMetaData(PCG node, List<ClonableString> extraMetaData, boolean prepend)
	{
		if (extraMetaData == null || extraMetaData.isEmpty())
		{
			return;
		}

		List<ClonableString> allMetaData = new LinkedList<ClonableString>();

		if(prepend)
		{
			allMetaData.addAll(extraMetaData);
			allMetaData.addAll(node.getMetaData());
		}
		else
		{
			allMetaData.addAll(node.getMetaData());
			allMetaData.addAll(extraMetaData);
		}

		node.setMetaData(allMetaData);
	}

	public IRSettings getIrSettings()
	{
		return javaGen.getSettings();
	}

	public void setIrSettings(IRSettings irSettings)
	{
		javaGen.setSettings(irSettings);
	}

	public JavaSettings getJavaSettings()
	{
		return javaGen.getJavaSettings();
	}

	public void setJavaSettings(JavaSettings javaSettings)
	{
		javaGen.setJavaSettings(javaSettings);
	}
}

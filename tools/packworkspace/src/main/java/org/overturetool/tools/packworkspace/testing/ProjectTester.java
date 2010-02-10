package org.overturetool.tools.packworkspace.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.overturetool.tools.packworkspace.ProjectPacker;
import org.overturetool.vdmj.ExitStatus;
import org.overturetool.vdmj.Settings;
import org.overturetool.vdmj.VDMJ;
import org.overturetool.vdmj.VDMPP;
import org.overturetool.vdmj.VDMRT;
import org.overturetool.vdmj.VDMSL;
import org.overturetool.vdmj.messages.Console;
import org.overturetool.vdmj.messages.StderrRedirector;
import org.overturetool.vdmj.messages.StdoutRedirector;
import org.overturetool.vdmj.runtime.Interpreter;
import org.overturetool.vdmj.values.Value;

public class ProjectTester
{
	VDMJ controller;
	File reportLocation;

	ExitStatus statusParse = null;
	ExitStatus statusTypeCheck = null;
	ExitStatus statusInterpreter = null;
	boolean isFaild = false;

	enum Phase {
		SyntaxCheck, TypeCheck, Interpretation
	}

	public ProjectTester(File reportLocation) {
		this.reportLocation = reportLocation;
	}

	public String test(ProjectPacker project) throws IOException
	{
		switch (project.getDialect())
		{
		case VDM_PP:
			controller = new VDMPP();
			break;
		case VDM_RT:
			controller = new VDMRT();
			break;
		case VDM_SL:
			controller = new VDMSL();
			break;
		}

		Settings.dialect = project.getDialect();
		Settings.dynamictypechecks = project.getSettings()
				.getDynamicTypeChecks();
		Settings.invchecks = project.getSettings().getInvChecks();
		Settings.postchecks = project.getSettings().getPostChecks();
		Settings.prechecks = project.getSettings().getPreChecks();
		Settings.release = project.getSettings().getLanguageVersion();

		StringBuilder sb = new StringBuilder();

		project.getSettings().createReadme(new File(reportLocation,
				project.getSettings().getName() + "/Settings.txt"));
		setConsole(project.getSettings().getName(), Phase.SyntaxCheck);
		statusParse = controller.parse(project.getSpecFiles());
		if (statusParse == ExitStatus.EXIT_OK)
		{
			setConsole(project.getSettings().getName(), Phase.TypeCheck);
			statusTypeCheck = controller.typeCheck();

			if (project.getSettings().getEntryPoint() != null
					&& project.getSettings().getEntryPoint().length() > 0)
			{
				try
				{
					setConsole(project.getSettings().getName(),
							Phase.Interpretation);
					Interpreter i = controller.getInterpreter();
					i.init(null);
					Value value = i.evaluate(project.getSettings()
							.getEntryPoint(), i.initialContext);
					Console.out.println(value);
					statusInterpreter = ExitStatus.EXIT_OK;
				} catch (Exception e)
				{
					statusInterpreter = ExitStatus.EXIT_ERRORS;
					;
				}
			}
		}

		switch (project.getSettings().getExpectedResult())
		{
		case NO_CHECK:
			isFaild = false;
			break;
		case NO_ERROR_SYNTAX:
			isFaild = statusParse != ExitStatus.EXIT_OK;
			break;
		case NO_ERROR_TYPE_CHECK:
			isFaild = statusTypeCheck != ExitStatus.EXIT_OK
					|| statusParse != ExitStatus.EXIT_OK;
			break;
		case NO_ERROR_INTERPRETER:
			isFaild = statusInterpreter != ExitStatus.EXIT_OK
					|| statusTypeCheck != ExitStatus.EXIT_OK
					|| statusParse != ExitStatus.EXIT_OK;
			break;
		}

		if (!isFaild)
			sb.append(HtmlTable.makeCell(HtmlPage.makeLink(project.getSettings()
					.getName(),
					project.getSettings().getName() + "/Settings.txt")));
		else
			sb.append(makeCell(ExitStatus.EXIT_ERRORS,
					HtmlPage.makeLink(project.getSettings().getName(),
							project.getSettings().getName() + "/Settings.txt")));

		if (statusParse != null)
			sb.append(makeCell(statusParse, statusParse.name()
					+ " "
					+ getLinks(project.getSettings().getName(),
							Phase.SyntaxCheck)));
		else
			sb.append(HtmlTable.makeCell(""));

		if (statusTypeCheck != null)
			sb.append(makeCell(statusTypeCheck,
					statusTypeCheck.name()
							+ " "
							+ getLinks(project.getSettings().getName(),
									Phase.TypeCheck)));
		else
			sb.append(HtmlTable.makeCell(""));

		if (statusInterpreter != null)
			sb.append(makeCell(statusInterpreter, statusInterpreter.name()
					+ " "
					+ getLinks(project.getSettings().getName(),
							Phase.Interpretation)));
		else
			sb.append(HtmlTable.makeCell(""));

		return HtmlTable.makeRow(sb.toString());
	}

	private static String makeCell(ExitStatus status, String text)
	{
		switch (status)
		{
		case EXIT_ERRORS:
			return HtmlTable.makeCell(text, HtmlTable.STYLE_CLASS_FAILD);
		case EXIT_OK:
			return HtmlTable.makeCell(text, HtmlTable.STYLE_CLASS_OK);
		case RELOAD:
			return HtmlTable.makeCell(text);

		}
		return text;
	}

	private String getLinks(String projectName, Phase phase)
	{
		File out = new File(reportLocation, projectName + "/" + phase
				+ "Out.txt");
		File err = new File(reportLocation, projectName + "/" + phase
				+ "Err.txt");
		String value = "";
		if (logFileExists(out))
			value += HtmlPage.makeLink("Out", projectName + "/" + phase
					+ "Out.txt");
		if (logFileExists(err))
			value += "/"+HtmlPage.makeLink("Err", projectName + "/" + phase
					+ "Err.txt");

		return value;
	}

	public boolean logFileExists(File file)
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			BufferedReader input = new BufferedReader(new FileReader(file));
			try
			{

				String line = null;
				while ((line = input.readLine()) != null)
				{
					sb.append(line);
				}
			} finally
			{
				input.close();
			}
			if (sb.toString().trim().length() == 0)
			{
				file.delete();
				return false;
			}
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return true;
	}

	private void setConsole(String projectName, Phase phase) throws IOException
	{
		File projectDir = new File(reportLocation, projectName);
		projectDir.mkdirs();
		Console.out = new StdoutRedirector(new FileWriter(new File(projectDir,
				phase + "Out.txt"), false));
		Console.err = new StderrRedirector(new FileWriter(new File(projectDir,
				phase + "Err.txt"), false));
	}

	public boolean isSyntaxCorrect()
	{
		return statusParse == null || statusParse == ExitStatus.EXIT_OK;
	}

	public boolean isTypeCorrect()
	{
		return statusTypeCheck == null || statusTypeCheck == ExitStatus.EXIT_OK;
	}

	public boolean isInterpretationSuccessfull()
	{
		return statusInterpreter == null
				|| statusInterpreter == ExitStatus.EXIT_OK;
	}

	public boolean isFaild()
	{
		return isFaild;
	}
}

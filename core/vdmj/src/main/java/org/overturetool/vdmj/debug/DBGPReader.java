/*******************************************************************************
 *
 *	Copyright (c) 2009 Fujitsu Services Ltd.
 *
 *	Author: Nick Battle
 *
 *	This file is part of VDMJ.
 *
 *	VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package org.overturetool.vdmj.debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.overturetool.vdmj.ExitStatus;
import org.overturetool.vdmj.Settings;
import org.overturetool.vdmj.VDMJ;
import org.overturetool.vdmj.VDMOV;
import org.overturetool.vdmj.VDMPP;
import org.overturetool.vdmj.VDMRT;
import org.overturetool.vdmj.VDMSL;
import org.overturetool.vdmj.definitions.ClassDefinition;
import org.overturetool.vdmj.definitions.ClassList;
import org.overturetool.vdmj.expressions.Expression;
import org.overturetool.vdmj.lex.Dialect;
import org.overturetool.vdmj.lex.LexException;
import org.overturetool.vdmj.lex.LexLocation;
import org.overturetool.vdmj.lex.LexNameToken;
import org.overturetool.vdmj.lex.LexToken;
import org.overturetool.vdmj.lex.LexTokenReader;
import org.overturetool.vdmj.lex.Token;
import org.overturetool.vdmj.messages.Console;
import org.overturetool.vdmj.messages.InternalException;
import org.overturetool.vdmj.messages.RTLogger;
import org.overturetool.vdmj.modules.Module;
import org.overturetool.vdmj.pog.ProofObligation;
import org.overturetool.vdmj.pog.ProofObligationList;
import org.overturetool.vdmj.runtime.Breakpoint;
import org.overturetool.vdmj.runtime.ClassContext;
import org.overturetool.vdmj.runtime.ClassInterpreter;
import org.overturetool.vdmj.runtime.Context;
import org.overturetool.vdmj.runtime.ContextException;
import org.overturetool.vdmj.runtime.DebuggerException;
import org.overturetool.vdmj.runtime.Interpreter;
import org.overturetool.vdmj.runtime.ModuleInterpreter;
import org.overturetool.vdmj.runtime.ObjectContext;
import org.overturetool.vdmj.runtime.RootContext;
import org.overturetool.vdmj.runtime.SourceFile;
import org.overturetool.vdmj.runtime.StateContext;
import org.overturetool.vdmj.statements.Statement;
import org.overturetool.vdmj.syntax.ParserException;
import org.overturetool.vdmj.util.Base64;
import org.overturetool.vdmj.values.NameValuePairMap;
import org.overturetool.vdmj.values.Value;

public class DBGPReader
{
	private final String host;
	private final int port;
	private final String ideKey;
	private final String expression;
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;
	private final Interpreter interpreter;

	private int sessionId = 0;
	private DBGPStatus status = null;
	private DBGPReason statusReason = null;
	private DBGPCommandType command = null;
	private String transaction = "";
	private DBGPFeatures features;
	private byte separator = '\0';

	private Context breakContext = null;
	private Breakpoint breakpoint = null;
	private Value theAnswer = null;
	private boolean breaksSuspended = false;

	private static final int SOURCE_LINES = 5;

	public static void main(String[] args)
	{
		Settings.usingDBGP = true;

		String host = null;
		int port = -1;
		String ideKey = null;
		Settings.dialect = null;
		String expression = null;
		List<File> files = new Vector<File>();
		List<String> largs = Arrays.asList(args);
		VDMJ controller = null;
		boolean warnings = true;
		String logfile = null;

		for (Iterator<String> i = largs.iterator(); i.hasNext();)
		{
			String arg = i.next();

    		if (arg.equals("-vdmsl"))
    		{
    			controller = new VDMSL();
    		}
    		else if (arg.equals("-vdmpp"))
    		{
    			controller = new VDMPP();
    		}
    		else if (arg.equals("-vdmrt"))
    		{
    			controller = new VDMRT();
    		}
    		else if (arg.equals("-overture"))
    		{
    			controller = new VDMOV();
    		}
    		else if (arg.equals("-h"))
    		{
    			if (i.hasNext())
    			{
    				host = i.next();
    			}
    			else
    			{
    				usage("-h option requires a hostname");
    			}
    		}
    		else if (arg.equals("-p"))
    		{
    			try
    			{
    				port = Integer.parseInt(i.next());
    			}
    			catch (Exception e)
    			{
    				usage("-p option requires a port");
    			}
    		}
    		else if (arg.equals("-k"))
    		{
    			if (i.hasNext())
    			{
    				ideKey = i.next();
    			}
    			else
    			{
    				usage("-k option requires a key");
    			}
    		}
    		else if (arg.equals("-e"))
    		{
    			if (i.hasNext())
    			{
    				expression = i.next();
    			}
    			else
    			{
    				usage("-e option requires an expression");
    			}
    		}
    		else if (arg.equals("-log"))
    		{
    			if (i.hasNext())
    			{
    				logfile = i.next();
    			}
    			else
    			{
    				usage("-log option requires a filename");
    			}
    		}
    		else if (arg.equals("-w"))
    		{
    			warnings = false;
    		}
    		else if (arg.startsWith("-"))
    		{
    			usage("Unknown option " + arg);
    		}
    		else
    		{
    			try
    			{
    				files.add(new File(new URI(arg)));
    			}
    			catch (URISyntaxException e)
    			{
    				usage(e.getMessage() + ": " + arg);
    			}
    			catch (IllegalArgumentException e)
    			{
    				usage(e.getMessage() + ": " + arg);
    			}
    		}
		}

		if (host == null || port == -1 || controller == null ||
			ideKey == null || expression == null || Settings.dialect == null ||
			files.isEmpty())
		{
			usage("Missing mandatory arguments");
		}

		if (Settings.dialect != Dialect.VDM_RT && logfile != null)
		{
			usage("-log can only be used with -vdmrt");
		}

		controller.setWarnings(warnings);

		if (controller.parse(files) == ExitStatus.EXIT_OK)
		{
    		if (controller.typeCheck() == ExitStatus.EXIT_OK)
    		{
				try
				{
					if (logfile != null)
					{
		    			PrintWriter p = new PrintWriter(new FileOutputStream(logfile, true));
		    			RTLogger.setLogfile(p);
					}

					Interpreter i = controller.getInterpreter();
					new DBGPReader(host, port, ideKey, i, expression).run(true);

					RTLogger.dump(true);
	    			System.exit(0);
				}
				catch (ContextException e)
				{
					System.out.println("Initialization: " + e);
					e.ctxt.printStackTrace(Console.out, true);
					RTLogger.dump(true);
					System.exit(3);
				}
				catch (Exception e)
				{
					System.out.println("Initialization: " + e);
					RTLogger.dump(true);
					System.exit(3);
				}
    		}
    		else
    		{
    			System.exit(2);
    		}
		}
		else
		{
			System.exit(1);
		}
	}

	private static void usage(String string)
	{
		System.err.println(string);
		System.err.println("Usage: -h <host> -p <port> -k <ide key> <-vdmpp|-vdmsl|-vdmrt> -e <expression> {<filename URLs>}");
		System.exit(1);
	}

	public DBGPReader(
		String host, int port, String ideKey,
		Interpreter interpreter, String expression)
		throws Exception
	{
		this.host = host;
		this.port = port;
		this.ideKey = ideKey;
		this.expression = expression;
		this.interpreter = interpreter;

		if (port > 0)
		{
			InetAddress server = InetAddress.getByName(host);
			socket = new Socket(server, port);
			input = socket.getInputStream();
			output = socket.getOutputStream();
		}
		else
		{
			socket = null;
			input = System.in;
			output = System.out;
			separator = ' ';
		}

		init();
	}

	public DBGPReader newThread() throws Exception
	{
		DBGPReader r = new DBGPReader(host, port, ideKey, interpreter, null);
		r.command = DBGPCommandType.UNKNOWN;
		r.transaction = "?";
		r.run(false);			// New threads wait for a "run -i"
		return r;
	}

	private void init() throws IOException
	{
		sessionId = Math.abs(new Random().nextInt());
		status = DBGPStatus.STARTING;
		statusReason = DBGPReason.OK;
		features = new DBGPFeatures();

		StringBuilder sb = new StringBuilder();

		sb.append("<init ");
		sb.append("appid=\"");
		sb.append(features.getProperty("language_name"));
		sb.append("\" ");
		sb.append("idekey=\"" + ideKey + "\" ");
		sb.append("session=\"" + sessionId + "\" ");
		sb.append("thread=\"");
		sb.append(Thread.currentThread().getId());
		sb.append("\" ");
		sb.append("parent=\"");
		sb.append(features.getProperty("language_name"));
		sb.append("\" ");
		sb.append("language=\"");
		sb.append(features.getProperty("language_name"));
		sb.append("\" ");
		sb.append("protocol_version=\"");
		sb.append(features.getProperty("protocol_version"));
		sb.append("\"");

		Set<File> files = interpreter.getSourceFiles();
		sb.append(" fileuri=\"");
		sb.append(files.iterator().next().toURI());		// Any old one...
		sb.append("\"");

		sb.append("/>\n");

		write(sb);
	}

	private String readLine() throws IOException
	{
		StringBuilder line = new StringBuilder();
		int c = input.read();

		while (c != '\n' && c > 0)
		{
			if (c != '\r') line.append((char)c);		// Ignore CRs
			c = input.read();
		}

		return (line.length() == 0 && c == -1) ? null : line.toString();
	}

	private void write(StringBuilder data) throws IOException
	{
		byte[] header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes("UTF-8");
		byte[] body = data.toString().getBytes("UTF-8");
		byte[] size = Integer.toString(header.length + body.length).getBytes("UTF-8");

		output.write(size);
		output.write(separator);
		output.write(header);
		output.write(body);
		output.write(separator);

		output.flush();
	}

	private void response(StringBuilder hdr, StringBuilder body) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<response command=\"");
		sb.append(command);
		sb.append("\"");

		if (hdr != null)
		{
			sb.append(" ");
			sb.append(hdr);
		}

		sb.append(" transaction_id=\"");
		sb.append(transaction);
		sb.append("\"");

		if (body != null)
		{
			sb.append(">");
			sb.append(body);
			sb.append("</response>\n");
		}
		else
		{
			sb.append("/>\n");
		}

		write(sb);
	}

	private void errorResponse(DBGPErrorCode errorCode, String reason)
	{
		try
		{
			StringBuilder sb = new StringBuilder();

			sb.append("<error code=\"");
			sb.append(errorCode.value);
			sb.append("\" apperr=\"\"><message>");
			sb.append(reason);
			sb.append("</message></error>");

			response(null, sb);
		}
		catch (IOException e)
		{
			throw new InternalException(29, "DBGP: " + reason);
		}
	}

	private void statusResponse() throws IOException
	{
		statusResponse(status, statusReason);
	}

	private void statusResponse(DBGPStatus s, DBGPReason reason)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();

		status = s;
		statusReason = reason;

		sb.append("status=\"");
		sb.append(status);
		sb.append("\"");
		sb.append(" reason=\"");
		sb.append(statusReason);
		sb.append("\"");

		response(sb, null);
	}

	private StringBuilder breakpointResponse(Breakpoint bp)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<breakpoint id=\"" + bp.number + "\"");
		sb.append(" type=\"line\"");
		sb.append(" state=\"enabled\"");
		sb.append(" filename=\"" + bp.location.file.toURI() + "\"");
		sb.append(" lineno=\"" + bp.location.startLine + "\"");
		sb.append(">");

		if (bp.trace != null)
		{
			sb.append("<expression>" + quote(bp.trace) + "</expression>");
		}

		sb.append("</breakpoint>");

		return sb;
	}

	private StringBuilder stackResponse(LexLocation location, int level)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<stack level=\"" + level + "\"");
		sb.append(" type=\"file\"");
		sb.append(" filename=\"" + location.file.toURI() + "\"");
		sb.append(" lineno=\"" + location.startLine + "\"");
		sb.append(" cmdbegin=\"" + location.startLine + ":" + location.startPos + "\"");
		sb.append("/>");

		return sb;
	}

	private StringBuilder propertyResponse(NameValuePairMap vars)
		throws UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();

		for (Entry<LexNameToken, Value> e: vars.entrySet())
		{
			sb.append(propertyResponse(e.getKey(), e.getValue()));
		}

		return sb;
	}

	private StringBuilder propertyResponse(LexNameToken name, Value value)
		throws UnsupportedEncodingException
	{
		return propertyResponse(
			name.name, name.getExplicit(true).toString(),
			name.module, value.toString());
	}

	private StringBuilder propertyResponse(
		String name, String fullname, String clazz, String value)
		throws UnsupportedEncodingException
    {
    	StringBuilder sb = new StringBuilder();

    	sb.append("<property");
    	sb.append(" name=\"" + quote(name) + "\"");
    	sb.append(" fullname=\"" + quote(fullname) + "\"");
    	sb.append(" type=\"string\"");
    	sb.append(" classname=\"" + clazz + "\"");
    	sb.append(" constant=\"0\"");
    	sb.append(" children=\"0\"");
    	sb.append(" size=\"" + value.length() + "\"");
    	sb.append(" encoding=\"base64\"");
    	sb.append("><![CDATA[");
    	sb.append(Base64.encode(value.getBytes("UTF-8")));
    	sb.append("]]></property>");

    	return sb;
    }

	private void cdataResponse(String msg) throws IOException
	{
		// Send back a CDATA response with a plain message
		response(null, new StringBuilder("<![CDATA[" + quote(msg) + "]]>"));
	}

	private static String quote(String in)
	{
		return in
    		.replaceAll("&", "&amp;")
    		.replaceAll("<", "&lt;")
    		.replaceAll(">", "&gt;")
    		.replaceAll("\\\"", "&quot;");
	}

	public void run(boolean init) throws IOException
	{
		if (init)
		{
			interpreter.init(this);
		}

		String line = null;

		do
		{
			line = readLine();
		}
		while (line != null && process(line));
	}

	public void stopped(Context ctxt, Breakpoint bp)
	{
		if (breaksSuspended)
		{
			return;		// We're inside an eval command, so don't stop
		}

		try
		{
			breakContext = ctxt;
			breakpoint = bp;
			statusResponse(DBGPStatus.BREAK, DBGPReason.OK);

			run(false);

			breakContext = null;
			breakpoint = null;
		}
		catch (Exception e)
		{
			errorResponse(DBGPErrorCode.INTERNAL_ERROR, e.getMessage());
		}
	}

	public void tracing(String display)
	{
    	try
    	{
    		cdataResponse(display);
    	}
    	catch (Exception e)
    	{
    		errorResponse(DBGPErrorCode.INTERNAL_ERROR, e.getMessage());
    	}
	}

	public void complete(DBGPReason reason, ContextException ctxt)
	{
		try
		{
			if (reason == DBGPReason.EXCEPTION && ctxt != null)
			{
				dyingThread(ctxt);
			}
			else
			{
				statusResponse(DBGPStatus.STOPPED, reason);
			}
		}
		catch (IOException e)
		{
			errorResponse(DBGPErrorCode.INTERNAL_ERROR, e.getMessage());
		}
		finally
		{
			try
			{
				if (socket != null)
				{
					socket.close();
				}
			}
			catch (IOException e)
			{
				// ?
			}
		}
	}

	private boolean process(String line)
	{
		boolean carryOn = true;

		try
		{
			command = DBGPCommandType.UNKNOWN;
			transaction = "?";

    		String[] parts = line.split("\\s+");
    		DBGPCommand c = parse(parts);

    		switch (c.type)
    		{
    			case STATUS:
    				processStatus(c);
    				break;

    			case FEATURE_GET:
    				processFeatureGet(c);
    				break;

    			case FEATURE_SET:
    				processFeatureSet(c);
    				break;

    			case RUN:
    				carryOn = processRun(c);
    				break;

    			case EVAL:
    				carryOn = processEval(c);
    				break;

    			case EXPR:
    				carryOn = processExpr(c);
    				break;

    			case STEP_INTO:
    				processStepInto(c);
    				carryOn = false;
    				break;

    			case STEP_OVER:
    				processStepOver(c);
    				carryOn = false;
    				break;

    			case STEP_OUT:
    				processStepOut(c);
    				carryOn = false;
    				break;

    			case STOP:
    				processStop(c);
    				break;

    			case BREAKPOINT_GET:
    				breakpointGet(c);
    				break;

    			case BREAKPOINT_SET:
    				breakpointSet(c);
    				break;

    			case BREAKPOINT_UPDATE:
    				breakpointUpdate(c);
    				break;

    			case BREAKPOINT_REMOVE:
    				breakpointRemove(c);
    				break;

    			case BREAKPOINT_LIST:
    				breakpointList(c);
    				break;

    			case STACK_DEPTH:
    				stackDepth(c);
    				break;

    			case STACK_GET:
    				stackGet(c);
    				break;

    			case CONTEXT_NAMES:
    				contextNames(c);
    				break;

    			case CONTEXT_GET:
    				contextGet(c);
    				break;

    			case PROPERTY_GET:
    				propertyGet(c);
    				break;

    			case SOURCE:
    				processSource(c);
    				break;

    			case STDOUT:
       				processStdout(c);
    				break;

       			case STDERR:
       				processStderr(c);
    				break;

    			case DETACH:
    				carryOn = false;
    				break;

    			case XCMD_OVERTURE_CMD:
    				processOvertureCmd(c);
    				break;

    			case PROPERTY_SET:
    			default:
    				errorResponse(DBGPErrorCode.NOT_AVAILABLE, c.type.value);
    		}
		}
		catch (DBGPException e)
		{
			errorResponse(e.code, e.reason);
		}
		catch (Throwable e)
		{
			errorResponse(DBGPErrorCode.INTERNAL_ERROR, e.toString());
		}

		return carryOn;
	}

	private DBGPCommand parse(String[] parts) throws Exception
	{
		// "<type> [<options>] [-- <base64 args>]"

		List<DBGPOption> options = new Vector<DBGPOption>();
		String args = null;
		boolean doneOpts = false;
		boolean gotXID = false;

		try
		{
			command = DBGPCommandType.lookup(parts[0]);

			for (int i=1; i<parts.length; i++)
			{
				if (doneOpts)
				{
					if (args != null)
					{
						throw new Exception("Expecting one base64 arg after '--'");
					}
					else
					{
						args = parts[i];
					}
				}
				else
				{
	    			if (parts[i].equals("--"))
	    			{
	    				doneOpts = true;
	    			}
	     			else
	    			{
	    				DBGPOptionType opt = DBGPOptionType.lookup(parts[i++]);

	    				if (opt == DBGPOptionType.TRANSACTION_ID)
	    				{
	    					gotXID = true;
	    					transaction = parts[i];
	    				}

						options.add(new DBGPOption(opt, parts[i]));
	     			}
				}
			}

			if (!gotXID)
			{
				throw new Exception("No transaction_id");
			}
		}
		catch (DBGPException e)
		{
			throw e;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new DBGPException(
				DBGPErrorCode.INVALID_OPTIONS, "Option arg missing");
		}
		catch (Exception e)
		{
			if (doneOpts)
			{
				throw new DBGPException(DBGPErrorCode.PARSE, e.getMessage());
			}
			else
			{
				throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, e.getMessage());
			}
		}

		return new DBGPCommand(command, options, args);
	}

	private void checkArgs(DBGPCommand c, int n, boolean data) throws DBGPException
	{
		if (data && c.data == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		if (c.options.size() != n)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}
	}

	private void processStatus(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 1, false);
		statusResponse();
	}

	private void processFeatureGet(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 2, false);
		DBGPOption option = c.getOption(DBGPOptionType.N);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		String feature = features.getProperty(option.value);
		StringBuilder hdr = new StringBuilder();
   		StringBuilder body = new StringBuilder();

		if (feature == null)
		{
			// Unknown feature - unsupported in header; nothing in body
    		hdr.append("feature_name=\"");
    		hdr.append(option.value);
    		hdr.append("\" supported=\"0\"");
		}
		else
		{
			// Known feature - supported in header; body reflects actual support
    		hdr.append("feature_name=\"");
    		hdr.append(option.value);
    		hdr.append("\" supported=\"1\"");
    		body.append(feature);
		}

		response(hdr, body);
	}

	private void processFeatureSet(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 3, false);
		DBGPOption option = c.getOption(DBGPOptionType.N);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		String feature = features.getProperty(option.value);

		if (feature == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		DBGPOption newval = c.getOption(DBGPOptionType.V);

		if (newval == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		features.setProperty(option.value, newval.value);

		StringBuilder hdr = new StringBuilder();

		hdr.append("feature_name=\"");
		hdr.append(option.value);
		hdr.append("\" success=\"1\"");

		response(hdr, null);
	}

	private void dyingThread(ContextException ex)
	{
		try
		{
			breakContext = ex.ctxt;
			breakpoint = new Breakpoint(ex.ctxt.location);
			status = DBGPStatus.STOPPING;
			statusReason = DBGPReason.EXCEPTION;
			errorResponse(DBGPErrorCode.EVALUATION_ERROR, ex.getMessage());

			run(false);

			breakContext = null;
			breakpoint = null;
			statusResponse(DBGPStatus.STOPPED, DBGPReason.EXCEPTION);
		}
		catch (Exception e)
		{
			errorResponse(DBGPErrorCode.INTERNAL_ERROR, e.getMessage());
		}
	}

	private boolean processRun(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 1, false);

		if (status == DBGPStatus.BREAK || status == DBGPStatus.STOPPING)
		{
			if (breakContext != null)
			{
				breakContext.threadState.set(0, null, null);
				status = DBGPStatus.RUNNING;
				statusReason = DBGPReason.OK;
				return false;	// run means continue
			}
			else
			{
				throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
			}
		}

		if (status == DBGPStatus.STARTING && expression == null)
		{
			status = DBGPStatus.RUNNING;
			statusReason = DBGPReason.OK;
			return false;	// a run for a new thread, means continue
		}

		if (status != DBGPStatus.STARTING)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		if (c.data != null)	// data is in "expression"
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		try
		{
			status = DBGPStatus.RUNNING;
			statusReason = DBGPReason.OK;
			interpreter.init(this);
			theAnswer = interpreter.execute(expression, this);
			stdout(theAnswer.toString());
			statusResponse(DBGPStatus.STOPPED, DBGPReason.OK);
		}
		catch (ContextException e)
		{
			dyingThread(e);
		}
		catch (Exception e)
		{
			status = DBGPStatus.STOPPED;
			statusReason = DBGPReason.ERROR;
			errorResponse(DBGPErrorCode.EVALUATION_ERROR, e.getMessage());
		}

		return true;
	}

	private boolean processEval(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 1, true);

		if ((status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
			|| breakpoint == null)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		breaksSuspended = true;

		try
		{
			String exp = c.data;	// Already base64 decoded by the parser
			interpreter.setDefaultName(breakpoint.location.module);
			theAnswer = interpreter.evaluate(exp, breakContext);
			StringBuilder property = propertyResponse(
				exp, exp, interpreter.getDefaultName(), theAnswer.toString());
			StringBuilder hdr = new StringBuilder("success=\"1\"");
			response(hdr, property);
		}
		catch (ContextException e)
		{
			dyingThread(e);
		}
		catch (Exception e)
		{
			errorResponse(DBGPErrorCode.EVALUATION_ERROR, e.getMessage());
		}
		finally
		{
			breaksSuspended = false;
		}

		return true;
	}

	private boolean processExpr(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 1, true);

		if (status == DBGPStatus.BREAK || status == DBGPStatus.STOPPING)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		try
		{
			status = DBGPStatus.RUNNING;
			statusReason = DBGPReason.OK;
			String exp = c.data;	// Already base64 decoded by the parser
			theAnswer = interpreter.execute(exp, this);
			StringBuilder property = propertyResponse(
				exp, exp, interpreter.getDefaultName(), theAnswer.toString());
			StringBuilder hdr = new StringBuilder("success=\"1\"");
			status = DBGPStatus.STOPPED;
			statusReason = DBGPReason.OK;
			response(hdr, property);
		}
		catch (ContextException e)
		{
			dyingThread(e);
		}
		catch (Exception e)
		{
			status = DBGPStatus.STOPPED;
			statusReason = DBGPReason.ERROR;
			errorResponse(DBGPErrorCode.EVALUATION_ERROR, e.getMessage());
		}

		return true;
	}

	private void processStepInto(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 1, false);

		if (breakpoint != null)
		{
	   		breakContext.threadState.set(breakpoint.location.startLine, null, null);
		}

		status = DBGPStatus.RUNNING;
		statusReason = DBGPReason.OK;
	}

	private void processStepOver(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 1, false);

		if (breakpoint != null)
		{
			breakContext.threadState.set(
				breakpoint.location.startLine, breakContext.getRoot(), null);
		}

		status = DBGPStatus.RUNNING;
		statusReason = DBGPReason.OK;
	}

	private void processStepOut(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 1, false);

		if (breakpoint != null)
		{
			breakContext.threadState.set(
				breakpoint.location.startLine, null, breakContext.getRoot().outer);
		}

		status = DBGPStatus.RUNNING;
		statusReason = DBGPReason.OK;
	}

	private void processStop(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 1, false);

		if (breakpoint == null)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		statusResponse(DBGPStatus.STOPPING, DBGPReason.OK);
		DebuggerException e = new DebuggerException("terminated");
		Interpreter.stop(null, e, breakContext);
	}

	private void breakpointGet(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 2, false);

		DBGPOption option = c.getOption(DBGPOptionType.D);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		Breakpoint bp = interpreter.breakpoints.get(Integer.parseInt(option.value));

		if (bp == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_BREAKPOINT, c.toString());
		}

		response(null, breakpointResponse(bp));
	}

	private void breakpointSet(DBGPCommand c)
		throws DBGPException, IOException, URISyntaxException
	{
		DBGPOption option = c.getOption(DBGPOptionType.T);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		DBGPBreakpointType type = DBGPBreakpointType.lookup(option.value);

		if (type == null)
		{
			throw new DBGPException(DBGPErrorCode.BREAKPOINT_TYPE_UNSUPPORTED, option.value);
		}

		option = c.getOption(DBGPOptionType.F);
		File filename = null;

		if (option != null)
		{
			filename = new File(new URI(option.value));
		}
		else
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		option = c.getOption(DBGPOptionType.S);

		if (option != null)
		{
    		if (!option.value.equalsIgnoreCase("enabled"))
    		{
    			throw new DBGPException(DBGPErrorCode.INVALID_BREAKPOINT, option.value);
    		}
		}

		option = c.getOption(DBGPOptionType.N);
		int lineno = 0;

		if (option != null)
		{
			lineno = Integer.parseInt(option.value);
		}

		String condition = null;

		if (c.data != null)
		{
			condition = c.data;
		}
		else
		{
			DBGPOption cond = c.getOption(DBGPOptionType.O);
			DBGPOption hits = c.getOption(DBGPOptionType.H);

			if (cond != null || hits != null)
			{
				String cs = (cond == null) ? ">=" : cond.value;
				String hs = (hits == null) ? "0"  : hits.value;

				if (hs.equals("0"))
				{
					condition = "= 0";		// impossible (disabled)
				}
				else if (cs.equals("=="))
    			{
    				condition = "= " + hs;
    			}
    			else if (cs.equals(">="))
    			{
    				condition = ">= " + hs;
    			}
    			else if (cs.equals("%"))
    			{
    				condition = "mod " + hs;
    			}
    			else
    			{
    				throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
    			}
			}
		}

		Breakpoint bp = null;
		Statement stmt = interpreter.findStatement(filename, lineno);

		if (stmt == null)
		{
			Expression exp = interpreter.findExpression(filename, lineno);

			if (exp == null)
			{
				throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT, filename + ":" + lineno);
			}
			else
			{
				try
				{
					if (exp.breakpoint.number != 0)
					{
						// Multiple threads set BPs multiple times, so...
						bp = exp.breakpoint;	// Re-use the existing one
					}
					else
					{
						bp = interpreter.setBreakpoint(exp, condition);
					}
				}
				catch (ParserException e)
				{
					throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT,
						filename + ":" + lineno + ", " + e.getMessage());
				}
				catch (LexException e)
				{
					throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT,
						filename + ":" + lineno + ", " + e.getMessage());
				}
			}
		}
		else
		{
			try
			{
				if (stmt.breakpoint.number != 0)
				{
					// Multiple threads set BPs multiple times, so...
					bp = stmt.breakpoint;	// Re-use the existing one
				}
				else
				{
					bp = interpreter.setBreakpoint(stmt, condition);
				}
			}
			catch (ParserException e)
			{
				throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT,
					filename + ":" + lineno + ", " + e.getMessage());
			}
			catch (LexException e)
			{
				throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT,
					filename + ":" + lineno + ", " + e.getMessage());
			}
		}

		StringBuilder hdr = new StringBuilder(
			"state=\"enabled\" id=\"" + bp.number + "\"");
		response(hdr, null);
	}

	private void breakpointUpdate(DBGPCommand c) throws DBGPException
	{
		checkArgs(c, 2, false);

		DBGPOption option = c.getOption(DBGPOptionType.D);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		Breakpoint bp = interpreter.breakpoints.get(Integer.parseInt(option.value));

		if (bp == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_BREAKPOINT, c.toString());
		}

		throw new DBGPException(DBGPErrorCode.UNIMPLEMENTED, c.toString());
	}

	private void breakpointRemove(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 2, false);

		DBGPOption option = c.getOption(DBGPOptionType.D);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		Breakpoint old = interpreter.clearBreakpoint(Integer.parseInt(option.value));

		if (old == null)
		{
			// Multiple threads remove BPs multiple times
			// throw new DBGPException(DBGPErrorCode.INVALID_BREAKPOINT, c.toString());
		}

		response(null, null);
	}

	private void breakpointList(DBGPCommand c) throws IOException, DBGPException
	{
		checkArgs(c, 1, false);
		StringBuilder bps = new StringBuilder();

		for (Integer key: interpreter.breakpoints.keySet())
		{
			Breakpoint bp = interpreter.breakpoints.get(key);
			bps.append(breakpointResponse(bp));
		}

		response(null, bps);
	}

	private void stackDepth(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 1, false);

		if (status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		StringBuilder sb = new StringBuilder();
		sb.append(breakContext.getDepth());

		response(null, sb);
	}

	private void stackGet(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 1, false);

		if ((status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
			|| breakpoint == null)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		DBGPOption option = c.getOption(DBGPOptionType.D);
		int depth = -1;

		if (option != null)
		{
			depth = Integer.parseInt(option.value);	// 0 to n-1
		}

		// We omit the last frame, as this is unhelpful (globals),

		int actualDepth = breakContext.getDepth() - 1;

		if (depth >= actualDepth)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_STACK_DEPTH, c.toString());
		}

		if (depth == 0)
		{
			response(null, stackResponse(breakpoint.location, 0));
		}
		else if (depth > 0)
		{
			RootContext ctxt = breakContext.getFrame(depth).getRoot();
			response(null, stackResponse(ctxt.location, depth));
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			Context ctxt = breakContext;

			int d = 0;
			sb.append(stackResponse(breakpoint.location, d++));

			while (d < actualDepth)
			{
				ctxt = breakContext.getFrame(d);
				sb.append(stackResponse(ctxt.location, d++));
			}

			response(null, sb);
		}
	}

	private void contextNames(DBGPCommand c) throws DBGPException, IOException
	{
		if (c.data != null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		DBGPOption option = c.getOption(DBGPOptionType.D);

		if (c.options.size() > ((option == null) ? 1 : 2))
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		StringBuilder names = new StringBuilder();

		names.append("<context name=\"Local\" id=\"0\"/>");
		names.append("<context name=\"Class\" id=\"1\"/>");
		names.append("<context name=\"Global\" id=\"2\"/>");

		response(null, names);
	}

	private NameValuePairMap getContextValues(DBGPContextType context, int depth)
	{
		NameValuePairMap vars = new NameValuePairMap();
		Context frame = breakContext.getFrame(depth);

		switch (context)
		{
			case LOCAL:
				vars.putAll(frame.getFreeVariables());
				break;

			case CLASS:
				RootContext root = frame.getRoot();

				if (root instanceof ObjectContext)
				{
					ObjectContext octxt = (ObjectContext)root;
					vars.putAll(octxt.self.members);
				}
				else if (root instanceof ClassContext)
				{
					ClassContext cctxt = (ClassContext)root;
					vars.putAll(cctxt);
				}
				else
				{
					StateContext sctxt = (StateContext)root;
					vars.putAll(sctxt.stateCtxt);
				}
				break;

			case GLOBAL:
				vars.putAll(interpreter.initialContext);
				break;
		}

		return vars;
	}

	private void contextGet(DBGPCommand c) throws DBGPException, IOException
	{
		if (c.data != null || c.options.size() > 3)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		if (status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		DBGPOption option = c.getOption(DBGPOptionType.C);
		int type = 0;

		if (option != null)
		{
			type = Integer.parseInt(option.value);
		}

		DBGPContextType context = DBGPContextType.lookup(type);

		option = c.getOption(DBGPOptionType.D);
		int depth = 0;

		if (option != null)
		{
			depth = Integer.parseInt(option.value);
		}

		int actualDepth = breakContext.getDepth() - 1;

		if (depth >= actualDepth)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_STACK_DEPTH, c.toString());
		}

		NameValuePairMap vars = getContextValues(context, depth);

		response(null, propertyResponse(vars));
	}

	private void propertyGet(DBGPCommand c) throws DBGPException, IOException
	{
		if (c.data != null || c.options.size() > 4)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		if (status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		DBGPOption option = c.getOption(DBGPOptionType.C);
		int type = 0;

		if (option != null)
		{
			type = Integer.parseInt(option.value);
		}

		DBGPContextType context = DBGPContextType.lookup(type);

		option = c.getOption(DBGPOptionType.D);
		int depth = -1;

		if (option != null)
		{
			depth = Integer.parseInt(option.value);
		}

		option = c.getOption(DBGPOptionType.N);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.CANT_GET_PROPERTY, c.toString());
		}

		LexTokenReader ltr = new LexTokenReader(option.value, Dialect.VDM_PP);
		LexToken token = null;

		try
		{
			token = ltr.nextToken();
		}
		catch (LexException e)
		{
			throw new DBGPException(DBGPErrorCode.CANT_GET_PROPERTY, option.value);
		}

		if (token.isNot(Token.NAME))
		{
			throw new DBGPException(DBGPErrorCode.CANT_GET_PROPERTY, token.toString());
		}

		NameValuePairMap vars = getContextValues(context, depth);
		LexNameToken longname = (LexNameToken)token;
		Value value = vars.get(longname);

		if (value == null)
		{
			throw new DBGPException(
				DBGPErrorCode.CANT_GET_PROPERTY, longname.toString());
		}

		response(null, propertyResponse(longname, value));
	}

	private void processSource(DBGPCommand c) throws DBGPException, IOException
	{
		if (c.data != null || c.options.size() > 4)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		DBGPOption option = c.getOption(DBGPOptionType.B);
		int begin = 1;

		if (option != null)
		{
			begin = Integer.parseInt(option.value);
		}

		option = c.getOption(DBGPOptionType.E);
		int end = 0;

		if (option != null)
		{
			end = Integer.parseInt(option.value);
		}

		option = c.getOption(DBGPOptionType.F);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		File file = null;

		try
		{
			file = new File(new URI(option.value));
		}
		catch (URISyntaxException e)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		SourceFile s = interpreter.getSourceFile(file);
		StringBuilder sb = new StringBuilder();

		if (end == 0)
		{
			end = s.getCount();
		}

		sb.append("<![CDATA[");

		for (int n = begin; n <= end; n++)
		{
			sb.append(quote(s.getLine(n)));
			sb.append("\n");
		}

		sb.append("]]>");
		response(null, sb);
	}

	private void processStdout(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 2, false);
		DBGPOption option = c.getOption(DBGPOptionType.C);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		DBGPRedirect redirect = DBGPRedirect.lookup(option.value);
		Console.directStdout(this, redirect);

		response(new StringBuilder("success=\"1\""), null);
	}

	private void processStderr(DBGPCommand c) throws DBGPException, IOException
	{
		checkArgs(c, 2, false);
		DBGPOption option = c.getOption(DBGPOptionType.C);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		DBGPRedirect redirect = DBGPRedirect.lookup(option.value);
		Console.directStderr(this, redirect);

		response(new StringBuilder("success=\"1\""), null);
	}

	public synchronized void stdout(String line) throws IOException
	{
		StringBuilder sb = new StringBuilder("<stream type=\"stdout\"><![CDATA[");
		sb.append(Base64.encode(line.getBytes("UTF-8")));
		sb.append("]]></stream>\n");
		write(sb);
	}

	public synchronized void stderr(String line) throws IOException
	{
		StringBuilder sb = new StringBuilder("<stream type=\"stderr\"><![CDATA[");
		sb.append(Base64.encode(line.getBytes("UTF-8")));
		sb.append("]]></stream>\n");
		write(sb);
	}

	private void processOvertureCmd(DBGPCommand c)
		throws DBGPException, IOException, URISyntaxException
	{
		checkArgs(c, 2, false);
		DBGPOption option = c.getOption(DBGPOptionType.C);

		if (option == null)
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}

		if (option.value.equals("init"))
		{
			processInit(c);
		}
		else if (option.value.equals("create"))
		{
			processCreate(c);
		}
		else if (option.value.equals("currentline"))
		{
			processCurrentLine(c);
		}
		else if (option.value.equals("source"))
		{
			processCurrentSource(c);
		}
		else if (option.value.equals("coverage"))
		{
			processCoverage(c);
		}
		else if (option.value.equals("pog"))
		{
			processPOG(c);
		}
		else if (option.value.equals("stack"))
		{
			processStack(c);
		}
		else if (option.value.equals("trace"))
		{
			processTrace(c);
		}
		else if (option.value.equals("list"))
		{
			processList();
		}
		else if (option.value.equals("files"))
		{
			processFiles();
		}
		else if (option.value.equals("classes"))
		{
			processClasses(c);
		}
		else if (option.value.equals("modules"))
		{
			processModules(c);
		}
		else if (option.value.equals("default"))
		{
			processDefault(c);
		}
		else if (option.value.equals("log"))
		{
			processLog(c);
		}
		else
		{
			throw new DBGPException(DBGPErrorCode.INVALID_OPTIONS, c.toString());
		}
	}

	private void processInit(DBGPCommand c) throws IOException, DBGPException
	{
		if (status == DBGPStatus.BREAK || status == DBGPStatus.STOPPING)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		LexLocation.clearLocations();
		interpreter.init(this);
		cdataResponse("Global context and test coverage initialized");
	}

	private void processLog(DBGPCommand c) throws IOException
	{
		StringBuilder out = new StringBuilder();

		try
		{
			if (c.data == null)
			{
				if (RTLogger.getLogSize() > 0)
				{
					out.append("Flushing " + RTLogger.getLogSize() + " RT events\n");
				}

				RTLogger.setLogfile(null);
				out.append("RT events now logged to the console");
			}
			else if (c.data.equals("off"))
			{
				RTLogger.enable(false);
				out.append("RT event logging disabled");
			}
			else
			{
				PrintWriter p = new PrintWriter(new FileOutputStream(c.data, true));
				RTLogger.setLogfile(p);
				out.append("RT events now logged to " + c.data);
			}
		}
		catch (FileNotFoundException e)
		{
			out.append("Cannot create RT event log: " + e.getMessage());
		}

		cdataResponse(out.toString());
	}

	private void processCreate(DBGPCommand c) throws DBGPException
	{
		if (!(interpreter instanceof ClassInterpreter))
		{
			throw new DBGPException(DBGPErrorCode.INTERNAL_ERROR, c.toString());
		}

		try
		{
			int i = c.data.indexOf(' ');
			String var = c.data.substring(0, i);
			String exp = c.data.substring(i + 1);

			((ClassInterpreter)interpreter).create(var, exp);
		}
		catch (Exception e)
		{
			throw new DBGPException(DBGPErrorCode.INTERNAL_ERROR, e.getMessage());
		}
	}

	private void processStack(DBGPCommand c) throws IOException, DBGPException
	{
		if ((status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
			|| breakpoint == null)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		OutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);
		pw.println("Stopped at " + breakpoint);
		breakContext.printStackTrace(pw, true);
		pw.close();
		cdataResponse(out.toString());
	}

	private void processTrace(DBGPCommand c) throws DBGPException
	{
		File file = null;
		int line = 0;
		String trace = null;

		try
		{
    		int i = c.data.indexOf(' ');
    		int j = c.data.indexOf(' ', i+1);
    		file = new File(new URI(c.data.substring(0, i)));
    		line = Integer.parseInt(c.data.substring(i+1, j));
    		trace = c.data.substring(j+1);

    		if (trace.length() == 0) trace = null;

    		OutputStream out = new ByteArrayOutputStream();
    		PrintWriter pw = new PrintWriter(out);

    		Statement stmt = interpreter.findStatement(file, line);

    		if (stmt == null)
    		{
    			Expression exp = interpreter.findExpression(file, line);

    			if (exp == null)
    			{
    				throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT,
    					"No breakable expressions or statements at " + file + ":" + line);
    			}
    			else
    			{
    				interpreter.clearBreakpoint(exp.breakpoint.number);
    				Breakpoint bp = interpreter.setTracepoint(exp, trace);
    				pw.println("Created " + bp);
    				pw.println(interpreter.getSourceLine(bp.location));
    			}
    		}
    		else
    		{
    			interpreter.clearBreakpoint(stmt.breakpoint.number);
    			Breakpoint bp = interpreter.setTracepoint(stmt, trace);
    			pw.println("Created " + bp);
    			pw.println(interpreter.getSourceLine(bp.location));
    		}

    		pw.close();
    		cdataResponse(out.toString());
		}
		catch (Exception e)
		{
			throw new DBGPException(DBGPErrorCode.CANT_SET_BREAKPOINT, e.getMessage());
		}
	}

	private void processList() throws IOException
	{
		Map<Integer, Breakpoint> map = interpreter.getBreakpoints();
		OutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);

		for (Integer key: map.keySet())
		{
			Breakpoint bp = map.get(key);
			pw.println(bp.toString());
			pw.println(interpreter.getSourceLine(bp.location));
		}

		pw.close();
		cdataResponse(out.toString());
	}

	private void processFiles() throws IOException
	{
		Set<File> filenames = interpreter.getSourceFiles();
		OutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);

		for (File file: filenames)
		{
			pw.println(file.getPath());
		}

		pw.close();
		cdataResponse(out.toString());
	}

	private void processClasses(DBGPCommand c) throws IOException, DBGPException
	{
		if (!(interpreter instanceof ClassInterpreter))
		{
			throw new DBGPException(DBGPErrorCode.INTERNAL_ERROR, c.toString());
		}

		ClassInterpreter cinterpreter = (ClassInterpreter)interpreter;
		String def = cinterpreter.getDefaultName();
		ClassList classes = cinterpreter.getClasses();
		OutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);

		for (ClassDefinition cls: classes)
		{
			if (cls.name.name.equals(def))
			{
				pw.println(cls.name.name + " (default)");
			}
			else
			{
				pw.println(cls.name.name);
			}
		}

		pw.close();
		cdataResponse(out.toString());
	}

	private void processModules(DBGPCommand c) throws DBGPException, IOException
	{
		if (!(interpreter instanceof ModuleInterpreter))
		{
			throw new DBGPException(DBGPErrorCode.INTERNAL_ERROR, c.toString());
		}

		ModuleInterpreter minterpreter = (ModuleInterpreter)interpreter;
		String def = minterpreter.getDefaultName();
		List<Module> modules = minterpreter.getModules();
		OutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);

		for (Module m: modules)
		{
			if (m.name.name.equals(def))
			{
				pw.println(m.name.name + " (default)");
			}
			else
			{
				pw.println(m.name.name);
			}
		}

		pw.close();
		cdataResponse(out.toString());
	}

	private void processDefault(DBGPCommand c) throws DBGPException
	{
		try
		{
			interpreter.setDefaultName(c.data);
			cdataResponse("Default set to " + interpreter.getDefaultName());
		}
		catch (Exception e)
		{
			throw new DBGPException(DBGPErrorCode.INTERNAL_ERROR, c.toString());
		}
	}

	private void processCoverage(DBGPCommand c)
		throws DBGPException, IOException, URISyntaxException
	{
		if (status == DBGPStatus.BREAK)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		File file = new File(new URI(c.data));
		SourceFile source = interpreter.getSourceFile(file);

		if (source == null)
		{
			cdataResponse(file + ": file not found");
		}
		else
		{
			OutputStream out = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(out);
			source.printCoverage(pw);
			pw.close();
			cdataResponse(out.toString());
		}
	}

	private void processCurrentLine(DBGPCommand c) throws DBGPException, IOException
	{
		if ((status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
			|| breakpoint == null)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		OutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);
		pw.println("Stopped at " + breakpoint);
		pw.println(interpreter.getSourceLine(
			breakpoint.location.file, breakpoint.location.startLine, ":  "));
		pw.close();
		cdataResponse(out.toString());
	}

	private void processCurrentSource(DBGPCommand c) throws DBGPException, IOException
	{
		if ((status != DBGPStatus.BREAK && status != DBGPStatus.STOPPING)
			|| breakpoint == null)
		{
			throw new DBGPException(DBGPErrorCode.NOT_AVAILABLE, c.toString());
		}

		File file = breakpoint.location.file;
		int current = breakpoint.location.startLine;

		int start = current - SOURCE_LINES;
		if (start < 1) start = 1;
		int end = start + SOURCE_LINES*2 + 1;

		StringBuilder sb = new StringBuilder();

		for (int src = start; src < end; src++)
		{
			sb.append(interpreter.getSourceLine(
				file, src, (src == current) ? ":>>" : ":  "));
			sb.append("\n");
		}

		cdataResponse(sb.toString());
	}

	private void processPOG(DBGPCommand c) throws IOException
	{
		ProofObligationList all = interpreter.getProofObligations();
		ProofObligationList list = null;

		if (c.data.equals("*"))
		{
			list = all;
		}
		else
		{
			list = new ProofObligationList();
			String name = c.data + "(";

			for (ProofObligation po: all)
			{
				if (po.name.indexOf(name) >= 0)
				{
					list.add(po);
				}
			}
 		}

		if (list.isEmpty())
		{
			cdataResponse("No proof obligations generated");
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Generated ");
			sb.append(plural(list.size(), "proof obligation", "s"));
			sb.append(":\n");
			sb.append(list);
			cdataResponse(sb.toString());
		}
	}

	private String plural(int n, String s, String pl)
	{
		return n + " " + (n != 1 ? s + pl : s);
	}
}

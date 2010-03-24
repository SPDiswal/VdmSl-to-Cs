package org.overture.ide.debug.utils.communication;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ui.services.IDisposable;
import org.overture.ide.debug.core.IDebugConstants;
import org.overture.ide.debug.core.model.VdmDebugTarget;
import org.overture.ide.debug.utils.xml.XMLNode;
import org.overture.ide.debug.utils.xml.XMLParser;
import org.overture.ide.debug.utils.xml.XMLTagNode;
import org.overturetool.vdmj.debug.DBGPRedirect;
import org.overturetool.vdmj.expressions.NewExpression;

public class DebugCommunication implements IDisposable{

	static private DebugCommunication instance = null;
	private ServerSocket server;
	private boolean keepAlive = true;
	private List<Socket> sockets;
	private Map<String, VdmDebugTarget> targets = new HashMap<String, VdmDebugTarget>();
	private int sessionId = 0;

	private DebugCommunication() throws IOException {
		int portNumber = findFreePort();
		if(portNumber == -1) 
			throw new IOException("Debug communication: no ports available");//very unlikely 
		server = new ServerSocket(portNumber);
		System.out.println("listning on port: " + portNumber);
		//server.setSoTimeout(50000);
		
		
		Thread t = new Thread(new ThreadAccept());
		t.setName("DebugCommunication Socket Listener");
		t.start();		
	}

	public String getSessionId(){
		return Integer.toString(++sessionId);
	}
	
	static public DebugCommunication getInstance() throws IOException {
		if (instance == null) {
			instance = new DebugCommunication();
			return instance;
		} else {
			return instance;
		}
	}

	public int getPort() {
		return this.server.getLocalPort();
	}

	public void RegisterDebugTarger(String sessionId, VdmDebugTarget target) throws DebugException {

		if (!targets.containsKey(sessionId)) {
			targets.put(sessionId, target);
		}else {
			throw new DebugException(new Status(IStatus.ERROR, IDebugConstants.PLUGIN_ID, "Failed to register target: session Id already exists"));
		}
	}

	class ThreadAccept implements Runnable {

		public void run() {
			while (keepAlive) {
				try {
					Socket s = server.accept();
					receive(s);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}
	
	private void receive(Socket s) throws IOException
	{
		BufferedInputStream input = new BufferedInputStream(s.getInputStream());
		// <ascii length> \0 <XML data> \0

		int c = input.read();
		int length = 0;

		while (c >= '0' && c <= '9')
		{
			length = length * 10 + (c - '0');
			c = input.read();
		}

		if (c == -1)
		{
			//connected = false; // End of thread
			return;
		}

		if (c != 0)
		{
			throw new IOException("Malformed DBGp count on " + this);
		}

		byte[] data = new byte[length];
		int offset = 0;
		int remaining = length;
		int retries = 10;
		int done = input.read(data, offset, remaining);

		while (done < remaining && --retries > 0)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			remaining -= done;
			offset += done;
			done = input.read(data, offset, remaining);
		}

		if (retries == 0)
		{
			throw new IOException("Timeout DBGp reply on initialization, got [" + new String(data) + "]");
		}

		if (done != remaining)
		{
			throw new IOException("Short DBGp reply on initialization got [" + new String(data) + "]");
		}

		if (input.read() != 0)
		{
			throw new IOException("Malformed DBGp terminator on initialization, got [" + new String(data) + "]");
		}

		XMLParser parser = new XMLParser(data);
		XMLNode node = parser.readNode();

		// if (trace) System.err.println("[" + id + "] " + node); // diags!

		XMLTagNode tagnode = (XMLTagNode) node;

		if (tagnode.tag.equals("init")) {

			System.out.println("Process Init: " + tagnode);
			processInit(tagnode,s);
		} else {
			System.out.println("Unexpected tag: " + tagnode.tag);
		}
		
			
	}
	
	private void processInit(XMLTagNode tagnode, Socket s) throws IOException
	{
		
		String sessionId = tagnode.getAttr("idekey");
		
		if(targets.containsKey(sessionId)){
			try {
				targets.get(sessionId).newThread(tagnode,s);
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		

	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free
	 * port.
	 * 
	 * @return a free port number on localhost, or -1 if unable to find a free
	 *         port
	 */
	public static int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void disposeTarget(String sessionId) {
		if(targets.containsKey(sessionId)){
			targets.remove(sessionId);
		}
		
	}

	
}

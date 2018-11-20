package com.ambraspace.btalllights;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Communicator
{
	
	public static final Logger logger = Maintainer.logger;
	
	private String serverAddress;
	private int serverPort;
	
	private List<CommunicationListener> listeners = new ArrayList<CommunicationListener>();
	
	Socket monitorSession = null;
	BufferedReader mInput = null;
	PrintWriter mOutput = null;
	
	
	public Communicator(String serverAddress, int serverPort) throws UnknownHostException, IOException
	{
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		
		connect();
		startMonitor();
		
	}
	
	private void connect() throws UnknownHostException, IOException
	{

		logger.fine("Connecting to " + this.serverAddress);
		monitorSession = new Socket(this.serverAddress, this.serverPort);
		mInput = new BufferedReader(new InputStreamReader(monitorSession.getInputStream()));
		mOutput = new PrintWriter(monitorSession.getOutputStream());

		int slovo;
		StringBuilder sb = new StringBuilder();
		
		while ((slovo=mInput.read())!=-1) {
			sb.append((char)slovo);
			if (sb.toString().endsWith("##"))
			{
				break;
			}
		}
		
		if (!sb.toString().equals("*#*1##"))
		{
			logger.severe("Unrecognized data received! (" + sb.toString() + ")");
			mOutput.close();
			mInput.close();
			monitorSession.close();
			throw new IOException();
		}
		
		logger.fine("<-- *#*1##");
		logger.fine("--> *99*1##");
		mOutput.print("*99*1##");
		mOutput.flush();
		
		sb.delete(0, sb.length());

		while ((slovo=mInput.read())!=-1) {
			sb.append((char)slovo);
			if (sb.toString().endsWith("##"))
			{
				break;
			}
		}
		
		if (!sb.toString().equals("*#*1##"))
		{
			logger.severe("Unrecognized data received! (" + sb.toString() + ")");
			mOutput.close();
			mInput.close();
			monitorSession.close();
			throw new IOException();
		}

		logger.fine("<-- *#*1##");

	}
	
	private void startMonitor()
	{
		
		Runnable listener = new Runnable()
			{
				
				@Override
				public void run()
				{
					int slovo;
					StringBuilder sb = new StringBuilder();
					try 
					{
						while ((slovo=mInput.read())!=-1)
						{
							sb.append((char)slovo);
							if (sb.toString().endsWith("##"))
							{
								for (CommunicationListener cl:listeners)
								{
									cl.receiveMessage(sb.toString());
								}
								sb.delete(0, sb.length());
							}
						}
						mOutput.close();
						mInput.close();
						monitorSession.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			};

		new Thread(listener).start();
		
		logger.fine("Monitor session started.");
		
	}
	
	
	public boolean sendMessage(String message) throws UnknownHostException, IOException
	{
		
		logger.fine("Trying to send command: " + message);
		
		Socket commandSession = null;
		BufferedReader cInput = null;
		PrintWriter cOutput = null;
		
		logger.fine("Starting new command session.");
		commandSession = new Socket(this.serverAddress, this.serverPort);
		cInput = new BufferedReader(new InputStreamReader(commandSession.getInputStream()));
		cOutput = new PrintWriter(commandSession.getOutputStream());
		
		int slovo;
		StringBuilder sb = new StringBuilder();
		
		while ((slovo=cInput.read())!=-1) {
			sb.append((char)slovo);
			if (sb.toString().endsWith("##"))
			{
				break;
			}
		}
		
		if (!sb.toString().equals("*#*1##"))
		{
			logger.severe("Unrecognized data received! (" + sb.toString() + ")");
			cOutput.close();
			cInput.close();
			commandSession.close();
			return false;
		}
		
		logger.fine("<-- *#*1##");
		logger.fine("--> *99*0##");
		cOutput.print("*99*0##");
		cOutput.flush();
		
		sb.delete(0, sb.length());

		while ((slovo=cInput.read())!=-1) {
			sb.append((char)slovo);
			if (sb.toString().endsWith("##"))
			{
				break;
			}
		}
		
		if (!sb.toString().equals("*#*1##"))
		{
			logger.severe("Unrecognized data received! (" + sb.toString() + ")");
			cOutput.close();
			cInput.close();
			commandSession.close();
			return false;
		}

		logger.fine("<-- *#*1##");
		logger.info("--> " + message);
		cOutput.print(message);
		cOutput.flush();
		
		sb.delete(0, sb.length());
		
		while ((slovo=cInput.read())!=-1)
		{
			sb.append((char)slovo);
			if (sb.toString().endsWith("##"))
			{
				logger.info("<-- " + sb.toString());
				if (sb.toString().equals("*#*1##"))
				{
					logger.fine("Command successfully sent.");
					cOutput.close();
					cInput.close();
					commandSession.close();
					return true;
				} else if (sb.toString().equals("*#*0##"))
				{
					logger.warning("Command not understood by the server.");
					cOutput.close();
					cInput.close();
					commandSession.close();
					return false;
				}
				sb.delete(0, sb.length());
			}
		}

		logger.warning("Session ended prematurelly!");
		cOutput.close();
		cInput.close();
		commandSession.close();
		return false;
		
	}
	
	
	public void addCommunicationListener(CommunicationListener cl)
	{
		if (cl != null)
		{
			listeners.add(cl);
		}
	}
	
	
	public void removeCommunicaionListener(CommunicationListener cl)
	{
		listeners.remove(cl);
	}
	
	
	public void clearCommunicatioListeners() {
		listeners.clear();
	}
	
}

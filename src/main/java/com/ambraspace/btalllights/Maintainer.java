package com.ambraspace.btalllights;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ambraspace.btalllights.exceptions.PropertiesCreationException;

public class Maintainer
{
	
	private static final Logger logger = Logger.getLogger("Maintainer");
	private SortedSet<Switch> devices = new TreeSet<Switch>();
	private MaintainerOptions options;
	
	public Maintainer(MaintainerOptions options) throws ClassNotFoundException, UnknownHostException, IOException
	{
		this.options = options;
		
		initiate();
		
	}
	
	private void initiate() throws ClassNotFoundException, UnknownHostException, IOException
	{

		// process SQLite file and add devices to a collection
		
		Class.forName("org.sqlite.JDBC");
		
	    Connection connection = null;
	    try
	    {
	      // create a database connection
	      connection = DriverManager.getConnection("jdbc:sqlite:" + options.getDbFile());
	      Statement statement = connection.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec.

	      logger.info("Connected to the database. Collecting devices' info.");
	      
	      ResultSet rs = statement.executeQuery("select mode, address, interface, title from light");
	      Switch d = null;
	      while(rs.next())
	      {
	    	  int a, pl;
	    	  String tmp = rs.getString("address");
	    	  a=Integer.parseInt(tmp.substring(0, 2));
	    	  pl=Integer.parseInt(tmp.substring(2,4));

	    	  if (rs.getInt("mode")==0)
	    	  {
	    		  d = new Switch(rs.getString("title"),
	    				  Integer.parseInt(rs.getString("interface")), a, pl);
		    	  devices.add(d);
	    	  } else if (rs.getInt("mode")==1)
	    	  {
	    		  d = new Dimmer(rs.getString("title"),
	    				  Integer.parseInt(rs.getString("interface")), a, pl);
		    	  devices.add(d);
	    	  } else {
	    		  logger.warning("Unknown device mode!");
	    	  }
	    	  
	      }
	    }
	    catch(SQLException e)
	    {
	      // if the error message is "out of memory", 
	      // it probably means no database file is found
	      logger.severe(e.getMessage());
	    }
	    finally
	    {
	      try
	      {
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	    	  logger.severe(e.getMessage());
	      }
	    }
	    
	    logger.info("" + devices.size() + " device(s) collected.");
	    
		// establish connection to the Bticino server
	    
	    Communicator communicator = new Communicator(options.getServerAddress(), options.getServerPort());
	    
	    CommunicationListener listener = new CommunicationListener()
			{
				@Override
				public void receiveMessage(String message)
				{
					String tmp;
					if (message.startsWith("*") && message.endsWith("##"))
					{
						tmp = message.substring(1, message.length() - 2);
					} else
					{
						logger.warning("Unrecognized reply: " + message);
						return;
					}
					Scanner sc1 = new Scanner(tmp);
					sc1.useDelimiter("\\*");
					int who, what;
					String where;
					who = sc1.nextInt();
					what = sc1.nextInt();
					where = sc1.next();
					sc1.close();
					int four;
					if ((four=where.indexOf("#4#"))<2)
					{
						logger.warning("Unrecognized reply: " + message);
						return;
					}
					String address = where.substring(0, four);
					int iface = Integer.parseInt(where.substring(four+3));
					int a, pl;
					if (address.length() == 2)
					{
						a=Integer.parseInt(address.substring(0,1));
						pl=Integer.parseInt(address.substring(1,2));
					} else if (address.length() == 4)
					{
						a=Integer.parseInt(address.substring(0,2));
						pl=Integer.parseInt(address.substring(2,4));
					} else
					{
						logger.warning("Unrecognized reply: " + message);
						return;
					}
					
					if (who!=1)
					{
						logger.warning("Unrecognized reply: " + message);
						return;
					}
					if (what<0 || what>10)
					{
						logger.warning("Unrecognized reply: " + message);
						return;
					}
					
					logger.info("<-- " + message);
					Switch tmpDev = new Switch("DUMMY", iface, a, pl);
					SortedSet<Switch> tmpSet = devices.tailSet(tmpDev);
					if (tmpSet.isEmpty())
					{
						logger.warning(String.format("Unknown device: (IF=%d, A=%d, PL=%d)\n", tmpDev.getIface(), tmpDev.getA(), tmpDev.getPl()));
						return;
					}
					Switch found = tmpSet.first();
					if (found instanceof Dimmer)
					{
						Dimmer dimmer = (Dimmer)found;
						dimmer.setIntensity(what);
						if (dimmer.getIntensity()>0)
						{
							dimmer.setStatus(Switch.STATUS_ON);
						} else
						{
							dimmer.setStatus(Switch.STATUS_OFF);
						}
					} else {
						found.setStatus(what);
					}
				}
			};
			
		communicator.addCommunicationListener(listener);
		
		Runnable check = new Runnable()
			{
				@Override
				public void run()
				{

					try
					{
						communicator.sendMessage("*#1*0##");
					} catch (UnknownHostException e1)
					{
						e1.printStackTrace();
					} catch (IOException e1)
					{
						e1.printStackTrace();
					}
					
					try
					{
						TimeUnit.SECONDS.sleep(options.getDelayAfterBroadcast());
					} catch (InterruptedException e2)
					{
						logger.warning("Thread interrupted!");
						e2.printStackTrace();
					}
					
					Iterator<Switch> it = null;
					while (!Thread.interrupted())
					{
						Switch currentDevice = null;
						it = devices.iterator();
						while (it.hasNext())
						{
							currentDevice = it.next();
							try
							{
								communicator.sendMessage(currentDevice.statusRequest());
							} catch (UnknownHostException e)
							{
								e.printStackTrace();
							} catch (IOException e)
							{
								e.printStackTrace();
							}
							try
							{
								TimeUnit.SECONDS.sleep(options.getDelayAfterUnicast());
							} catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}

				}
			};
			
		new Thread(check).start();

		// open port for command reception
		
		class RequestProcessor implements Runnable {

			private Socket rq = null;
			BufferedReader input = null;
			PrintWriter output = null;
			
			public RequestProcessor(Socket rq) throws IOException
			{
				this.rq = rq;
				input = new BufferedReader(new InputStreamReader(this.rq.getInputStream()));
				output = new PrintWriter(this.rq.getOutputStream(), true);
			}
			
			@Override
			public void run()
			{
				String line = null;
				try
				{
					line=input.readLine();
					output.println(processRequest(line));
					output.close();
					input.close();
					rq.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			private String processRequest(String rq)
			{
				logger.info("Request received: " + rq);
				JSONArray retVal = new JSONArray();
				try 
				{
					switch (rq)
					{
						case "LIST":
							JSONObject item = null;
							for (Switch s:devices)
							{
								item = new JSONObject();
								item.put("if", s.getIface());
								item.put("a", s.getA());
								item.put("pl", s.getPl());
								item.put("name", s.getName());
								item.put("status", s.getStatus());
								if (s instanceof Dimmer)
								{
									item.put("intensity", ((Dimmer)s).getIntensity());
								}
								retVal.put(item);
							}
							break;
						default:
					}
				} catch (JSONException e)
				{
					e.printStackTrace();
				}
				return retVal.toString();
			}
			
		}
		
		logger.info("Starting service on port " + options.servicePort + "...");
		ServerSocket server = new ServerSocket(options.servicePort);
		Socket request = null;
		while (!server.isClosed())
		{
			request = server.accept();
			new Thread(new RequestProcessor(request)).start();
			
		}
		server.close();
		
	}
	
	public static void main(String[] args)
	{
		
		String os = System.getProperties().getProperty("os.name");
		String home = System.getProperties().getProperty("user.home");
		
		File settingsDir = new File(home + File.separator +
				(os.startsWith("Linux") ? "." : "") + "btalllights");
		
		MaintainerOptions options = null;
		try
		{
			options = new MaintainerOptions(settingsDir);
		} catch (PropertiesCreationException e)
		{
			e.printStackTrace();
			return;
		}
		
		if (options.isDebug())
		{
			logger.setLevel(Level.ALL);
			Communicator.logger.setLevel(Level.ALL);
			MaintainerOptions.logger.setLevel(Level.ALL);
		} else
		{
			logger.setLevel(Level.WARNING);
			Communicator.logger.setLevel(Level.WARNING);
			MaintainerOptions.logger.setLevel(Level.WARNING);
		}
		
		try
		{
			new Maintainer(options);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
			
	}

}

package com.ambraspace.btalllights;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.ambraspace.btalllights.exceptions.PropertiesCreationException;


public class MaintainerOptions
{
	
	public static final Logger logger = Logger.getLogger("MaintainerOptions");
	
	private static final String OPTION_SERVER_ADDRESS = "serverAddress";
	private static final String OPTION_SERVER_PORT = "serverPort";
	private static final String OPTION_DB_FILE = "dbFile";
	private static final String OPTION_DISPLAY_WINDOW = "displayWindow";
	private static final String OPTION_DEBUG = "debug";
	private static final String OPTION_DELAY_AFTER_BROADCAST = "delayAfterBroadcast";
	private static final String OPTION_DELAY_AFTER_UNICAST = "delayAfterUnicast";
	private static final String OPTION_SERVICE_PORT = "servicePort";

	
	private final File propertiesFile;
	
	// Default options
	String serverAddress = "192.168.1.200";
	int serverPort = 20000;
	String dbFile = "installation.db";
	boolean displayWindow = true;
	boolean debug = true;
	int delayAfterBroadcast = 10;
	int delayAfterUnicast = 6;
	int servicePort = 12345;
	
	
	/**
	 * Creates new MaintainerOptions object with default values.
	 * @throws PropertiesCreationException if properties file can not be created or read 
	 */
	public MaintainerOptions(File baseDir) throws PropertiesCreationException {
		
		if (baseDir.exists()) {
			if (baseDir.isDirectory()) {
				this.propertiesFile = new File(baseDir, "properties.ini");
			} else {
				throw new PropertiesCreationException();
			}
		} else {
			if (baseDir.mkdirs()) {
				this.propertiesFile = new File(baseDir, "properties.ini");
			} else {
				throw new PropertiesCreationException();
			}
		}
		
		try {
			loadOptions();
		} catch (IOException e) {
			throw new PropertiesCreationException();
		}
		
	}
	
	
	public String getServerAddress()
	{
		return serverAddress;
	}

	
	private void setServerAddress(String serverAddress)
	{
		String oldValue = this.serverAddress;
		if (serverAddress.trim().equals("") || serverAddress.trim().equals("null"))
		{
			this.serverAddress = null;
		} else
		{
			this.serverAddress = serverAddress;
		}
		if (oldValue != this.serverAddress && ((oldValue != null && !oldValue.equals(this.serverAddress))
				|| (this.serverAddress != null && !this.serverAddress.equals(oldValue))))
		{
			saveOptions();
		}
	}

	
	public int getServerPort()
	{
		return serverPort;
	}

	
	private void setServerPort(int serverPort)
	{
		int oldValue = this.serverPort;
		this.serverPort = serverPort;
		if (oldValue!=this.serverPort)
		{
			saveOptions();
		}
	}


	public String getDbFile()
	{
		return dbFile;
	}

	
	private void setDbFile(String dbFile)
	{
		String oldValue = this.dbFile;
		if (dbFile.trim().equals("") || dbFile.trim().equals("null"))
		{
			this.dbFile = null;
		} else
		{
			this.dbFile = dbFile;
		}
		if (oldValue != this.dbFile && ((oldValue != null && !oldValue.equals(this.dbFile))
				|| (this.dbFile != null && !this.dbFile.equals(oldValue))))
		{
			saveOptions();
		}
	}


	
	public boolean isDisplayWindow()
	{
		return displayWindow;
	}

	
	private void setDisplayWindow(boolean displayWindow)
	{
		boolean oldValue = this.displayWindow;
		this.displayWindow = displayWindow;
		if (oldValue != this.displayWindow)
		{
			saveOptions();
		}
	}
	
	


	
	public boolean isDebug()
	{
		return debug;
	}


	
	private void setDebug(boolean debug)
	{
		boolean oldValue = this.debug;
		this.debug = debug;
		if (oldValue!=this.debug)
		{
			saveOptions();
		}
	}

	
	
	public int getDelayAfterBroadcast()
	{
		return delayAfterBroadcast;
	}


	
	private void setDelayAfterBroadcast(int delayAfterBroadcast)
	{
		int oldValue = this.delayAfterBroadcast;
		this.delayAfterBroadcast = delayAfterBroadcast;
		if (oldValue!=this.delayAfterBroadcast)
		{
			saveOptions();
		}
	}


	
	public int getDelayAfterUnicast()
	{
		return delayAfterUnicast;
	}


	
	private void setDelayAfterUnicast(int delayAfterUnicast)
	{
		int oldValue = this.delayAfterUnicast;
		this.delayAfterUnicast = delayAfterUnicast;
		if (oldValue!=this.delayAfterUnicast)
		{
			saveOptions();
		}
	}


	public File getPropertiesFile() {
		return propertiesFile;
	}

	
	public int getServicePort()
	{
		return servicePort;
	}


	
	private void setServicePort(int servicePort)
	{
		int oldValue = this.servicePort;
		if (servicePort<0 || servicePort>65535) {
			throw new RuntimeException("Port value not allowed.");
		}
		this.servicePort = servicePort;
		if (oldValue!=this.servicePort)
		{
			saveOptions();
		}
	}


	private void loadOptions() throws IOException {

		/*
		 * Create empty Properties and read them from the file
		 */
		Properties properties = new Properties();

		if (!propertiesFile.exists()) {
			propertiesFile.createNewFile();
		}


		FileInputStream fin = new FileInputStream(propertiesFile);
		properties.load(fin);
		fin.close();

		/*
		 * Set Properties values from the file. If there is an item missing in the file,
		 * set it to the default value in newProps.
		 * If an option in the file has invalid value it will be set at this point,
		 * so this must be corrected following this.
		 */
		Properties newProps = new Properties();
		newProps.setProperty(OPTION_SERVER_ADDRESS,
				properties.getProperty(OPTION_SERVER_ADDRESS, "" + this.serverAddress));
		newProps.setProperty(OPTION_SERVER_PORT,
				properties.getProperty(OPTION_SERVER_PORT, "" + this.serverPort));
		newProps.setProperty(OPTION_DB_FILE,
				properties.getProperty(OPTION_DB_FILE, "" + this.dbFile));
		newProps.setProperty(OPTION_DISPLAY_WINDOW,
				properties.getProperty(OPTION_DISPLAY_WINDOW, "" + this.displayWindow));
		newProps.setProperty(OPTION_DEBUG,
				properties.getProperty(OPTION_DEBUG, "" + this.debug));
		newProps.setProperty(OPTION_DELAY_AFTER_BROADCAST,
				properties.getProperty(OPTION_DELAY_AFTER_BROADCAST, "" + this.delayAfterBroadcast));
		newProps.setProperty(OPTION_DELAY_AFTER_UNICAST,
				properties.getProperty(OPTION_DELAY_AFTER_UNICAST, "" + this.delayAfterUnicast));
		newProps.setProperty(OPTION_SERVICE_PORT,
				properties.getProperty(OPTION_SERVICE_PORT, "" + this.servicePort));
		
		
		/*
		 * Set also private fields according to values in the file
		 * (the values may be invalid at this point).
		 * Intention is to avoid unnecessary write with setters used after this.
		 */
		this.serverAddress = newProps.getProperty(OPTION_SERVER_ADDRESS);
		this.serverPort = Integer.parseInt(newProps.getProperty(OPTION_SERVER_PORT));
		this.dbFile = newProps.getProperty(OPTION_DB_FILE);
		this.displayWindow = Boolean.parseBoolean(newProps.getProperty(OPTION_DISPLAY_WINDOW));
		this.debug = Boolean.parseBoolean(newProps.getProperty(OPTION_DEBUG));
		this.delayAfterBroadcast = Integer.parseInt(newProps.getProperty(OPTION_DELAY_AFTER_BROADCAST));
		this.delayAfterUnicast = Integer.parseInt(newProps.getProperty(OPTION_DELAY_AFTER_UNICAST));
		this.servicePort = Integer.parseInt(newProps.getProperty(OPTION_SERVICE_PORT));
		
		
		/*
		 * Now set private fields from properties (it's important for the values
		 * to pass through setters so they are valid).
		 */
		this.setServerAddress(newProps.getProperty(OPTION_SERVER_ADDRESS));
		this.setServerPort(Integer.parseInt(newProps.getProperty(OPTION_SERVER_PORT)));
		this.setDbFile(newProps.getProperty(OPTION_DB_FILE));
		this.setDisplayWindow(Boolean.parseBoolean(newProps.getProperty(OPTION_DISPLAY_WINDOW)));
		this.setDebug(Boolean.parseBoolean(newProps.getProperty(OPTION_DEBUG)));
		this.setDelayAfterBroadcast(Integer.parseInt(newProps.getProperty(OPTION_DELAY_AFTER_BROADCAST)));
		this.setDelayAfterUnicast(Integer.parseInt(newProps.getProperty(OPTION_DELAY_AFTER_UNICAST)));
		this.setServicePort(Integer.parseInt(newProps.getProperty(OPTION_SERVICE_PORT)));

		
		/*
		 * For the following comparison we need to set newProps to valid values.
		 */
		
		newProps.setProperty(OPTION_SERVER_ADDRESS, "" + this.serverAddress);
		newProps.setProperty(OPTION_SERVER_PORT, "" + this.serverPort);
		newProps.setProperty(OPTION_DB_FILE, "" + this.dbFile);
		newProps.setProperty(OPTION_DISPLAY_WINDOW, "" + this.displayWindow);
		newProps.setProperty(OPTION_DEBUG, "" + this.debug);
		newProps.setProperty(OPTION_DELAY_AFTER_BROADCAST, "" + this.delayAfterBroadcast);
		newProps.setProperty(OPTION_DELAY_AFTER_UNICAST, "" + this.delayAfterUnicast);
		newProps.setProperty(OPTION_SERVICE_PORT, "" + this.servicePort);
		
		/*
		 * Avoid unnecessary write.
		 */
		
		if (!newProps.equals(properties)) {
			saveOptions();
		}
		
	}
	
	

	private void saveOptions() {
		
		Properties properties = new Properties();
		
		properties.setProperty(OPTION_SERVER_ADDRESS, "" + this.getServerAddress());
		properties.setProperty(OPTION_SERVER_PORT, "" + this.getServerPort());
		properties.setProperty(OPTION_DB_FILE, "" + this.getDbFile());
		properties.setProperty(OPTION_DISPLAY_WINDOW, "" + this.isDisplayWindow());
		properties.setProperty(OPTION_DEBUG, "" + this.isDebug());
		properties.setProperty(OPTION_DELAY_AFTER_BROADCAST, "" + this.delayAfterBroadcast);
		properties.setProperty(OPTION_DELAY_AFTER_UNICAST, "" + this.delayAfterUnicast);
		properties.setProperty(OPTION_SERVICE_PORT, "" + this.servicePort);
		
		try (FileOutputStream fout = new FileOutputStream(propertiesFile);) {
			properties.store(fout, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}

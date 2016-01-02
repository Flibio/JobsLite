package me.Flibio.JobsLite.Utils;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;

import com.typesafe.config.ConfigException;

import java.io.File;
import java.io.IOException;


public class FileManager {
	
	//Declare FileType enumerator
	public enum FileType {
		CONFIGURATION,
		PLAYER_DATA,
		JOBS_DATA
	}

	private Logger logger;
	private ConfigurationNode configRoot;
	private ConfigurationNode jobsRoot;
	private ConfigurationNode playerRoot;
	
	public FileManager(Logger logger) {
		this.logger = logger;
	}
	
	public void testDefault(String path, Object value) {
		if(configRoot!=null) {
			//Check if the configuration file doesn't contain the path
			if(configRoot.getNode((Object[]) path.split("\\.")).getValue()==null) {
				//Set the path to the default value
				configRoot.getNode((Object[]) path.split("\\.")).setValue(value);
				saveFile(FileType.CONFIGURATION,configRoot);
			}
		}
	}

	public String getConfigValue(String path) {
		if(configRoot!=null) {
			//Check if the configuration file contains the path
			if(configRoot.getNode((Object[]) path.split("\\.")).getValue()!=null){
				//Get the value and return it
				return configRoot.getNode((Object[]) path.split("\\.")).getString();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	
	public void generateFolder(String path) {
		File folder = new File(path);
		try{
			if(!folder.exists()){
				logger.info(path+" not found, generating...");
				if(folder.mkdir()){
					logger.info("Successfully generated "+path);
				} else {
					logger.warn("Error generating "+path);
				}
				
			}
		} catch(Exception e){
			logger.warn("Error generating "+path+": ");
			logger.warn(e.getMessage());
		}
	}
	
	public void generateFile(String path) {
		File file = new File(path);
		try{
			if(!file.exists()){
				logger.info(path+" not found, generating...");
				try {
	        		file.createNewFile();
	        		logger.info("Successfully generated "+path);
				} catch (IOException e) {
					logger.error("Error generating "+path);
					logger.error(e.getMessage());
				}
			}
		} catch(Exception e){
			logger.warn("Error generating "+path+": ");
			logger.warn(e.getMessage());
		}
	}
	
	public void loadFile(FileType file) {
		String fileName = "";
		switch(file) {
			case CONFIGURATION:
				fileName = "config.conf";
				break;
			
			case PLAYER_DATA:
				fileName = "playerData.conf";
				break;
				
			case JOBS_DATA:
				fileName = "jobsData.conf";
				break;
		}
		
		ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/JobsLite/"+fileName)).build();
		ConfigurationNode root;
		try {
			root = manager.load();
		} catch (IOException e) {
			logger.error("Error loading "+fileName+"!");
			logger.error(e.getMessage());
			return;
		} catch (ConfigException e) {
			logger.error("Error loading "+fileName+"!");
			logger.error("Did you edit something wrong? For a blank value use double quotes.");
			logger.error(e.getMessage());
			return;
		}
		
		switch(file) {
		case CONFIGURATION:
			configRoot = root;
			break;
		
		case PLAYER_DATA:
			playerRoot = root;
			break;
			
		case JOBS_DATA:
			jobsRoot = root;
			break;
		}
	}
	
	public ConfigurationNode getFile(FileType file) {
		switch(file) {
			case CONFIGURATION:
				return configRoot;
			
			case PLAYER_DATA:
				return playerRoot;
				
			case JOBS_DATA:
				return jobsRoot;
				
			default:
				return null;
		}
	}
	
	private void saveFile(FileType file, ConfigurationNode root) {
		switch(file) {
			case CONFIGURATION:
				configRoot = root;
				break;
			
			case PLAYER_DATA:
				playerRoot = root;
				break;
				
			case JOBS_DATA:
				jobsRoot = root;
				break;
		}
	}
	
	public void saveAllFiles() {
		for(FileType type : FileType.values()) {
			String fileName = "";
			ConfigurationNode root = null;
			switch(type) {
				case CONFIGURATION:
					fileName = "config.conf";
					root = configRoot;
					break;
				
				case PLAYER_DATA:
					fileName = "playerData.conf";
					root = playerRoot;
					break;
					
				case JOBS_DATA:
					fileName = "jobsData.conf";
					root = jobsRoot;
					break;
			}
			
			ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/JobsLite/"+fileName)).build();
			
			try {
				manager.save(root);
			} catch (IOException e) {
				logger.error("Error saving "+fileName+"!");
				logger.error(e.getMessage());
			}
		}
	}
	
}

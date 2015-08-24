package me.Flibio.JobsLite.Utils;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.FileManager.FileType;
import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.service.profile.GameProfileResolver;

import com.google.common.base.Optional;

public class PlayerManager {
	
	private FileManager fileManager;
	private JobManager jobManager;
	private Logger logger;
	
	public PlayerManager() {
		fileManager = Main.access.fileManager;
		jobManager = Main.access.jobManager;
		logger = Main.access.logger;
	}
	
	/**
	 * Looks up a player's UUID
	 * @param name
	 * 	Name of the player whom to lookup
	 * @return
	 * 	String of the UUID found(blank string if an error occured)
	 */
	public String getUUID(String name) {
		Optional<GameProfileResolver> resolverOptional = Main.access.game.getServiceManager().provide(GameProfileResolver.class);
		if(resolverOptional.isPresent()) {
			GameProfileResolver resolver = resolverOptional.get();
			GameProfile profile;
			try {
				profile = resolver.get(name).get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error getting player's UUID");
				return "";
			}
			return profile.getUniqueId().toString();
		} else {
			return "";
		}
	}
	
	/**
	 * Adds a player to the JobsLite system
	 * @param uuid
	 * 	UUID of the player to add
	 */
	public void addPlayer(String uuid) {
		if(playerExists(uuid)) return;
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		root.getNode(uuid).getNode("hasJoined").setValue(true);
	}
	
	/**
	 * Checks if a player has their data stored in the JobsLite system
	 * @param uuid
	 * 	The UUID of the player to check
	 * @return
	 * 	Boolean based on if the player was found or not
	 */
	public boolean playerExists(String uuid) {
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);	
		//Check if the uuid is found in the file
		if(root.getChildrenMap().containsKey(uuid)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the current job of a player
	 * @param uuid
	 * 	UUID of the player to get the current job of
	 * @return
	 * 	The current job of the player - empty if none is found
	 */
	public String getCurrentJob(String uuid) {
		if(!playerExists(uuid)) {
			return "";
		} else {
			ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
			ConfigurationNode player = root.getNode(uuid);
			if(player!=null) {
				for(Object raw : player.getChildrenMap().keySet()) {
					if(raw instanceof String) {
						return (String) raw;
					}
				}
			} else {
				return "";
			}
			return "";
		}
	}
	
	/**
	 * Gets the level of the player at their job
	 * @param uuid
	 * 	UUID of the player
	 * @param job
	 * 	Name of the job
	 * @return
	 *	The level of the player - -1 if an error occured
	 */
	public int getCurrentLevel(String uuid, String job) {
		if(!playerExists(uuid)) {
			return -1;
		} else {
			ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
			ConfigurationNode player = root.getNode(uuid);
			if(player!=null) {
				for(Object raw : player.getChildrenMap().keySet()) {
					if(raw instanceof String) {
						String jobName = (String) raw;
						ConfigurationNode level = root.getNode(uuid).getNode(jobName).getNode("level");
						if(level==null) return -1;
						return level.getInt();
					}
				}
			} else {
				return -1;
			}
			return -1;
		}
	}
	
	/**
	 * Gets the exp of the player at their job
	 * @param uuid
	 * 	UUID of the player
	 * @param job
	 * 	Name of the job
	 * @return
	 *	The exp of the player - -1 if an error occured
	 */
	public int getCurrentExp(String uuid, String job) {
		if(!playerExists(uuid)) {
			return -1;
		} else {
			ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
			ConfigurationNode player = root.getNode(uuid);
			if(player!=null) {
				for(Object raw : player.getChildrenMap().keySet()) {
					if(raw instanceof String) {
						String jobName = (String) raw;
						ConfigurationNode exp = root.getNode(uuid).getNode(jobName).getNode("exp");
						if(exp==null) return -1;
						return exp.getInt();
					}
				}
			} else {
				return -1;
			}
			return -1;
		}
	}
	
	/**
	 * Sets the players exp for their job
	 * @param uuid
	 * 	UUID of the player
	 * @param jobName
	 * 	Name of the job
	 * @param exp
	 * 	Amount of exp
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean setExp(String uuid, String jobName, int exp) {
		if(!jobManager.jobExists(jobName)) return false;
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		if(root.getNode(uuid)==null) return false;
		if(root.getNode(uuid).getNode(jobName)==null) return false;
		root.getNode(uuid).getNode(jobName).getNode("exp").setValue(exp);
		
		return true;
	}
	
	/**
	 * Sets the players level for their job
	 * @param uuid
	 * 	UUID of the player
	 * @param jobName
	 * 	Name of the job
	 * @param level
	 * 	Amount of level
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean setLevel(String uuid, String jobName, int level) {
		if(!jobManager.jobExists(jobName)) return false;
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		if(root.getNode(uuid)==null) return false;
		if(root.getNode(uuid).getNode(jobName)==null) return false;
		root.getNode(uuid).getNode(jobName).getNode("level").setValue(level);
		
		return true;
	}
	
	/**
	 * Sets the job of a player
	 * @param uuid
	 * 	UUID of the player whose job to set
	 * @param jobName
	 * 	Name of the job to set the players job to
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean setJob(String uuid, String jobName) {
		if(!jobManager.jobExists(jobName)) return false;
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		if(root.getNode(uuid)==null) return false;
		root.getNode(uuid).setValue(null);
		root.getNode(uuid).getNode(jobName).getNode("level").setValue(0);
		root.getNode(uuid).getNode(jobName).getNode("exp").setValue(0);
		
		return true;
	}
	
	/**
	 * Clears all jobs a player has
	 * @param uuid
	 * 	The UUID of the player to clear the jobs from
	 * @return
	 * 	Boolean based on if the method was a success or not
	 */
	public boolean clearJobs(String uuid) {
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		if(root.getNode(uuid)==null) return false;
		root.getNode(uuid).setValue(null);
		
		return true;
	}
	
	/**
	 * Removes a job from a player
	 * @param uuid
	 * 	UUID of the player to take the job from
	 * @param jobName
	 * 	Name of the job to remove
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean clearJob(String uuid, String jobName) {
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		if(root.getNode(uuid)==null) return false;
		if(root.getNode(uuid).getNode(jobName)==null) return false;
		root.getNode(uuid).getNode(jobName).setValue(null);
		
		return true;
	}
	
	/**
	 * Gets all of the players that are in the JobsLite system
	 * @return
	 * 	String arraylist of all of player's UUIDS
	 */
	public ArrayList<String> getPlayers() {
		ConfigurationNode root = fileManager.getFile(FileType.PLAYER_DATA);
		ArrayList<String> players = new ArrayList<String>();
		for(Object raw : root.getChildrenMap().keySet()) {
			if(raw instanceof String) {
				players.add((String) raw);
			}
		}
		return players;
	}
	
}

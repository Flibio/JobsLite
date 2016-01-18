/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.flibio.jobslite.utils;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.utils.FileManager.FileType;
import ninja.leaping.configurate.ConfigurationNode;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class JobManager {
	
	public enum ActionType {
		BREAK, PLACE
	}
	
	private FileManager fileManager;
	private PlayerManager playerManager;
	
	public JobManager() {
		fileManager = JobsLite.access.fileManager;
		playerManager = JobsLite.access.playerManager;
	}
	
	/**
	 * Checks if a job exists
	 * @param name
	 * 	The name of the job to check
	 * @return
	 * 	Whether or not the job was found
	 */
	public boolean jobExists(String name) {
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		//Check if the job name is in the job file
		for(Object raw : root.getChildrenMap().keySet()) {
			if(raw instanceof String) {
				String jobName = (String) raw;
				
				if(jobName.trim().equalsIgnoreCase(name.trim())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets the display of a job
	 * @param name
	 * 	The name of the job to get the display name of
	 * @return
	 * 	The display name of the job
	 */
	public String getDisplayName(String name) {
		if(!jobExists(name)) {
			return "";
		} else {
			//Load and get the jobs file
			ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
			if(root.getNode(name)!=null) {
				ConfigurationNode displayNameNode = root.getNode(name).getNode("displayName");
				if(displayNameNode!=null) {
					return displayNameNode.getString();
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Gets the equation to calculate how much reward increases per level
	 * @param job
	 * 	The job to get the equation from
	 * @return
	 * 	String of the equation
	 */
	public String getRewardEquation(String job) {
		if(!jobExists(job)) {
			return "";
		} else {
			//Load and get the jobs file
			ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
			if(root.getNode(job)!=null) {
				ConfigurationNode rewardNode = root.getNode(job).getNode("rewardProgressionEquation");
				if(rewardNode!=null) {
					return rewardNode.getString();
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Gets the equation to calculate how much exp required increases per level
	 * @param job
	 * 	The job to get the equation from
	 * @return
	 * 	String of the equation
	 */
	public String getExpRequiredEquation(String job) {
		if(!jobExists(job)) {
			return "";
		} else {
			//Load and get the jobs file
			ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
			if(root.getNode(job)!=null) {
				ConfigurationNode expNode = root.getNode(job).getNode("expRequiredProgressionEquation");
				if(expNode!=null) {
					return expNode.getString();
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Gets the color of a job
	 * @param jobName
	 * 	The name of the job whose color to get
	 * @return
	 * 	The color that was found
	 */
	public TextColor getColor(String jobName) {
		if(!jobExists(jobName)) {
			return TextColors.WHITE;
		} else {
			//Load and get the jobs file
			ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
			if(root.getNode(jobName)!=null) {
				ConfigurationNode colorNode = root.getNode(jobName).getNode("color");
				if(colorNode!=null) {
					String rawColor = colorNode.getString();
					Optional<TextColor> optional = JobsLite.access.game.getRegistry().getType(TextColor.class, rawColor.toUpperCase());
					if(optional.isPresent()) {
						return optional.get();
					} else {
						return TextColors.WHITE;
					}
				} else {
					return TextColors.WHITE;
				}
			} else {
				return TextColors.WHITE;
			}
		}
	}
	
	/**
	 * Creates a new job
	 * @param jobName
	 * 	Name of the job
	 * @param displayName
	 * 	Display name of the job
	 * @param blockBreaks
	 * 	Blocks that people can break and earn currency & xp from
	 * @param blockPlaces
	 * 	Blocks that people can place and earn currency & xp from
	 * @return
	 * 	Boolean based on whether the method was successful or not
	 */
	public boolean newJob(String jobName, String displayName, HashMap<BlockState,HashMap<String,Integer>> blockBreaks, HashMap<BlockState,HashMap<String,Integer>> blockPlaces, int maxLevel, TextColor color) {
		if(jobExists(jobName)) {
			return false;
		} else {
			//Load and get the jobs file
			ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
			root.getNode(jobName).getNode("displayName").setValue(displayName);
			root.getNode(jobName).getNode("color").setValue(color.getName().toUpperCase());
			root.getNode(jobName).getNode("expRequiredProgressionEquation").setValue("100*((1.12)^((currentLevel-1)*1.88))");
			root.getNode(jobName).getNode("rewardProgressionEquation").setValue("startingPoint*((1.048)^(currentLevel-1))");
			root.getNode(jobName).getNode("maxLevel").setValue(maxLevel);
			//Enter break data
			for(BlockState state : blockBreaks.keySet()) {
				HashMap<String,Integer> data = blockBreaks.get(state);
				if(data.containsKey("currency")&&data.containsKey("exp")) {
					int currency = data.get("currency");
					int exp = data.get("exp");
					root.getNode(jobName).getNode("breaks").getNode(state.toString()).getNode("currency").setValue(currency);
					root.getNode(jobName).getNode("breaks").getNode(state.toString()).getNode("exp").setValue(exp);
				}
			}
			//Enter place data
			for(BlockState state : blockPlaces.keySet()) {
				HashMap<String,Integer> data = blockPlaces.get(state);
				if(data.containsKey("currency")&&data.containsKey("exp")) {
					int currency = data.get("currency");
					int exp = data.get("exp");
					root.getNode(jobName).getNode("places").getNode(state.toString()).getNode("currency").setValue(currency);
					root.getNode(jobName).getNode("places").getNode(state.toString()).getNode("exp").setValue(exp);
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Gets all of the blocks that will give you rewards for breaking them
	 * @param jobName
	 * 	The job to check
	 * @return
	 * 	String array list of the blocks
	 */
	public ArrayList<String> getBreakBlocks(String jobName) {
		ArrayList<String> blocks = new ArrayList<String>();
		if(!jobExists(jobName)) return blocks;
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		ConfigurationNode breaksNode = root.getNode(jobName).getNode("breaks");
		if(breaksNode==null) return blocks;
		for(Object raw : breaksNode.getChildrenMap().keySet()) {
			if(raw instanceof String) {
				blocks.add((String) raw);
			}
		}
		return blocks;
	}
	
	/**
	 * Gets all of the blocks that will give you rewards for placing them
	 * @param jobName
	 * 	The job to check
	 * @return
	 * 	String array list of the blocks
	 */
	public ArrayList<String> getPlaceBlocks(String jobName) {
		ArrayList<String> blocks = new ArrayList<String>();
		if(!jobExists(jobName)) return blocks;
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		ConfigurationNode placesNode = root.getNode(jobName).getNode("places");
		if(placesNode==null) return blocks;
		for(Object raw : placesNode.getChildrenMap().keySet()) {
			if(raw instanceof String) {
				blocks.add((String) raw);
			}
		}
		return blocks;
	}
	
	/**
	 * Gets all of the registered jobs
	 * @return
	 * 	An arraylist of all of the registered jobs
	 */
	public ArrayList<String> getJobs() {
		ArrayList<String> jobs = new ArrayList<String>();
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		for(Object raw : root.getChildrenMap().keySet()) {
			if(raw instanceof String) {
				jobs.add((String)raw);
			}
		}
		return jobs;
	}
	
	/**
	 * Gets the currency reward for a specific job and block
	 * @param jobName
	 * 	The job to check
	 * @param block
	 * 	The block to check
	 * @return
	 * 	The amount of currency the user is rewarded
	 */
	public int getCurrencyReward(String jobName, String block, ActionType action) {
		String path = "";
		switch(action) {
			case BREAK:
				path = "breaks";
				break;
			case PLACE:
				path = "places";
				break;
		}
		if(!jobExists(jobName)) return -1;
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		ConfigurationNode node = root.getNode(jobName).getNode(path);
		if(node==null) return -1;
		ConfigurationNode blockNode = node.getNode(block);
		if(blockNode==null) return -1;
		ConfigurationNode currencyNode = blockNode.getNode("currency");
		if(currencyNode==null) return -1;
		return currencyNode.getInt();
	}
	
	/**
	 * Gets the exp reward for a specific job and block
	 * @param jobName
	 * 	The job to check
	 * @param block
	 * 	The block to check
	 * @return
	 * 	The amount of experience the user is rewarded
	 */
	public int getExpReward(String jobName, String block, ActionType action) {
		String path = "";
		switch(action) {
			case BREAK:
				path = "breaks";
				break;
			case PLACE:
				path = "places";
				break;
		}
		if(!jobExists(jobName)) return -1;
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		ConfigurationNode breaksNode = root.getNode(jobName).getNode(path);
		if(breaksNode==null) return -1;
		ConfigurationNode blockNode = breaksNode.getNode(block);
		if(blockNode==null) return -1;
		ConfigurationNode expNode = blockNode.getNode("exp");
		if(expNode==null) return -1;
		return expNode.getInt();
	}
	
	/**
	 * Gets the maximum level for a job
	 * @param jobName
	 * 	The job to get the max level of
	 * @return
	 * 	The max level of the job
	 */
	public int getMaxLevel(String jobName) {
		if(!jobExists(jobName)) return -1;
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		ConfigurationNode maxNode = root.getNode(jobName).getNode("maxLevel");
		if(maxNode==null) return -1;
		return maxNode.getInt();
	}
	
	/**
	 * Deletes a job from JobsLite
	 * @param jobName
	 * 	The name of the job to delete
	 * @return
	 * 	Boolean based on if the method was sucessful or not
	 */
	public boolean deleteJob(String jobName) {
		ConfigurationNode root = fileManager.getFile(FileType.JOBS_DATA);
		if(root.getNode(jobName)==null) return false;
		root.getNode(jobName).setValue(null);
		for(String uuid : playerManager.getPlayers()) {
			playerManager.clearJob(uuid, jobName);
		}
		
		return true;
	}
	
	
}

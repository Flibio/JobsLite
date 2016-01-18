/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
 * Copyright (c) Contributors
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
import me.flibio.jobslite.data.JobData;
import me.flibio.jobslite.data.JobDataManipulatorBuilder;
import me.flibio.jobslite.data.JobInfo;
import me.flibio.jobslite.data.Keys;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;

import java.util.concurrent.ExecutionException;

public class PlayerManager {

	private JobManager jobManager;
	private Logger logger;
	
	public PlayerManager(Logger logger) {
		jobManager = JobsLite.access.jobManager;
		this.logger = logger;
	}
	
	/**
	 * Looks up a player's UUID
	 * @param name
	 * 	Name of the player whom to lookup
	 * @return
	 * 	String of the UUID found(blank string if an error occured)
	 */
	public String getUUID(String name) {
		GameProfileManager manager = JobsLite.access.game.getServer().getGameProfileManager();
		GameProfile profile;
		try {
			profile = manager.get(name).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error getting player's UUID");
			return "";
		}
		return profile.getUniqueId().toString();
	}
	
	/**
	 * Adds a player to the JobsLite system
	 * @param player
	 * 	The player to add
	 */
	public void addPlayer(User player) {
		if(playerExists(player)) return;
		JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
		JobData data = builder.setJobInfo(new JobInfo("",0,0)).create();
		player.offer(data);
	}
	
	/**
	 * Checks if a player has their data stored in the JobsLite system
	 * @param player
	 * 	User to check
	 * @return
	 * 	Boolean based on if the player was found or not
	 */
	public boolean playerExists(User player) {
		return player.get(Keys.JOB).isPresent();
	}
	
	/**
	 * Gets the current job of a player
	 * @param player
	 * 	The player to get the current job of
	 * @return
	 * 	The current job of the player - empty if none is found
	 */
	public String getCurrentJob(User player) {
		if(!playerExists(player)) {
			return "";
		} else {
			return player.get(Keys.JOB).get().getJobName();
		}
	}
	
	/**
	 * Gets the level of the player at their job
	 * @param player
	 * 	The player
	 * @param job
	 * 	Name of the job
	 * @return
	 *	The level of the player - -1 if an error occured
	 */
	public int getCurrentLevel(User player, String job) {
		if(!playerExists(player)) {
			return -1;
		} else {
			return player.get(Keys.JOB).get().getLevel();
		}
	}
	
	/**
	 * Gets the exp of the player at their job
	 * @param player
	 * 	The player
	 * @param job
	 * 	Name of the job
	 * @return
	 *	The exp of the player - -1 if an error occured
	 */
	public int getCurrentExp(User player, String job) {
		if(!playerExists(player)) {
			return -1;
		} else {
			return player.get(Keys.JOB).get().getExp();
		}
	}
	
	/**
	 * Sets the players exp for their job
	 * @param player
	 * 	The player
	 * @param jobName
	 * 	Name of the job
	 * @param exp
	 * 	Amount of exp
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean setExp(User player, String jobName, int exp) {
		if(!jobManager.jobExists(jobName)) return false;
		if(!playerExists(player)) return false;
		JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
		JobData data = builder.setJobInfo(new JobInfo(jobName,player.get(Keys.JOB).get().getLevel(),exp)).create();
		return player.offer(data).isSuccessful();
	}
	
	/**
	 * Sets the players level for their job
	 * @param player
	 * 	The player
	 * @param jobName
	 * 	Name of the job
	 * @param level
	 * 	Amount of level
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean setLevel(User player, String jobName, int level) {
		if(!jobManager.jobExists(jobName)) return false;
		if(!playerExists(player)) return false;
		JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
		JobData data = builder.setJobInfo(new JobInfo(jobName,level,player.get(Keys.JOB).get().getExp())).create();
		return player.offer(data).isSuccessful();
	}
	
	/**
	 * Sets the job of a player
	 * @param player
	 * 	The player whose job to set
	 * @param jobName
	 * 	Name of the job to set the players job to
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean setJob(User player, String jobName) {
		if(!jobManager.jobExists(jobName)) return false;
		if(!playerExists(player)) return false;
		JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
		JobData data = builder.setJobInfo(new JobInfo(jobName,0,0)).create();
		return player.offer(data).isSuccessful();
	}
	
	/**
	 * Clears all jobs a player has
	 * @param player
	 * 	The player to clear the jobs from
	 * @return
	 * 	Boolean based on if the method was a success or not
	 */
	public boolean clearJobs(User player) {
		JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
		JobData data = builder.setJobInfo(new JobInfo("",0,0)).create();
		return player.offer(data).isSuccessful();
	}
	
	/**
	 * Removes a job from a player
	 * @param player
	 * 	The player to remove the job from
	 * @param jobName
	 * 	Name of the job to remove
	 * @return
	 * 	Boolean based on if the method was successful or not
	 */
	public boolean clearJob(User player, String jobName) {
		JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
		JobData data = builder.setJobInfo(new JobInfo("",0,0)).create();
		return player.offer(data).isSuccessful();
	}
	
}

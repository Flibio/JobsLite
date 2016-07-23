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

import io.github.flibio.utils.file.ConfigManager;
import me.flibio.jobslite.JobsLite;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public class PlayerManager {

    private ConfigManager fileManager;
    private JobManager jobManager;

    public PlayerManager() {
        jobManager = JobsLite.access.jobManager;
        fileManager = JobsLite.access.configManager;
    }

    /**
     * Adds a player to the JobsLite system
     * 
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        if (playerExists(player))
            return;
        fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".exp", Double.class, 0.0);
        fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".level", Integer.class, 0);
        fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".job", String.class, "");
    }

    /**
     * Checks if a player has their data stored in the JobsLite system
     * 
     * @param player User to check
     * @return Boolean based on if the player was found or not
     */
    public boolean playerExists(Player player) {
        return fileManager.nodeExists("playerjobdata.conf", player.getUniqueId().toString());
    }

    /**
     * Gets the current job of a player
     * 
     * @param player The player to get the current job of
     * @return The current job of the player - empty if none is found
     */
    public String getCurrentJob(Player player) {
        if (!playerExists(player)) {
            return "";
        } else {
            return fileManager.getValue("playerjobdata.conf", player.getUniqueId().toString() + ".job", String.class).get();
        }
    }

    /**
     * Gets the level of the player at their job
     * 
     * @param player The player
     * @param job Name of the job
     * @return The level of the player - -1 if an error occured
     */
    public int getCurrentLevel(Player player, String job) {
        if (!playerExists(player)) {
            return -1;
        } else {
            return fileManager.getValue("playerjobdata.conf", player.getUniqueId().toString() + ".level", Integer.class).get();
        }
    }

    /**
     * Gets the exp of the player at their job
     * 
     * @param player The player
     * @param job Name of the job
     * @return The exp of the player - -1 if an error occured
     */
    public Optional<Double> getCurrentExp(Player player, String job) {
        if (!playerExists(player)) {
            return Optional.empty();
        } else {
            return Optional.of(fileManager.getValue("playerjobdata.conf", player.getUniqueId().toString() + ".exp", Double.class).get());
        }
    }

    /**
     * Sets the players exp for their job
     * 
     * @param player The player
     * @param jobName Name of the job
     * @param exp Amount of exp
     * @return Boolean based on if the method was successful or not
     */
    public boolean setExp(Player player, String jobName, double exp) {
        if (!jobManager.jobExists(jobName))
            return false;
        if (!playerExists(player))
            return false;
        return fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".exp", Double.class, exp);
    }

    /**
     * Sets the players level for their job
     * 
     * @param player The player
     * @param jobName Name of the job
     * @param level Amount of level
     * @return Boolean based on if the method was successful or not
     */
    public boolean setLevel(Player player, String jobName, int level) {
        if (!jobManager.jobExists(jobName))
            return false;
        if (!playerExists(player))
            return false;
        return fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".level", Integer.class, level);
    }

    /**
     * Sets the job of a player
     * 
     * @param player The player whose job to set
     * @param jobName Name of the job to set the players job to
     * @return Boolean based on if the method was successful or not
     */
    public boolean setJob(Player player, String jobName) {
        if (!jobManager.jobExists(jobName))
            return false;
        if (!playerExists(player))
            return false;
        return fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".job", String.class, jobName);
    }

    /**
     * Clears all jobs a player has
     * 
     * @param player The player to clear the jobs from
     * @return Boolean based on if the method was a success or not
     */
    public boolean clearJobs(Player player) {
        fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".exp", Double.class, 0.0);
        fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".level", Integer.class, 0);
        return fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".job", String.class, "");
    }

    /**
     * Removes a job from a player
     * 
     * @param player The player to remove the job from
     * @param jobName Name of the job to remove
     * @return Boolean based on if the method was successful or not
     */
    public boolean clearJob(Player player, String jobName) {
        if (getCurrentJob(player).equalsIgnoreCase(jobName)) {
            fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".exp", Double.class, 0.0);
            fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".level", Integer.class, 0);
            return fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".job", String.class, "");
        }
        return false;
    }

    public void markAsMoved(Player player) {
        fileManager.setValue("playerjobdata.conf", player.getUniqueId().toString() + ".moved", Boolean.class, true);
    }

    public boolean hasMoved(Player player) {
        Optional<Boolean> mOpt = fileManager.getValue("playerjobdata.conf", player.getUniqueId().toString() + ".moved", Boolean.class);
        if (mOpt.isPresent()) {
            return mOpt.get();
        }
        return false;
    }

}

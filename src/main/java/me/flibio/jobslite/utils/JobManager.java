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

    private ConfigManager fileManager;

    public JobManager() {
        fileManager = JobsLite.access.configManager;
    }

    /**
     * Checks if a job exists
     * 
     * @param name The name of the job to check
     * @return Whether or not the job was found
     */
    public boolean jobExists(String name) {
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return false;
        ConfigurationNode root = rOpt.get();
        // Check if the job name is in the job file
        for (Object raw : root.getChildrenMap().keySet()) {
            if (raw instanceof String) {
                String jobName = (String) raw;

                if (jobName.trim().equalsIgnoreCase(name.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the display of a job
     * 
     * @param name The name of the job to get the display name of
     * @return The display name of the job
     */
    public String getDisplayName(String name) {
        if (!jobExists(name)) {
            return "";
        } else {
            // Load and get the jobs file
            Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
            if (!rOpt.isPresent())
                return "";
            ConfigurationNode root = rOpt.get();
            if (root.getNode(name) != null) {
                ConfigurationNode displayNameNode = root.getNode(name).getNode("displayName");
                if (displayNameNode != null) {
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
     * 
     * @param job The job to get the equation from
     * @return String of the equation
     */
    public String getRewardEquation(String job) {
        if (!jobExists(job)) {
            return "";
        } else {
            // Load and get the jobs file
            Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
            if (!rOpt.isPresent())
                return "";
            ConfigurationNode root = rOpt.get();
            if (root.getNode(job) != null) {
                ConfigurationNode rewardNode = root.getNode(job).getNode("rewardProgressionEquation");
                if (rewardNode != null) {
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
     * 
     * @param job The job to get the equation from
     * @return String of the equation
     */
    public String getExpRequiredEquation(String job) {
        if (!jobExists(job)) {
            return "";
        } else {
            // Load and get the jobs file
            Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
            if (!rOpt.isPresent())
                return "";
            ConfigurationNode root = rOpt.get();
            if (root.getNode(job) != null) {
                ConfigurationNode expNode = root.getNode(job).getNode("expRequiredProgressionEquation");
                if (expNode != null) {
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
     * 
     * @param jobName The name of the job whose color to get
     * @return The color that was found
     */
    public TextColor getColor(String jobName) {
        if (!jobExists(jobName)) {
            return TextColors.WHITE;
        } else {
            // Load and get the jobs file
            Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
            if (!rOpt.isPresent())
                return TextColors.WHITE;
            ConfigurationNode root = rOpt.get();
            if (root.getNode(jobName) != null) {
                ConfigurationNode colorNode = root.getNode(jobName).getNode("color");
                if (colorNode != null) {
                    String rawColor = colorNode.getString();
                    Optional<TextColor> optional = JobsLite.access.game.getRegistry().getType(TextColor.class, rawColor.toUpperCase());
                    if (optional.isPresent()) {
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
     * 
     * @param jobName Name of the job
     * @param displayName Display name of the job
     * @param blockBreaks Blocks that people can break and earn currency & xp
     *        from
     * @param blockPlaces Blocks that people can place and earn currency & xp
     *        from
     * @return Boolean based on whether the method was successful or not
     */
    public boolean newJob(String jobName, String displayName, HashMap<BlockState, HashMap<String, Double>> blockBreaks,
            HashMap<BlockState, HashMap<String, Double>> blockPlaces, int maxLevel, TextColor color, boolean silkTouch, boolean worldGen) {
        if (jobExists(jobName)) {
            return false;
        } else {
            // Load and get the jobs file
            Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
            if (!rOpt.isPresent())
                return false;
            ConfigurationNode root = rOpt.get();
            root.getNode(jobName).getNode("displayName").setValue(displayName);
            root.getNode(jobName).getNode("color").setValue(color.getName().toUpperCase());
            root.getNode(jobName).getNode("expRequiredProgressionEquation").setValue("100*((1.12)^((currentLevel-1)*1.88))");
            root.getNode(jobName).getNode("rewardProgressionEquation").setValue("startingPoint*((1.048)^(currentLevel-1))");
            root.getNode(jobName).getNode("maxLevel").setValue(maxLevel);
            root.getNode(jobName).getNode("silkTouch").setValue(silkTouch);
            root.getNode(jobName).getNode("worldGen").setValue(worldGen);
            // Enter break data
            for (BlockState state : blockBreaks.keySet()) {
                HashMap<String, Double> data = blockBreaks.get(state);
                if (data.containsKey("currency") && data.containsKey("exp")) {
                    double currency = data.get("currency");
                    double exp = data.get("exp");
                    root.getNode(jobName).getNode("breaks").getNode(state.toString()).getNode("currency").setValue(currency);
                    root.getNode(jobName).getNode("breaks").getNode(state.toString()).getNode("exp").setValue(exp);
                }
            }
            // Enter place data
            for (BlockState state : blockPlaces.keySet()) {
                HashMap<String, Double> data = blockPlaces.get(state);
                if (data.containsKey("currency") && data.containsKey("exp")) {
                    double currency = data.get("currency");
                    double exp = data.get("exp");
                    root.getNode(jobName).getNode("places").getNode(state.toString()).getNode("currency").setValue(currency);
                    root.getNode(jobName).getNode("places").getNode(state.toString()).getNode("exp").setValue(exp);
                }
            }
            fileManager.saveFile("jobsData.conf", root);
            return true;
        }
    }

    /**
     * Gets all of the blocks that will give you rewards for breaking them
     * 
     * @param jobName The job to check
     * @return String array list of the blocks
     */
    public ArrayList<String> getBreakBlocks(String jobName) {
        ArrayList<String> blocks = new ArrayList<String>();
        if (!jobExists(jobName))
            return blocks;
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return blocks;
        ConfigurationNode root = rOpt.get();
        ConfigurationNode breaksNode = root.getNode(jobName).getNode("breaks");
        if (breaksNode == null)
            return blocks;
        for (Object raw : breaksNode.getChildrenMap().keySet()) {
            if (raw instanceof String) {
                blocks.add((String) raw);
            }
        }
        return blocks;
    }

    /**
     * Gets all of the blocks that will give you rewards for placing them
     * 
     * @param jobName The job to check
     * @return String array list of the blocks
     */
    public ArrayList<String> getPlaceBlocks(String jobName) {
        ArrayList<String> blocks = new ArrayList<String>();
        if (!jobExists(jobName))
            return blocks;
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return blocks;
        ConfigurationNode root = rOpt.get();
        ConfigurationNode placesNode = root.getNode(jobName).getNode("places");
        if (placesNode == null)
            return blocks;
        for (Object raw : placesNode.getChildrenMap().keySet()) {
            if (raw instanceof String) {
                blocks.add((String) raw);
            }
        }
        return blocks;
    }

    /**
     * Gets all of the registered jobs
     * 
     * @return An arraylist of all of the registered jobs
     */
    public ArrayList<String> getJobs() {
        ArrayList<String> jobs = new ArrayList<String>();
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return jobs;
        ConfigurationNode root = rOpt.get();
        for (Object raw : root.getChildrenMap().keySet()) {
            if (raw instanceof String) {
                jobs.add((String) raw);
            }
        }
        return jobs;
    }

    /**
     * Gets the currency reward for a specific job and block
     * 
     * @param jobName The job to check
     * @param block The block to check
     * @return The amount of currency the user is rewarded
     */
    public Optional<Double> getCurrencyReward(String jobName, String block, ActionType action) {
        String path = "";
        switch (action) {
            case BREAK:
                path = "breaks";
                break;
            case PLACE:
                path = "places";
                break;
        }
        if (!jobExists(jobName))
            return Optional.empty();
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return Optional.empty();
        ConfigurationNode root = rOpt.get();
        ConfigurationNode node = root.getNode(jobName).getNode(path);
        if (node == null)
            return Optional.empty();
        ConfigurationNode blockNode = node.getNode(block);
        if (blockNode == null)
            return Optional.empty();
        ConfigurationNode currencyNode = blockNode.getNode("currency");
        if (currencyNode == null)
            return Optional.empty();
        return Optional.of(currencyNode.getDouble());
    }

    /**
     * Gets the exp reward for a specific job and block
     * 
     * @param jobName The job to check
     * @param block The block to check
     * @return The amount of experience the user is rewarded
     */
    public Optional<Double> getExpReward(String jobName, String block, ActionType action) {
        String path = "";
        switch (action) {
            case BREAK:
                path = "breaks";
                break;
            case PLACE:
                path = "places";
                break;
        }
        if (!jobExists(jobName))
            return Optional.empty();
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return Optional.empty();
        ConfigurationNode root = rOpt.get();
        ConfigurationNode breaksNode = root.getNode(jobName).getNode(path);
        if (breaksNode == null)
            return Optional.empty();
        ConfigurationNode blockNode = breaksNode.getNode(block);
        if (blockNode == null)
            return Optional.empty();
        ConfigurationNode expNode = blockNode.getNode("exp");
        if (expNode == null)
            return Optional.empty();
        return Optional.of(expNode.getDouble());
    }

    /**
     * Gets the maximum level for a job
     * 
     * @param jobName The job to get the max level of
     * @return The max level of the job
     */
    public int getMaxLevel(String jobName) {
        if (!jobExists(jobName))
            return -1;
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return -1;
        ConfigurationNode root = rOpt.get();
        ConfigurationNode maxNode = root.getNode(jobName).getNode("maxLevel");
        if (maxNode == null)
            return -1;
        return maxNode.getInt();
    }

    /**
     * Gets if the job disallows silk touch tools.
     * 
     * @param jobName The name of the job.
     * @return If the job disallows silk touch tools.
     */
    public boolean onlySilkTouch(String jobName) {
        if (!jobExists(jobName))
            return false;
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return false;
        ConfigurationNode root = rOpt.get();
        ConfigurationNode silkTouch = root.getNode(jobName).getNode("silkTouch");
        if (silkTouch == null)
            return false;
        return silkTouch.getBoolean();
    }

    /**
     * Gets if the job only allows world generated blocks.
     * 
     * @param jobName The name of the job.
     * @return If the job disallows silk touch tools.
     */
    public boolean onlyWorldGen(String jobName) {
        if (!jobExists(jobName))
            return false;
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return false;
        ConfigurationNode root = rOpt.get();
        ConfigurationNode worldGen = root.getNode(jobName).getNode("worldGen");
        if (worldGen == null)
            return false;
        return worldGen.getBoolean();
    }

    /**
     * Deletes a job from JobsLite
     * 
     * @param jobName The name of the job to delete
     * @return Boolean based on if the method was sucessful or not
     */
    public boolean deleteJob(String jobName) {
        Optional<ConfigurationNode> rOpt = fileManager.getFile("jobsData.conf");
        if (!rOpt.isPresent())
            return false;
        ConfigurationNode root = rOpt.get();
        if (root.getNode(jobName) == null)
            return false;
        root.getNode(jobName).setValue(null);
        fileManager.saveFile("jobsData.confg", root);
        return true;
    }

}

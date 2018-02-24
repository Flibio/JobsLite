/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2018 Flibio
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
package me.flibio.jobslite.impl;

import io.github.flibio.utils.file.FileManager;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.Job;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.format.TextColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LocalJobManager implements JobManager {

    private FileManager fileManager = JobsLite.getFileManager();

    @Override
    public boolean jobExists(String job) {
        return fileManager.nodeExists("jobsData.conf", job);
    }

    @Override
    public Optional<Job> getJob(String job) {
        if (jobExists(job)) {
            Optional<String> dOpt = fileManager.getValue("jobsData.conf", job + ".displayName", String.class);
            Optional<Integer> mOpt = fileManager.getValue("jobsData.conf", job + ".maxLevel", Integer.class);
            Optional<String> cOpt = fileManager.getValue("jobsData.conf", job + ".color", String.class);
            Optional<Boolean> sOpt = fileManager.getValue("jobsData.conf", job + ".silkTouch", Boolean.class);
            Optional<Boolean> wOpt = fileManager.getValue("jobsData.conf", job + ".worldGen", Boolean.class);
            Optional<Boolean> iOpt = fileManager.getValue("jobsData.conf", job + ".ignoreData", Boolean.class);
            Optional<String> eQOpt = fileManager.getValue("jobsData.conf", job + ".expRequiredProgressionEquation", String.class);
            Optional<String> cQOpt = fileManager.getValue("jobsData.conf", job + ".rewardProgressionEquation", String.class);
            boolean ignoreData = iOpt.isPresent() ? iOpt.get() : false;
            boolean worldGen = wOpt.isPresent() ? wOpt.get() : false;
            boolean silkTouch = sOpt.isPresent() ? sOpt.get() : false;
            if (dOpt.isPresent() && mOpt.isPresent() && cOpt.isPresent() && eQOpt.isPresent() && cQOpt.isPresent()) {
                Map<String, Reward> blockBreaks = new HashMap<>();
                Map<String, Reward> blockPlaces = new HashMap<>();
                Map<String, Reward> mobKills = new HashMap<>();
                Optional<ConfigurationNode> fOpt = fileManager.getFile("jobsData.conf");
                if (fOpt.isPresent()) {
                    // Block breaks
                    fOpt.get().getNode(job).getNode("breaks").getChildrenMap().keySet().forEach(raw -> {
                        String id = raw.toString();
                        Optional<Double> curOpt = fileManager.getValue("jobsData.conf", job + ".breaks." + id + ".currency", Double.class);
                        Optional<Double> expOpt = fileManager.getValue("jobsData.conf", job + ".breaks." + id + ".exp", Double.class);
                        if (curOpt.isPresent() && expOpt.isPresent()) {
                            blockBreaks.put(id, new Reward(curOpt.get(), expOpt.get()));
                        }
                    });
                    // Block places
                    fOpt.get().getNode(job).getNode("places").getChildrenMap().keySet().forEach(raw -> {
                        String id = raw.toString();
                        Optional<Double> curOpt = fileManager.getValue("jobsData.conf", job + ".places." + id + ".currency", Double.class);
                        Optional<Double> expOpt = fileManager.getValue("jobsData.conf", job + ".places." + id + ".exp", Double.class);
                        if (curOpt.isPresent() && expOpt.isPresent()) {
                            blockPlaces.put(id, new Reward(curOpt.get(), expOpt.get()));
                        }
                    });
                    // Mob kills
                    fOpt.get().getNode(job).getNode("kills").getChildrenMap().keySet().forEach(raw -> {
                        String id = raw.toString();
                        Optional<Double> curOpt = fileManager.getValue("jobsData.conf", job + ".kills." + id + ".currency", Double.class);
                        Optional<Double> expOpt = fileManager.getValue("jobsData.conf", job + ".kills." + id + ".exp", Double.class);
                        if (curOpt.isPresent() && expOpt.isPresent()) {
                            mobKills.put(id, new Reward(curOpt.get(), expOpt.get()));
                        }
                    });
                    // Finalize
                    String color = cOpt.get();
                    Optional<TextColor> tOpt = Sponge.getRegistry().getType(TextColor.class, color);
                    if (tOpt.isPresent()) {
                        return Optional.of(new Job(job, dOpt.get(), mOpt.get(), tOpt.get(), silkTouch, worldGen, ignoreData, eQOpt.get(),
                                cQOpt.get(), blockBreaks, blockPlaces, mobKills));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Job> getJobs() {
        List<Job> jobs = new ArrayList<>();
        Optional<ConfigurationNode> fOpt = fileManager.getFile("jobsData.conf");
        if (fOpt.isPresent()) {
            fOpt.get().getChildrenMap().keySet().forEach(raw -> {
                Optional<Job> jOpt = getJob(raw.toString());
                if (jOpt.isPresent()) {
                    jobs.add(jOpt.get());
                }
            });
        }
        return jobs;
    }

    @Override
    public boolean createJob(String id, String displayName, int maxLevel, TextColor textColor, boolean silkTouch, boolean worldGen,
            boolean ignoreData, HashMap<BlockState, Reward> blockBreaks, HashMap<BlockState, Reward> blockPlaces,
            HashMap<EntityType, Reward> mobKills) {
        if (!jobExists(id)) {
            fileManager.setValue("jobsData.conf", id + ".displayName", String.class, displayName);
            fileManager.setValue("jobsData.conf", id + ".maxLevel", Integer.class, maxLevel);
            fileManager.setValue("jobsData.conf", id + ".color", String.class, textColor.toString());
            fileManager.setValue("jobsData.conf", id + ".silkTouch", Boolean.class, silkTouch);
            fileManager.setValue("jobsData.conf", id + ".worldGen", Boolean.class, worldGen);
            fileManager.setValue("jobsData.conf", id + ".ignoreData", Boolean.class, ignoreData);
            fileManager.setValue("jobsData.conf", id + ".expRequiredProgressionEquation", String.class, "100*((1.12)^((currentLevel-1)*1.88))");
            fileManager.setValue("jobsData.conf", id + ".rewardProgressionEquation", String.class, "startingPoint*((1.048)^(currentLevel-1))");
            blockBreaks.entrySet().forEach(entry -> {
                String key = entry.getKey().toString();
                Reward reward = entry.getValue();
                fileManager.setValue("jobsData.conf", id + ".breaks." + key + ".currency", Double.class, reward.getCurrency());
                fileManager.setValue("jobsData.conf", id + ".breaks." + key + ".exp", Double.class, reward.getExp());
            });
            blockPlaces.entrySet().forEach(entry -> {
                String key = entry.getKey().toString();
                Reward reward = entry.getValue();
                fileManager.setValue("jobsData.conf", id + ".places." + key + ".currency", Double.class, reward.getCurrency());
                fileManager.setValue("jobsData.conf", id + ".places." + key + ".exp", Double.class, reward.getExp());
            });
            mobKills.entrySet().forEach(entry -> {
                String key = entry.getKey().getId();
                Reward reward = entry.getValue();
                fileManager.setValue("jobsData.conf", id + ".kills." + key + ".currency", Double.class, reward.getCurrency());
                fileManager.setValue("jobsData.conf", id + ".kills." + key + ".exp", Double.class, reward.getExp());
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteJob(String job) {
        boolean del = fileManager.deleteValue("jobsData.conf", job);
        boolean player = JobsLite.getPlayerManager().removeJob(job);
        return del && player;
    }

}

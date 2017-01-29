/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2017 Flibio
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
import me.flibio.jobslite.api.PlayerManager;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LocalPlayerManager implements PlayerManager {

    private FileManager fileManager = JobsLite.getFileManager();

    @Override
    public List<String> getCurrentJobs(UUID uuid) {
        List<String> jobs = new ArrayList<>();
        Optional<ConfigurationNode> fOpt = fileManager.getFile("playerjobs.conf");
        if (fOpt.isPresent() && !fOpt.get().getNode(uuid.toString()).isVirtual()) {
            fOpt.get().getNode(uuid.toString()).getChildrenMap().keySet().forEach(raw -> {
                jobs.add(raw.toString());
            });
        }
        return jobs;
    }

    @Override
    public int getCurrentLevel(UUID uuid, String job) {
        if (hasJob(uuid, job)) {
            Optional<Integer> iOpt = fileManager.getValue("playerjobs.conf", uuid.toString() + "." + job + ".level", Integer.class);
            if (iOpt.isPresent()) {
                return iOpt.get();
            }
        }
        return -1;
    }

    @Override
    public Optional<Double> getCurrentExp(UUID uuid, String job) {
        if (hasJob(uuid, job)) {
            return fileManager.getValue("playerjobs.conf", uuid.toString() + "." + job + ".exp", Double.class);
        }
        return Optional.empty();
    }

    @Override
    public boolean setExp(UUID uuid, String job, double exp) {
        return fileManager.setValue("playerjobs.conf", uuid.toString() + "." + job + ".exp", Double.class, exp);
    }

    @Override
    public boolean setLevel(UUID uuid, String job, int level) {
        return fileManager.setValue("playerjobs.conf", uuid.toString() + "." + job + ".level", Integer.class, level);
    }

    @Override
    public boolean addJob(UUID uuid, String job) {
        return fileManager.setValue("playerjobs.conf", uuid.toString() + "." + job + ".exp", Double.class, 0.0)
                && fileManager.setValue("playerjobs.conf", uuid.toString() + "." + job + ".level", Integer.class, 0);
    }

    @Override
    public boolean removeAllJobs(UUID uuid) {
        return fileManager.deleteValue("playerjobs.conf", uuid.toString());
    }

    @Override
    public boolean removeJob(UUID uuid, String job) {
        return fileManager.deleteValue("playerjobs.conf", uuid.toString() + "." + job);
    }

    @Override
    public boolean removeJob(String job) {
        Optional<ConfigurationNode> fOpt = fileManager.getFile("playerjobs.conf");
        if (fOpt.isPresent()) {
            ConfigurationNode node = fOpt.get();
            node.getChildrenMap().keySet().forEach(raw -> {
                if (raw instanceof String) {
                    String uuid = (String) raw;
                    fileManager.deleteValue("playerjobs.conf", uuid + "." + job);
                }
            });
        }
        return false;
    }

}

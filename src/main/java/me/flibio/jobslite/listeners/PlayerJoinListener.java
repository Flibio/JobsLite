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
package me.flibio.jobslite.listeners;

import io.github.flibio.utils.file.FileManager;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.PlayerManager;
import me.flibio.jobslite.data.LiteKeys;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;
import java.util.UUID;

public class PlayerJoinListener {

    private PlayerManager playerManager = JobsLite.getPlayerManager();
    private JobManager jobManager = JobsLite.getJobManager();

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        UUID uuid = player.getUniqueId();
        // Move very old data
        moveData(player);
        // Move slightly old data
        convert(uuid);
        playerManager.getCurrentJobs(uuid).forEach(job -> {
            if (!jobManager.jobExists(job)) {
                playerManager.removeJob(uuid, job);
            }
        });
        JobsLite.getEconomyService().getOrCreateAccount(player.getUniqueId());
    }

    private void moveData(Player player) {
        if (player.get(LiteKeys.JOB_NAME).isPresent()) {
            String jobName = player.get(LiteKeys.JOB_NAME).get();
            if (jobManager.jobExists(jobName)) {
                UUID uuid = player.getUniqueId();
                // Transfer the job
                playerManager.addJob(uuid, jobName);
                playerManager.setExp(uuid, jobName, (double) player.get(LiteKeys.EXP).get());
                playerManager.setLevel(uuid, jobName, player.get(LiteKeys.LEVEL).get());
                // Remove the data
                // player.remove(JobData.class);
            } else {
                // Remove the data
                // player.remove(JobData.class);
            }
        }
    }

    private void convert(UUID uuid) {
        FileManager fileManager = JobsLite.getFileManager();
        Optional<ConfigurationNode> oOpt = fileManager.getFile("playerjobdata.conf");
        Optional<ConfigurationNode> nOpt = fileManager.getFile("playerjobs.conf");
        if (oOpt.isPresent() && nOpt.isPresent()) {
            ConfigurationNode old = oOpt.get();
            old.getChildrenMap().keySet().forEach(raw -> {
                String data = raw.toString();
                Optional<Double> eOpt = fileManager.getValue("playerjobdata.conf", data + ".exp", Double.class);
                Optional<Integer> lOpt = fileManager.getValue("playerjobdata.conf", data + ".level", Integer.class);
                Optional<String> jOpt = fileManager.getValue("playerjobdata.conf", data + ".job", String.class);
                if (eOpt.isPresent() && lOpt.isPresent() && jOpt.isPresent()) {
                    playerManager.addJob(uuid, jOpt.get());
                    playerManager.setExp(uuid, jOpt.get(), eOpt.get());
                    playerManager.setLevel(uuid, jOpt.get(), lOpt.get());
                }
                fileManager.deleteValue("playerjobdata.conf", data);
            });
        }
    }

}

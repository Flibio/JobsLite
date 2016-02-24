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
package me.flibio.jobslite.listeners;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.data.JobData;
import me.flibio.jobslite.data.JobDataManipulatorBuilder;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.PlayerManager;
import ninja.leaping.configurate.ConfigurationNode;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import io.github.flibio.utils.file.FileManager;

import java.util.Optional;

public class PlayerJoinListener {

    private PlayerManager playerManager = JobsLite.access.playerManager;
    private JobManager jobManager = JobsLite.access.jobManager;
    private FileManager fileManager = JobsLite.access.fileManager;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        playerManager.addPlayer(player);
        if(!playerManager.playerExists(player)) {
            if(!attemptMove(player)) {
                JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
                JobData data = builder.setJobInfo("", 0, 0).create();
                player.offer(data);
            }
        }
        if (!jobManager.jobExists(playerManager.getCurrentJob(player))) {
            JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
            JobData data = builder.setJobInfo("", 0, 0).create();
            player.offer(data);
        }
        JobsLite.access.economyService.createAccount(player.getUniqueId());
    }

    private boolean attemptMove(Player player) {
        String uuid = player.getUniqueId().toString();
        Optional<ConfigurationNode> fOpt = fileManager.getFile("playerData.conf");
        if(fOpt.isPresent()) {
            ConfigurationNode root = fOpt.get();
            if (root != null) {
                ConfigurationNode playerNode = root.getNode(uuid);
                if (playerNode != null) {
                    for (Object raw : playerNode.getChildrenMap().keySet()) {
                        if (raw instanceof String) {
                            String jobName = (String) raw;
                            ConfigurationNode levelNode = root.getNode(uuid).getNode(jobName).getNode("level");
                            ConfigurationNode expNode = root.getNode(uuid).getNode(jobName).getNode("exp");
                            if (levelNode != null && expNode != null) {
                                int level = levelNode.getInt();
                                int exp = expNode.getInt();
                                JobDataManipulatorBuilder builder =
                                        (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
                                JobData data = builder.setJobInfo(jobName, level, exp).create();
                                return player.offer(data).getType().equals(Type.SUCCESS);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}

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
import me.flibio.jobslite.data.LiteKeys;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.PlayerManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

@SuppressWarnings("deprecation")
public class PlayerJoinListener {

    private PlayerManager playerManager = JobsLite.getPlayerManager();
    private JobManager jobManager = JobsLite.getJobManager();

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        playerManager.addPlayer(player);
        moveData(player);
        if (!jobManager.jobExists(playerManager.getCurrentJob(player))) {
            playerManager.clearJobs(player);
        }
        JobsLite.getEconomyService().getOrCreateAccount(player.getUniqueId());
    }

    private void moveData(Player player) {
        if (player.get(LiteKeys.JOB_NAME).isPresent()) {
            String jobName = player.get(LiteKeys.JOB_NAME).get();
            if (jobManager.jobExists(jobName) && !playerManager.hasMoved(player)) {
                // Transfer the job
                playerManager.setJob(player, jobName);
                playerManager.setExp(player, jobName, (double) player.get(LiteKeys.EXP).get());
                playerManager.setLevel(player, jobName, player.get(LiteKeys.LEVEL).get());
                playerManager.markAsMoved(player);
                // Remove the data
                player.remove(JobData.class);
            } else {
                // Remove the data
                player.remove(JobData.class);
            }
        }
    }
}

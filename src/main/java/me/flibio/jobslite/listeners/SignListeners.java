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

import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.Job;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.PlayerManager;
import me.flibio.jobslite.data.LiteKeys;
import me.flibio.jobslite.data.SignJobData;
import me.flibio.jobslite.data.SignJobDataManipulatorBuilder;
import me.flibio.jobslite.utils.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

public class SignListeners {

    private JobManager jobManager = JobsLite.getJobManager();
    private PlayerManager playerManager = JobsLite.getPlayerManager();
    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Listener
    public void onSignChange(ChangeSignEvent event, @First Player player) {
        Sign signTile = event.getTargetTile();
        if (!signTile.get(LiteKeys.JOB_NAME).isPresent() && player.hasPermission("jobs.admin.sign.create")) {
            SignData data = event.getText();
            try {
                if (data.getValue(Keys.SIGN_LINES).get().get(0).toPlain().equals("[JobsLite]")) {
                    String signJob = data.getValue(Keys.SIGN_LINES).get().get(1).toPlain();
                    if (jobManager.jobExists(signJob)) {
                        signTile.offer(data.set(data.getValue(Keys.SIGN_LINES).get().set(0, Text.of("[Job]"))));
                        SignJobDataManipulatorBuilder builder =
                                (SignJobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(SignJobData.class).get();
                        SignJobData signJobData = builder.setSignInfo(signJob).create();
                        signTile.offer(signJobData);
                        player.sendMessage(messageStorage.getMessage("sign.success"));
                        return;
                    } else {
                        player.sendMessage(messageStorage.getMessage("sign.nojob"));
                        return;
                    }
                }
            } catch (IndexOutOfBoundsException | NoSuchElementException e) {
                player.sendMessage(messageStorage.getMessage("sign.invalid"));
                return;
            }
        } else if (signTile.get(LiteKeys.JOB_NAME).isPresent() && !player.hasPermission("jobs.admin.sign.delete")) {
            player.sendMessage(messageStorage.getMessage("sign.nopermission"));
            event.setCancelled(true);
        }
    }

    @Listener
    public void onSignClick(InteractBlockEvent.Secondary event, @First Player player) {
        Optional<String> dOpt = event.getTargetBlock().get(LiteKeys.JOB_NAME);
        if (dOpt.isPresent()) {
            String jobString = dOpt.get();
            Optional<Job> jOpt = jobManager.getJob(jobString);
            if (jOpt.isPresent()) {
                Job job = jOpt.get();
                String displayName = job.getDisplayName();
                if (!displayName.isEmpty()) {
                    if (playerManager.getCurrentJobs(player.getUniqueId()).contains(job.getId())) {
                        player.sendMessage(messageStorage.getMessage("command.join.already", "job", displayName));
                        return;
                    }
                    player.sendMessage(messageStorage.getMessage("command.join.confirm", "job", displayName));
                    player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

                        @Override
                        public void accept(CommandSource source) {
                            if (!playerManager.addJob(player.getUniqueId(), job.getId())) {
                                player.sendMessage(messageStorage.getMessage("generic.error"));
                                return;
                            }
                            player.sendMessage(messageStorage.getMessage("command.join.success", "job", displayName));
                        }

                    }));
                }
            } else {
                player.sendMessage(messageStorage.getMessage("sign.invalid"));
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        Optional<Player> playerOptional = event.getCause().first(Player.class);
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.isValid()) {
                if (transaction.getOriginal().get(LiteKeys.JOB_NAME).isPresent()) {
                    if (!playerOptional.isPresent()) {
                        event.setCancelled(true);
                        return;
                    }
                    Player player = playerOptional.get();
                    if (!player.hasPermission("jobs.admin.sign.delete")) {
                        player.sendMessage(messageStorage.getMessage("sign.nopermission"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}

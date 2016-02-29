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
import me.flibio.jobslite.data.LiteKeys;
import me.flibio.jobslite.data.SignJobData;
import me.flibio.jobslite.data.SignJobDataManipulatorBuilder;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.PlayerManager;
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
import org.spongepowered.api.text.format.TextColors;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

public class SignListeners {

    private JobManager jobManager = JobsLite.access.jobManager;
    private PlayerManager playerManager = JobsLite.access.playerManager;

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
                        player.sendMessage(Text.of(TextColors.GREEN, "Successfully created a join job sign!"));
                        return;
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "That job could not be found!"));
                        return;
                    }
                }
            } catch (IndexOutOfBoundsException | NoSuchElementException e) {
                player.sendMessage(Text.of(TextColors.RED, "Please follow the correct sign format!"));
                return;
            }
        } else if (signTile.get(LiteKeys.JOB_NAME).isPresent() && !player.hasPermission("jobs.admin.sign.delete")) {
            player.sendMessage(Text.of(TextColors.RED, "You may not edit this sign!"));
            event.setCancelled(true);
        }
    }

    @Listener
    public void onSignClick(InteractBlockEvent.Secondary event, @First Player player) {
        Optional<String> dOpt = event.getTargetBlock().get(LiteKeys.JOB_NAME);
        if (dOpt.isPresent()) {
            String job = dOpt.get();
            if (jobManager.jobExists(job)) {
                String displayName = jobManager.getDisplayName(job);
                if (!displayName.isEmpty()) {
                    if (playerManager.getCurrentJob(player).equalsIgnoreCase(job)) {
                        player.sendMessage(TextUtils.error("You are already a " + job + "!"));
                        return;
                    }
                    player.sendMessage(TextUtils.success("Are you sure you want to become a " + displayName + "?", TextColors.GREEN));
                    player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

                        @Override
                        public void accept(CommandSource source) {
                            if (!playerManager.setJob(player, job)) {
                                player.sendMessage(TextUtils.error("An error has occured!"));
                                return;
                            }
                            player.sendMessage(TextUtils.success("You are now a " + displayName + "!", TextColors.GREEN));
                        }

                    }));
                    player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

                        @Override
                        public void accept(CommandSource source) {
                            player.sendMessage(TextUtils.error("If you change your mind, you can click any of the above options again!"));
                        }

                    }));
                }
            } else {
                player.sendMessage(Text.of(TextColors.RED, "The job could not be found!"));
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.isValid()) {
                if (transaction.getOriginal().get(LiteKeys.JOB_NAME).isPresent() && !player.hasPermission("jobs.admin.sign.delete")) {
                    player.sendMessage(Text.of(TextColors.RED, "You may not remove this sign!"));
                    event.setCancelled(true);
                }
            }
        }
    }
}

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
package me.flibio.jobslite.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.Job;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.PlayerManager;
import me.flibio.jobslite.api.Reward;
import me.flibio.jobslite.utils.NumberUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

public class PlayerPlaceBlockListener {

    private UniqueAccount account;
    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Player player) {
        Sponge.getScheduler().createTaskBuilder().execute(c -> {
            run(event, player);
        }).async().submit(JobsLite.getInstance());
    }

    private void run(ChangeBlockEvent.Place event, Player player) {
        UUID uuid = player.getUniqueId();
        // Load managers
        PlayerManager playerManager = JobsLite.getPlayerManager();
        JobManager jobManager = JobsLite.getJobManager();
        for (String jobString : playerManager.getCurrentJobs(uuid)) {
            Optional<Job> jOpt = jobManager.getJob(jobString);
            if (jOpt.isPresent()) {
                Job job = jOpt.get();
                String displayName = job.getDisplayName();
                String id = job.getId();
                Optional<UniqueAccount> uOpt = JobsLite.getEconomyService().getOrCreateAccount(player.getUniqueId());
                if (!uOpt.isPresent()) {
                    return;
                }
                account = uOpt.get();
                for (Entry<String, Reward> entry : job.getBlockPlaces().entrySet()) {
                    String block = entry.getKey();
                    for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                        if (transaction.isValid()) {
                            BlockState blockTransactionState = transaction.getFinal().getState();
                            boolean pass = false;
                            if (job.isIgnoreData()) {
                                // Ignore data
                                String newBlock = block.replaceAll("\\[.*?]", "");
                                if (newBlock.equalsIgnoreCase(blockTransactionState.getId().replaceAll("\\[.*?]", ""))) {
                                    pass = true;
                                }
                            } else {
                                // Use data
                                if (block.equalsIgnoreCase(blockTransactionState.toString())) {
                                    pass = true;
                                }
                            }
                            if (pass) {
                                // Block is a match!
                                // Get all variables
                                // Max Level
                                int maxLevel = job.getMaxLevel();
                                // Current Level
                                int playerLevel = playerManager.getCurrentLevel(uuid, id);
                                if (playerLevel < 0)
                                    return;
                                // Current Exp
                                Optional<Double> playerExp = playerManager.getCurrentExp(uuid, id);
                                if (!playerExp.isPresent())
                                    return;
                                // Base exp reward
                                double baseExpReward = entry.getValue().getExp();
                                // Base currency reward
                                double baseCurrencyReward = entry.getValue().getCurrency();
                                // Get the equations
                                String rewardEquation = job.getCurEquation();
                                String rewardCurrencyEquation = rewardEquation;
                                String expEquation = job.getExpEquation();
                                // Replace the variables in the equation
                                rewardEquation =
                                        rewardEquation.replaceAll("startingPoint", baseExpReward + "").replaceAll("currentLevel",
                                                playerLevel + "");
                                rewardCurrencyEquation =
                                        rewardCurrencyEquation.replaceAll("startingPoint", baseCurrencyReward + "").replaceAll(
                                                "currentLevel", playerLevel + "");
                                expEquation = expEquation.replaceAll("currentLevel", playerLevel + "");
                                // Calculate the data
                                double reward;
                                double expRequired = NumberUtils.eval(expEquation);
                                double currencyReward;
                                if (playerLevel == 0) {
                                    reward = baseExpReward;
                                    currencyReward = baseCurrencyReward;
                                } else {
                                    reward = NumberUtils.eval(rewardEquation);
                                    currencyReward = NumberUtils.eval(rewardCurrencyEquation);
                                }
                                // Figure it out
                                if (playerExp.get() + reward >= expRequired) {
                                    // Player is leveling up
                                    if (playerLevel == maxLevel) {
                                        // Already at max level
                                        addFunds(currencyReward, player);
                                        return;
                                    } else {
                                        if (playerLevel + 1 == maxLevel) {
                                            playerManager.setLevel(uuid, id, playerLevel + 1);
                                            // Sound
                                            player.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, player.getLocation().getPosition(), 1);
                                            addFunds(currencyReward, player);
                                            player.sendMessage(messageStorage.getMessage("working.levelup", ImmutableMap.of("player",
                                                    Text.of(player.getName()), "level", Text.of(playerLevel + 1), "job", Text.of(displayName))));
                                            // Tell them they are now at the
                                            // max level
                                            player.sendMessage(messageStorage.getMessage("working.maxlevel", "job", displayName));
                                        } else {
                                            double expLeft = reward - (expRequired - playerExp.get());
                                            playerManager.setExp(uuid, id, expLeft);
                                            playerManager.setLevel(uuid, id, playerLevel + 1);
                                            // Sound
                                            player.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, player.getLocation().getPosition(), 1);
                                            addFunds(currencyReward, player);
                                            player.sendMessage(messageStorage.getMessage("working.levelup", ImmutableMap.of("player",
                                                    Text.of(player.getName()), "level", Text.of(playerLevel + 1), "job", Text.of(displayName))));
                                            // Tell them their new
                                            // statistics
                                            String newExpEq = expEquation.replaceAll("currentLevel", (playerLevel + 2) + "");
                                            double newExp = NumberUtils.eval(newExpEq);
                                            player.sendMessage(messageStorage.getMessage("working.nextlevel", "exp", NumberFormat
                                                    .getNumberInstance(Locale.US).format(newExp - expLeft)));
                                        }
                                    }
                                } else {
                                    // Player isn't leveling up
                                    addFunds(currencyReward, player);
                                    // If the player is at the max level
                                    // don't give them exp
                                    if (maxLevel == playerLevel)
                                        return;
                                    playerManager.setExp(uuid, id, playerExp.get() + reward);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addFunds(double amount, Player player) {
        String virt = JobsLite.getOption("virtual-draw");
        if (virt.equalsIgnoreCase("none")) {
            // Generate money
            account.deposit(JobsLite.getEconomyService().getDefaultCurrency(), BigDecimal.valueOf(amount),
                    Cause.of(EventContext.empty(), JobsLite.getInstance().container));
        } else {
            // Transfer money
            Optional<Account> aOpt = JobsLite.getEconomyService().getOrCreateAccount(virt);
            if (aOpt.isPresent()) {
                if (!aOpt.get()
                        .transfer(account, JobsLite.getEconomyService().getDefaultCurrency(), BigDecimal.valueOf(amount),
                                Cause.of(EventContext.empty(),JobsLite.getInstance().container)).getResult().equals(ResultType.SUCCESS)) {
                    // Transfer failed
                    if (!JobsLite.msgCache.contains(player.getUniqueId())) {
                        player.sendMessage(messageStorage.getMessage("working.nofunds"));
                        JobsLite.msgCache.add(player.getUniqueId());
                    }
                }
            } else {
                // Failed to find account
                JobsLite.getInstance().logger.error("Failed to find virtual account! Payment will not be given!");
            }
        }
    }

}

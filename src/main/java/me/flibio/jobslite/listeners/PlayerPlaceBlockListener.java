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
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.JobManager.ActionType;
import me.flibio.jobslite.utils.NumberUtils;
import me.flibio.jobslite.utils.PlayerManager;
import me.flibio.jobslite.utils.TextUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.Optional;

public class PlayerPlaceBlockListener {

    private UniqueAccount account;

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Player player) {
        // Load managers
        PlayerManager playerManager = JobsLite.access.playerManager;
        JobManager jobManager = JobsLite.access.jobManager;
        if (playerManager.playerExists(player)) {
            String job = playerManager.getCurrentJob(player).trim();
            if (!job.isEmpty()) {
                if (jobManager.jobExists(job)) {
                    String displayName = jobManager.getDisplayName(job);
                    if (displayName.isEmpty())
                        return;
                    Optional<UniqueAccount> uOpt = JobsLite.access.economyService.getOrCreateAccount(player.getUniqueId());
                    if (!uOpt.isPresent()) {
                        return;
                    }
                    account = uOpt.get();
                    for (String block : jobManager.getPlaceBlocks(job)) {
                        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                            if (transaction.isValid()) {
                                BlockState blockTransactionState = transaction.getFinal().getState();
                                if (block.equalsIgnoreCase(blockTransactionState.toString())
                                        || block.equalsIgnoreCase(blockTransactionState.getType().getName())) {
                                    // Block is a match!
                                    // Get all variables
                                    // Max Level
                                    int maxLevel = jobManager.getMaxLevel(job);
                                    if (maxLevel < 0)
                                        return;
                                    // Current Level
                                    int playerLevel = playerManager.getCurrentLevel(player, job);
                                    if (playerLevel < 0)
                                        return;
                                    // Current Exp
                                    int playerExp = playerManager.getCurrentExp(player, job);
                                    if (playerExp < 0)
                                        return;
                                    // Base exp reward
                                    int baseExpReward = jobManager.getExpReward(job, block, ActionType.PLACE);
                                    if (baseExpReward < 0)
                                        return;
                                    // Base currency reward
                                    int baseCurrencyReward = jobManager.getCurrencyReward(job, block, ActionType.PLACE);
                                    if (baseCurrencyReward < 0)
                                        return;
                                    // Get the equations
                                    String rewardEquation = jobManager.getRewardEquation(job);
                                    if (rewardEquation.isEmpty())
                                        return;
                                    String rewardCurrencyEquation = rewardEquation;
                                    String expEquation = jobManager.getExpRequiredEquation(job);
                                    if (expEquation.isEmpty())
                                        return;
                                    // Replace the variables in the equation
                                    rewardEquation =
                                            rewardEquation.replaceAll("startingPoint", baseExpReward + "").replaceAll("currentLevel",
                                                    playerLevel + "");
                                    rewardCurrencyEquation =
                                            rewardCurrencyEquation.replaceAll("startingPoint", baseCurrencyReward + "").replaceAll("currentLevel",
                                                    playerLevel + "");
                                    expEquation = expEquation.replaceAll("currentLevel", playerLevel + "");
                                    // Calculate the data
                                    int reward;
                                    int expRequired = (int) Math.round(NumberUtils.eval(expEquation));
                                    int currencyReward;
                                    if (playerLevel == 0) {
                                        reward = baseExpReward;
                                        currencyReward = baseCurrencyReward;
                                    } else {
                                        reward = (int) Math.round(NumberUtils.eval(rewardEquation));
                                        currencyReward = (int) Math.round(NumberUtils.eval(rewardCurrencyEquation));
                                    }
                                    // Figure it out
                                    if (playerExp + reward >= expRequired) {
                                        // Player is leveling up
                                        if (playerLevel == maxLevel) {
                                            // Already at max level
                                            addFunds(currencyReward);
                                            return;
                                        } else {
                                            if (playerLevel + 1 == maxLevel) {
                                                playerManager.setLevel(player, job, playerLevel + 1);
                                                // Sound
                                                player.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, player.getLocation().getPosition(), 1);
                                                addFunds(currencyReward);
                                                player.sendMessage(TextUtils.levelUp(player.getName(), playerLevel + 1, displayName));
                                                // Tell them they are now at the
                                                // max level
                                                player.sendMessage(TextUtils.maxLevel(displayName));
                                            } else {
                                                int expLeft = reward - (expRequired - playerExp);
                                                playerManager.setExp(player, job, expLeft);
                                                playerManager.setLevel(player, job, playerLevel + 1);
                                                // Sound
                                                player.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, player.getLocation().getPosition(), 1);
                                                addFunds(currencyReward);
                                                player.sendMessage(TextUtils.levelUp(player.getName(), playerLevel + 1, displayName));
                                                // Tell them their new
                                                // statistics
                                                String newExpEq = expEquation.replaceAll("currentLevel", (playerLevel + 2) + "");
                                                int newExp = (int) Math.round(NumberUtils.eval(newExpEq));
                                                player.sendMessage(TextUtils.toGo(newExp - expLeft, playerLevel + 2, displayName));
                                            }
                                        }
                                    } else {
                                        // Player isn't leveling up
                                        addFunds(currencyReward);
                                        // If the player is at the max level
                                        // don't give them exp
                                        if (maxLevel == playerLevel)
                                            return;
                                        playerManager.setExp(player, job, playerExp + reward);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addFunds(int amount) {
        account.deposit(JobsLite.access.economyService.getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(NamedCause.owner(JobsLite.access)));
    }

}

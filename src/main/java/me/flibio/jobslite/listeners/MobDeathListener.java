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

import com.google.common.collect.ImmutableMap;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.JobManager.ActionType;
import me.flibio.jobslite.utils.NumberUtils;
import me.flibio.jobslite.utils.PlayerManager;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class MobDeathListener {

    private UniqueAccount account;
    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Listener
    public void onBlockBreak(DestructEntityEvent.Death event, @First EntityDamageSource source) {
        if (source.getSource() instanceof Player) {
            Player player = ((Player) source.getSource());
            PlayerManager playerManager = JobsLite.getPlayerManager();
            JobManager jobManager = JobsLite.getJobManager();
            if (playerManager.playerExists(player)) {
                String job = playerManager.getCurrentJob(player).trim();
                if (!job.isEmpty()) {
                    if (jobManager.jobExists(job)) {
                        String displayName = jobManager.getDisplayName(job);
                        if (displayName.isEmpty())
                            return;
                        Optional<UniqueAccount> uOpt = JobsLite.getEconomyService().getOrCreateAccount(player.getUniqueId());
                        if (!uOpt.isPresent()) {
                            return;
                        }
                        account = uOpt.get();
                        for (String mob : jobManager.getKillMobs(job)) {
                            if (mob.equalsIgnoreCase(event.getTargetEntity().getType().getId())) {
                                // Mob is a match!
                                // Get all variables
                                // Max Level
                                int maxLevel = jobManager.getMaxLevel(job);
                                if (maxLevel < 0)
                                    continue;
                                // Current Level
                                int playerLevel = playerManager.getCurrentLevel(player, job);
                                if (playerLevel < 0)
                                    continue;
                                // Current Exp
                                Optional<Double> playerExp = playerManager.getCurrentExp(player, job);
                                if (!playerExp.isPresent())
                                    continue;
                                // Base exp reward
                                Optional<Double> baseExpReward = jobManager.getExpReward(job, mob, ActionType.KILL);
                                if (!baseExpReward.isPresent())
                                    continue;
                                // Base currency reward
                                Optional<Double> baseCurrencyReward = jobManager.getCurrencyReward(job, mob, ActionType.KILL);
                                if (!baseExpReward.isPresent())
                                    continue;
                                // Get the equations
                                String rewardEquation = jobManager.getRewardEquation(job);
                                if (rewardEquation.isEmpty())
                                    continue;
                                String rewardCurrencyEquation = rewardEquation;
                                String expEquation = jobManager.getExpRequiredEquation(job);
                                if (expEquation.isEmpty())
                                    continue;
                                // Replace the variables in the equation
                                rewardEquation =
                                        rewardEquation.replaceAll("startingPoint", baseExpReward.get() + "").replaceAll("currentLevel",
                                                playerLevel + "");
                                rewardCurrencyEquation =
                                        rewardCurrencyEquation.replaceAll("startingPoint", baseCurrencyReward.get() + "").replaceAll(
                                                "currentLevel", playerLevel + "");
                                expEquation = expEquation.replaceAll("currentLevel", playerLevel + "");
                                // Calculate the data
                                double reward;
                                double expRequired = NumberUtils.eval(expEquation);
                                double currencyReward;
                                if (playerLevel == 0) {
                                    reward = baseExpReward.get();
                                    currencyReward = baseCurrencyReward.get();
                                } else {
                                    reward = NumberUtils.eval(rewardEquation);
                                    currencyReward = NumberUtils.eval(rewardCurrencyEquation);
                                }
                                // Figure it out
                                if (playerExp.get() + reward >= expRequired) {
                                    // Player is leveling up
                                    if (playerLevel == maxLevel) {
                                        // Already at max level
                                        addFunds(currencyReward);
                                        continue;
                                    } else {
                                        if (playerLevel + 1 == maxLevel) {
                                            playerManager.setLevel(player, job, playerLevel + 1);
                                            // Sound
                                            player.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, player.getLocation().getPosition(), 1);
                                            addFunds(currencyReward);
                                            player.sendMessage(messageStorage.getMessage("working.levelup", ImmutableMap.of("player",
                                                    Text.of(player.getName()), "level", Text.of(playerLevel + 1), "job", Text.of(displayName))));
                                            // Tell them they are now at the
                                            // max level
                                            player.sendMessage(messageStorage.getMessage("working.maxlevel", "job", displayName));
                                        } else {
                                            double expLeft = reward - (expRequired - playerExp.get());
                                            playerManager.setExp(player, job, expLeft);
                                            playerManager.setLevel(player, job, playerLevel + 1);
                                            // Sound
                                            player.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, player.getLocation().getPosition(), 1);
                                            addFunds(currencyReward);
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
                                    addFunds(currencyReward);
                                    // If the player is at the max level
                                    // don't give them exp
                                    if (maxLevel == playerLevel)
                                        continue;
                                    playerManager.setExp(player, job, playerExp.get() + reward);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addFunds(double amount) {
        account.deposit(JobsLite.getEconomyService().getDefaultCurrency(), BigDecimal.valueOf(amount),
                Cause.of(NamedCause.owner(JobsLite.getInstance())));
    }

}

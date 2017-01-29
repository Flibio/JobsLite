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
package me.flibio.jobslite.creation.task;

import me.flibio.jobslite.api.Reward;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.creation.CreatingJob;
import me.flibio.jobslite.creation.data.KillData;
import me.flibio.jobslite.utils.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class KillTask extends Task {

    enum State {
        CLICK, CURRENCY, EXP, SELECTION
    }

    private State state;
    private Map<EntityType, Reward> rewards = new HashMap<>();
    private double currency;
    private double exp;
    private EntityType entityType;

    public KillTask(CreatingJob parent) {
        super(parent);
    }

    public void initialize() {
        Sponge.getEventManager().registerListeners(JobsLite.getInstance(), this);
        // Reset variables
        state = State.SELECTION;
        rewards.clear();
        currency = 0.0;
        exp = 0.0;
        entityType = null;
        // Ask the player if they want to add mobs
        getPlayer().sendMessage(messages.getMessage("creation.killing"));
        // Yes the player wants to add mobs
        getPlayer().sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

            public void accept(CommandSource source) {
                if (state.equals(State.SELECTION)) {
                    getPlayer().sendMessage(messages.getMessage("creation.killselect"));
                    state = State.CLICK;
                }
            }

        }));
        // No the player does not want to add mobs
        getPlayer().sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

            public void accept(CommandSource source) {
                if (state.equals(State.SELECTION)) {
                    getParent().setData(new KillData(rewards));
                    Sponge.getEventManager().unregisterListeners(this);
                    getParent().nextTask();
                }
            }

        }));
    }

    @Listener
    public void onPlayerClick(InteractEntityEvent event, @First Player player) {
        if (state.equals(State.CLICK) && player.getUniqueId().equals(getParent().getUUID())) {
            player.sendMessage(messages.getMessage("creation.killcurrency"));
            entityType = event.getTargetEntity().getType();
            state = State.CURRENCY;
        }
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        String message = event.getRawMessage().toPlain();
        if (player.getUniqueId().equals(getParent().getUUID())) {
            if (state.equals(State.CURRENCY)) {
                // Verify the data
                Optional<Double> dOpt = verifyDouble(message);
                if (dOpt.isPresent()) {
                    currency = dOpt.get();
                    player.sendMessage(messages.getMessage("creation.killbase", "amount", currency + " currency", "mob", entityType.getName()));
                    player.sendMessage(messages.getMessage("creation.killingexp"));
                    state = State.EXP;
                } else {
                    player.sendMessage(messages.getMessage("creation.validnumber"));
                }
                event.setCancelled(true);
            } else if (state.equals(State.EXP)) {
                // Verify the data
                Optional<Double> dOpt = verifyDouble(message);
                if (dOpt.isPresent()) {
                    exp = dOpt.get();
                    player.sendMessage(messages.getMessage("creation.killbase", "amount", currency + " currency", "mob", entityType.getName()));
                    state = State.SELECTION;
                    // Save the block
                    rewards.put(entityType, new Reward(currency, exp));
                    // Reset the variables
                    entityType = null;
                    currency = 0.0;
                    exp = 0.0;
                    // Ask the player if they would like to add another mob
                    player.sendMessage(messages.getMessage("creation.anothermob"));
                    // Yes - Select a new mob
                    player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

                        public void accept(CommandSource source) {
                            if (state.equals(State.SELECTION)) {
                                player.sendMessage(messages.getMessage("creation.killselect"));
                                state = State.CLICK;
                            }
                        }

                    }));
                    // No - Save the data
                    player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

                        public void accept(CommandSource source) {
                            if (state.equals(State.SELECTION)) {
                                // Save the data
                                getParent().setData(new KillData(rewards));
                                // Move to the next task
                                Sponge.getEventManager().unregisterListeners(this);
                                getParent().nextTask();
                            }
                        }

                    }));
                } else {
                    player.sendMessage(messages.getMessage("creation.validnumber"));
                }
                event.setCancelled(true);
            }
        }
    }
}

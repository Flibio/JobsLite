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
package me.flibio.jobslite.creation;

import org.spongepowered.api.event.Order;

import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.creation.data.DataType;
import me.flibio.jobslite.creation.data.DataTypes;
import me.flibio.jobslite.creation.task.BreakTask;
import me.flibio.jobslite.creation.task.ColorTask;
import me.flibio.jobslite.creation.task.FinalTask;
import me.flibio.jobslite.creation.task.KillTask;
import me.flibio.jobslite.creation.task.MaxLevelTask;
import me.flibio.jobslite.creation.task.NameTask;
import me.flibio.jobslite.creation.task.PlaceTask;
import me.flibio.jobslite.creation.task.SilkTouchTask;
import me.flibio.jobslite.creation.task.Task;
import me.flibio.jobslite.creation.task.WorldGenTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

public class CreatingJob {

    private MessageStorage messages = JobsLite.getMessageStorage();

    private UUID uuid;
    private SortedMap<Integer, Task> tasks = new TreeMap<>();

    private int currentTask = 0;
    private boolean cancelled = false;
    private Map<DataTypes, DataType<?>> data = new HashMap<>();

    public CreatingJob(UUID uuid) {
        this.uuid = uuid;
        // Add tasks
        addTasks(new NameTask(this), new MaxLevelTask(this), new ColorTask(this), new BreakTask(this), new PlaceTask(this), new KillTask(this),
                new SilkTouchTask(this), new WorldGenTask(this), new FinalTask(this));
        // Send cancel message
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid))
                player.sendMessage(messages.getMessage("creation.cancel"));
        }
        // Register listener
        Sponge.getEventManager().registerListeners(JobsLite.getInstance(), this);
        // Start task
        nextTask();
    }

    public UUID getUUID() {
        return uuid;
    }

    public DataType<?> getData(DataTypes type) {
        return data.get(type);
    }

    public void setData(DataType<?> data) {
        this.data.put(data.getType(), data);
    }

    public void cancel() {
        cancelled = true;
        tasks.values().forEach(task -> {
            Sponge.getEventManager().unregisterListeners(task);
        });
    }

    public void nextTask() {
        if (!cancelled) {
            // Make sure a next task exists
            if (tasks.containsKey(currentTask + 1)) {
                currentTask++;
                tasks.get(currentTask).initialize();
            }
        }
    }

    private void addTasks(Task... tasks) {
        Arrays.asList(tasks).forEach(task -> {
            if (this.tasks.size() > 0) {
                int last = this.tasks.lastKey();
                this.tasks.put(last + 1, task);
            } else {
                this.tasks.put(1, task);
            }
        });
    }

    @Listener(order = Order.FIRST)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        String message = event.getRawMessage().toPlain();
        if (player.getUniqueId().equals(uuid)) {
            if (message.equalsIgnoreCase("[cancel]")) {
                // Cancel the creation
                cancel();
                player.sendMessage(messages.getMessage("creation.cancelled"));
            }
        }
    }

}

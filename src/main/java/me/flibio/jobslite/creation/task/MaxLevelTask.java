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
package me.flibio.jobslite.creation.task;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.creation.CreatingJob;
import me.flibio.jobslite.creation.data.MaxLevelData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.Optional;

public class MaxLevelTask extends Task {

    public MaxLevelTask(CreatingJob parent) {
        super(parent);
    }

    public void initialize() {
        Sponge.getEventManager().registerListeners(JobsLite.getInstance(), this);
        getPlayer().sendMessage(messages.getMessage("creation.maxlevel"));
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        String message = event.getRawMessage().toPlain();
        if (player.getUniqueId().equals(getParent().getUUID())) {
            // Verify the data
            Optional<Integer> iOpt = verifyInteger(message);
            if (iOpt.isPresent() && iOpt.get() > 0) {
                // Set the data
                getParent().setData(new MaxLevelData(iOpt.get()));
                // Send confirmation
                player.sendMessage(messages.getMessage("creation.registered.maxlevel", "level", iOpt.get().toString()));
                // Move to the next task
                getParent().nextTask();
                // Remove the listener
                Sponge.getEventManager().unregisterListeners(this);
            } else {
                player.sendMessage(messages.getMessage("creation.validnumber"));
            }
            event.setCancelled(true);
        }
    }

}

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
package me.flibio.jobslite.creation.task;

import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.creation.CreatingJob;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public abstract class Task {

    protected MessageStorage messages = JobsLite.getMessageStorage();
    private CreatingJob parent;

    public Task(CreatingJob parent) {
        this.parent = parent;
    }

    public abstract void initialize();

    public CreatingJob getParent() {
        return parent;
    }

    protected Player getPlayer() {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.getUniqueId().equals(parent.getUUID()))
                return player;
        }
        return null;
    }

    protected Optional<Integer> verifyInteger(String string) {
        try {
            return Optional.of(Integer.parseInt(string));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected Optional<Double> verifyDouble(String string) {
        try {
            return Optional.of(Double.parseDouble(string));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

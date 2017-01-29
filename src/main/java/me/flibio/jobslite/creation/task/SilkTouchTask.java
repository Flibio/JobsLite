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

import me.flibio.jobslite.creation.CreatingJob;
import me.flibio.jobslite.creation.data.SilkTouchData;
import me.flibio.jobslite.utils.TextUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Consumer;

public class SilkTouchTask extends Task {

    boolean complete = false;

    public SilkTouchTask(CreatingJob parent) {
        super(parent);
    }

    public void initialize() {
        complete = false;
        Player player = getPlayer();
        player.sendMessage(messages.getMessage("creation.silktouch"));
        player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

            public void accept(CommandSource source) {
                if (!complete) {
                    complete = true;
                    player.sendMessage(messages.getMessage("creation.silktouchyes"));
                    getParent().setData(new SilkTouchData(true));
                    getParent().nextTask();
                }
            }

        }));

        player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

            public void accept(CommandSource source) {
                if (!complete) {
                    complete = true;
                    player.sendMessage(messages.getMessage("creation.silktouchno"));
                    getParent().setData(new SilkTouchData(false));
                    getParent().nextTask();
                }
            }

        }));
    }

}

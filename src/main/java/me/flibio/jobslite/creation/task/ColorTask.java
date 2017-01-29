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

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.creation.CreatingJob;
import me.flibio.jobslite.creation.data.ColorData;
import me.flibio.jobslite.utils.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class ColorTask extends Task {

    public ColorTask(CreatingJob parent) {
        super(parent);
    }

    public void initialize() {
        Sponge.getEventManager().registerListeners(JobsLite.getInstance(), this);
        colorChoices();
    }

    private void colorChoices() {
        ArrayList<TextColor> colors1 =
                new ArrayList<TextColor>(Arrays.asList(TextColors.AQUA, TextColors.BLACK, TextColors.BLUE, TextColors.DARK_AQUA));
        Text msg1 = Text.builder().build();
        for (TextColor currentColor : colors1) {
            msg1 = msg1.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {

                @Override
                public void accept(CommandSource source) {
                    if (getParent().isCancelled())
                        return;
                    // Set the data
                    getParent().setData(new ColorData(currentColor));
                    // Send confirmation
                    source.sendMessage(messages.getMessage("creation.setcolor", "color", Text.of(currentColor, currentColor.getName())));
                    // Move to the next task
                    getParent().nextTask();
                    // Remove the listeer
                    Sponge.getEventManager().unregisterListeners(this);
                }
            }, currentColor, currentColor.getName())).build();
        }
        ArrayList<TextColor> colors2 =
                new ArrayList<TextColor>(Arrays.asList(TextColors.DARK_BLUE, TextColors.DARK_GRAY, TextColors.DARK_GREEN, TextColors.DARK_PURPLE));
        Text msg2 = Text.builder().build();
        for (TextColor currentColor : colors2) {
            msg2 = msg2.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {

                @Override
                public void accept(CommandSource source) {
                    if (getParent().isCancelled())
                        return;
                    // Set the data
                    getParent().setData(new ColorData(currentColor));
                    // Send confirmation
                    source.sendMessage(messages.getMessage("creation.setcolor", "color", Text.of(currentColor, currentColor.getName())));
                    // Move to the next task
                    getParent().nextTask();
                    // Remove the listeer
                    Sponge.getEventManager().unregisterListeners(this);
                }
            }, currentColor, currentColor.getName())).build();
        }
        ArrayList<TextColor> colors3 =
                new ArrayList<TextColor>(Arrays.asList(TextColors.DARK_RED, TextColors.GOLD, TextColors.GRAY, TextColors.GREEN));
        Text msg3 = Text.builder().build();
        for (TextColor currentColor : colors3) {
            msg3 = msg3.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {

                @Override
                public void accept(CommandSource source) {
                    if (getParent().isCancelled())
                        return;
                    // Set the data
                    getParent().setData(new ColorData(currentColor));
                    // Send confirmation
                    source.sendMessage(messages.getMessage("creation.setcolor", "color", Text.of(currentColor, currentColor.getName())));
                    // Move to the next task
                    getParent().nextTask();
                    // Remove the listeer
                    Sponge.getEventManager().unregisterListeners(this);
                }
            }, currentColor, currentColor.getName())).build();
        }
        ArrayList<TextColor> colors4 =
                new ArrayList<TextColor>(Arrays.asList(TextColors.LIGHT_PURPLE, TextColors.RED, TextColors.WHITE, TextColors.YELLOW));
        Text msg4 = Text.builder().build();
        for (TextColor currentColor : colors4) {
            msg4 = msg4.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {

                @Override
                public void accept(CommandSource source) {
                    if (getParent().isCancelled())
                        return;
                    // Set the data
                    getParent().setData(new ColorData(currentColor));
                    // Send confirmation
                    source.sendMessage(messages.getMessage("creation.setcolor", "color", Text.of(currentColor, currentColor.getName())));
                    // Move to the next task
                    getParent().nextTask();
                    // Remove the listeer
                    Sponge.getEventManager().unregisterListeners(this);
                }
            }, currentColor, currentColor.getName())).build();
        }
        Player player = getPlayer();
        player.sendMessage(messages.getMessage("creation.choosecolor"));
        player.sendMessage(msg1);
        player.sendMessage(msg2);
        player.sendMessage(msg3);
        player.sendMessage(msg4);
    }

}

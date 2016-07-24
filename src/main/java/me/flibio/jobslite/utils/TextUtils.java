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
package me.flibio.jobslite.utils;

import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class TextUtils {

    private static MessageStorage messageStorage = JobsLite.access.messageStorage;

    public static Text yesOption(Consumer<CommandSource> onClick) {
        Text yes = Text.builder("[").color(TextColors.DARK_GRAY).build();
        yes = yes.toBuilder().append(messageStorage.getMessage("generic.yes")).build();
        yes = yes.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();

        yes = yes.toBuilder().onHover(TextActions.showText(messageStorage.getMessage("generic.yes"))).build();

        yes = yes.toBuilder().onClick(TextActions.executeCallback(onClick)).build();

        return yes;
    }

    public static Text noOption(Consumer<CommandSource> onClick) {
        Text no = Text.builder("[").color(TextColors.DARK_GRAY).build();
        no = no.toBuilder().append(messageStorage.getMessage("generic.no")).build();
        no = no.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();

        no = no.toBuilder().onHover(TextActions.showText(messageStorage.getMessage("generic.no"))).build();

        no = no.toBuilder().onClick(TextActions.executeCallback(onClick)).build();

        return no;
    }

    public static Text option(Consumer<CommandSource> onClick, TextColor color, String option) {
        Text text = Text.builder("[").color(TextColors.DARK_GRAY).build();
        text = text.toBuilder().append(Text.builder(option).color(color).build()).build();
        text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();

        text = text.toBuilder().onHover(TextActions.showText(Text.builder(option).color(color).build())).build();

        text = text.toBuilder().onClick(TextActions.executeCallback(onClick)).build();

        return text;
    }

    public static Text chatMessage(String name, String jobName, TextColor jobColor, String message) {
        Text text = Text.builder("[").color(TextColors.DARK_GRAY).build();
        text = text.toBuilder().append(Text.builder(jobName).color(jobColor).build()).build();
        text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
        text = text.toBuilder().append(Text.builder(" " + name + " ").color(TextColors.YELLOW).build()).build();
        text = text.toBuilder().append(Text.builder(message).color(TextColors.WHITE).build()).build();

        return text;
    }

    public static Text chatMessage(String name, String jobName, int level, TextColor jobColor, String message) {
        Text text = Text.builder("[").color(TextColors.DARK_GRAY).build();
        text = text.toBuilder().append(Text.builder(jobName).color(jobColor).build()).build();
        text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
        text = text.toBuilder().append(Text.builder(" {").color(TextColors.DARK_GRAY).build()).build();
        text = text.toBuilder().append(Text.builder(level + "").color(jobColor).build()).build();
        text = text.toBuilder().append(Text.builder("}").color(TextColors.DARK_GRAY).build()).build();
        text = text.toBuilder().append(Text.builder(" " + name + " ").color(TextColors.YELLOW).build()).build();
        text = text.toBuilder().append(Text.builder(message).color(TextColors.WHITE).build()).build();

        return text;
    }

    public static Text line() {
        Text text = Text.builder("").build();

        return text;
    }

}

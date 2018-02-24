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
package me.flibio.jobslite.commands;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Map;
import java.util.Map.Entry;

@AsyncCommand
@Command(aliases = {"jobs"}, permission = "jobs.user.command")
public class JobsCommand extends BaseCommandExecutor<Player> {

    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .description(messageStorage.getMessage("command.jobs.description"));
    }

    @Override
    public void run(Player src, CommandContext args) {
        Map<String, String> perms = Maps.newHashMap(ImmutableMap.of("jobs.user.info", "info", "jobs.user.join", "join", "jobs.user.leave", "leave"));
        Map<String, String> adminPerms =
                ImmutableMap.of("jobs.admin.add", "add", "jobs.admin.create", "create", "jobs.admin.delete", "delete", "jobs.admin.remove", "remove");
        perms.putAll(adminPerms);
        String commands = "";
        // Loop through the permissions
        boolean first = true;
        for (Entry<String, String> entry : perms.entrySet()) {
            String perm = entry.getKey();
            String command = entry.getValue();
            if (src.hasPermission(perm)) {
                if (first) {
                    commands += command;
                } else {
                    commands += " | " + command;
                }
            }
            first = false;
        }
        src.sendMessage(messageStorage.getMessage("command.usage", "command", "/jobs", "subcommands", commands));
    }
}

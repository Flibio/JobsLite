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
package me.flibio.jobslite.commands;

import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.utils.PlayerManager;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;

import java.text.DecimalFormat;
import java.util.Optional;

@AsyncCommand
@ParentCommand(parentCommand = JobsCommand.class)
@Command(aliases = {"info"})
public class InfoCommand extends BaseCommandExecutor<Player> {

    private PlayerManager playerManager = JobsLite.getPlayerManager();
    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .description(messageStorage.getMessage("command.info.description"))
                .permission("jobs.info");
    }

    @Override
    public void run(Player player, CommandContext args) {
        if (playerManager.playerExists(player)) {
            if (!playerManager.getCurrentJob(player).isEmpty()) {
                String job = playerManager.getCurrentJob(player);
                player.sendMessage(messageStorage.getMessage("command.info.job", "job", job));
                player.sendMessage(messageStorage.getMessage("command.info.level", "level", playerManager.getCurrentLevel(player, job) + ""));
                Optional<Double> eOpt = playerManager.getExpRequired(player, job);
                if (eOpt.isPresent()) {
                    String exp = DecimalFormat.getInstance().format(eOpt.get());
                    player.sendMessage(messageStorage.getMessage("command.info.exp", "exp", exp));
                }
            } else {
                player.sendMessage(messageStorage.getMessage("command.leave.nojob"));
            }
        } else {
            player.sendMessage(messageStorage.getMessage("generic.error"));
        }
    }

}

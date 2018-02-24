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

import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.commands.ParentCommand;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.Job;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.PlayerManager;
import me.flibio.jobslite.utils.TextUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@AsyncCommand
@ParentCommand(parentCommand = JobsCommand.class)
@Command(aliases = {"remove"}, permission = "jobs.admin.remove")
public class RemoveCommand extends BaseCommandExecutor<Player> {

    private PlayerManager playerManager = JobsLite.getPlayerManager();
    private JobManager jobManager = JobsLite.getJobManager();
    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.user(Text.of("player")))
                .description(messageStorage.getMessage("command.remove.description"));
    }

    @Override
    public void run(Player player, CommandContext args) {
        Optional<User> target = args.<User>getOne("player");
        if (target.isPresent()) {
            User user = target.get();
            String playerName = user.getName();
            UUID uuid = user.getUniqueId();
            player.sendMessage(messageStorage.getMessage("command.remove.warn", "player", playerName));
            for (String jobId : playerManager.getCurrentJobs(uuid)) {
                Optional<Job> jOpt = jobManager.getJob(jobId);
                if (jOpt.isPresent()) {
                    Job job = jOpt.get();
                    player.sendMessage(TextUtils.option(new Consumer<CommandSource>() {

                        @Override
                        public void accept(CommandSource source) {
                            player.sendMessage(messageStorage.getMessage("command.remove.confirm", "job", job.getDisplayName(), "player", playerName));
                            player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

                                @Override
                                public void accept(CommandSource source) {
                                    if (!playerManager.removeJob(uuid, job.getId())) {
                                        player.sendMessage(messageStorage.getMessage("generic.error"));
                                        return;
                                    }
                                    player.sendMessage(messageStorage.getMessage("command.remove.success", "player", playerName, "job",
                                            job.getDisplayName()));
                                }

                            }));
                        }

                    }, job.getTextColor(), job.getDisplayName()));
                }
            }
        } else {
            player.sendMessage(messageStorage.getMessage("generic.error"));
        }
    }
}

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
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;

@AsyncCommand
@ParentCommand(parentCommand = JobsCommand.class)
@Command(aliases = {"info"}, permission = "jobs.user.info")
public class InfoCommand extends BaseCommandExecutor<Player> {

    private PlayerManager playerManager = JobsLite.getPlayerManager();
    private JobManager jobManager = JobsLite.getJobManager();
    private MessageStorage messageStorage = JobsLite.getMessageStorage();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .description(messageStorage.getMessage("command.info.description"));
    }

    @Override
    public void run(Player player, CommandContext args) {
        UUID uuid = player.getUniqueId();
        boolean first = true;
        for (String jobString : playerManager.getCurrentJobs(uuid)) {
            Optional<Job> jOpt = jobManager.getJob(jobString);
            if (jOpt.isPresent()) {
                Job job = jOpt.get();
                int lvl = playerManager.getCurrentLevel(uuid, jobString);
                if (!first) {
                    player.sendMessage(Text.of(""));
                }
                player.sendMessage(messageStorage.getMessage("command.info.job", "job", job.getDisplayName()));
                player.sendMessage(messageStorage.getMessage("command.info.level", "level", lvl + ""));
                Optional<Double> eOpt = playerManager.getCurrentExp(uuid, jobString);
                if (eOpt.isPresent()) {
                    String exp = DecimalFormat.getInstance().format(job.getExpRequired(lvl, eOpt.get()));
                    player.sendMessage(messageStorage.getMessage("command.info.exp", "exp", exp));
                }
                first = false;
            }
        }

        if (first) {
            player.sendMessage(messageStorage.getMessage("command.info.nojob"));
        }
    }
}

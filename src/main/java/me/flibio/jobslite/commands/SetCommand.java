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

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.PlayerManager;
import me.flibio.jobslite.utils.TextUtils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.function.Consumer;

public class SetCommand implements CommandExecutor {

    private PlayerManager playerManager = JobsLite.access.playerManager;
    private JobManager jobManager = JobsLite.access.jobManager;

    @Override
    public CommandResult execute(CommandSource source, CommandContext args)
            throws CommandException {

        if (!(source instanceof Player)) {
            source.sendMessage(Text.builder("You must be a player to use /jobs!").color(TextColors.RED).build());
            return CommandResult.success();
        }

        Player player = (Player) source;
        Optional<String> target = args.<String>getOne("target");
        if (target.isPresent()) {
            JobsLite.access.game.getScheduler().createTaskBuilder().execute(new Runnable() {

                @Override
                public void run() {
                    String targetName = target.get();
                    Optional<UserStorageService> sOpt = Sponge.getServiceManager().provide(UserStorageService.class);
                    if (sOpt.isPresent()) {
                        UserStorageService userStorage = sOpt.get();
                        Optional<User> uOpt = userStorage.get(targetName);
                        if (uOpt.isPresent()) {
                            Optional<Player> pOpt = uOpt.get().getPlayer();
                            if (!pOpt.isPresent()) {
                                player.sendMessage(TextUtils.error("An error has occured!"));
                                return;
                            }
                            Player targetPlayer = pOpt.get();
                            // Check if the target player exists
                            if (playerManager.playerExists(targetPlayer)) {
                                // Send a list of jobs to the player
                                player.sendMessage(TextUtils.instruction("click on the job you would like " + targetName + " to be"));
                                for (String job : jobManager.getJobs()) {
                                    if (jobManager.jobExists(job)) {
                                        String displayName = jobManager.getDisplayName(job);
                                        if (!displayName.isEmpty()) {
                                            player.sendMessage(TextUtils.option(new Consumer<CommandSource>() {

                                                @Override
                                                public void accept(CommandSource source) {
                                                    if (playerManager.getCurrentJob(targetPlayer).equalsIgnoreCase(job)) {
                                                        player.sendMessage(TextUtils.error(targetName + " is already a " + job + "!"));
                                                        return;
                                                    }
                                                    player.sendMessage(TextUtils.success("Are you sure you want to set " + targetName
                                                            + "'s job to a " + displayName + "?", TextColors.GREEN));
                                                    player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

                                                        @Override
                                                        public void accept(CommandSource source) {
                                                            if (!playerManager.setJob(targetPlayer, job)) {
                                                                player.sendMessage(TextUtils.error("An error has occured!"));
                                                                return;
                                                            } else {
                                                                player.sendMessage(TextUtils.success(targetName + " is now a " + displayName + "!",
                                                                        TextColors.GREEN));
                                                                return;
                                                            }
                                                        }

                                                    }));
                                                    player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

                                                        @Override
                                                        public void accept(CommandSource source) {
                                                            player.sendMessage(TextUtils
                                                                    .error("If you change your mind, you can click any of the above options again!"));
                                                        }

                                                    }));
                                                }

                                            }, jobManager.getColor(job), displayName));
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage(TextUtils.error("An error has occurred!"));
                                return;
                            }
                        } else {
                            player.sendMessage(TextUtils.error("An error has occurred!"));
                            return;
                        }
                    } else {
                        player.sendMessage(TextUtils.error("An error has occurred!"));
                        return;
                    }
                }
            }).async().submit(JobsLite.access);
        } else {
            player.sendMessage(TextUtils.error("An error has occurred!"));
            return CommandResult.success();
        }

        return CommandResult.success();
    }
}

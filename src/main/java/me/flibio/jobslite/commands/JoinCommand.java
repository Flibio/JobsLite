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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class JoinCommand implements CommandExecutor{
	
	private PlayerManager playerManager = JobsLite.access.playerManager;
	private JobManager jobManager = JobsLite.access.jobManager;
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		
		if(!(source instanceof Player)) {
			source.sendMessage(Text.builder("You must be a player to use /jobs!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		
		final Player player = (Player) source;
		if(playerManager.playerExists(player)) {
			player.sendMessage(TextUtils.instruction("click on the job you would like to join"));
			player.sendMessage(TextUtils.error("This will reset your current level and exp for the job!"));
			for( String job : jobManager.getJobs()) {
				if(jobManager.jobExists(job)) {
					final String displayName = jobManager.getDisplayName(job);
					if(!displayName.isEmpty()) {
						player.sendMessage(TextUtils.option(new Consumer<CommandSource>() {

							@Override
							public void accept(CommandSource source) {
								if(playerManager.getCurrentJob(player).equalsIgnoreCase(job)) {
									player.sendMessage(TextUtils.error("You are already a "+job+"!"));
									return;
								}
								player.sendMessage(TextUtils.success("Are you sure you want to become a "+displayName+"?",TextColors.GREEN));
								player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

									@Override
									public void accept(CommandSource source) {
										if(!playerManager.setJob(player, job)) {
											player.sendMessage(TextUtils.error("An error has occured!"));
											return;
										}
										player.sendMessage(TextUtils.success("You are now a "+displayName+"!",TextColors.GREEN));
									}
									
								}));
								player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

									@Override
									public void accept(CommandSource source) {
										player.sendMessage(TextUtils.error("If you change your mind, you can click any of the above options again!"));
									}
									
								}));
							}
							
						}, jobManager.getColor(job), displayName));
					}
				}
			}
		} else {
			source.sendMessage(Text.builder("An error has occurred!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		
		return CommandResult.success();
	}
}

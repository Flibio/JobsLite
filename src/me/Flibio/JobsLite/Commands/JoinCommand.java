package me.Flibio.JobsLite.Commands;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.PlayerManager;
import me.Flibio.JobsLite.Utils.TextUtils;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class JoinCommand implements CommandExecutor{
	
	private PlayerManager playerManager = Main.access.playerManager;
	private JobManager jobManager = Main.access.jobManager;
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		
		if(!(source instanceof Player)) {
			source.sendMessage(Texts.builder("You must be a player to use /jobs!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		
		Player player = (Player) source;
		String uuid = player.getUniqueId().toString();
		if(playerManager.playerExists(uuid)) {
			player.sendMessage(TextUtils.instruction("click on the job you would like to join"));
			player.sendMessage(TextUtils.error("This will reset your current level and exp for the job!"));
			for(String job : jobManager.getJobs()) {
				if(jobManager.jobExists(job)) {
					String displayName = jobManager.getDisplayName(job);
					if(!displayName.isEmpty()) {
						player.sendMessage(TextUtils.option(new Consumer<CommandSource>() {

							@Override
							public void accept(CommandSource source) {
								if(playerManager.getCurrentJob(uuid).equalsIgnoreCase(job)) {
									player.sendMessage(TextUtils.error("You are already a "+job+"!"));
									return;
								}
								player.sendMessage(TextUtils.success("Are you sure you want to become a "+displayName+"?",TextColors.GREEN));
								player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

									@Override
									public void accept(CommandSource source) {
										if(!playerManager.setJob(uuid, job)) {
											player.sendMessage(TextUtils.error("An error has occured!"));
											return;
										} else {
											player.sendMessage(TextUtils.success("You are now a "+displayName+"!",TextColors.GREEN));
											return;
										}
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
			source.sendMessage(Texts.builder("An error has occurred!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		
		return CommandResult.success();
	}
}

package me.Flibio.JobsLite.Commands;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.PlayerManager;
import me.Flibio.JobsLite.Utils.TextUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.Optional;
import java.util.function.Consumer;

public class SetCommand implements CommandExecutor{
	
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
		Optional<String> target = args.<String>getOne("target");
		if(target.isPresent()) {
			Main.access.game.getScheduler().createTaskBuilder().execute(new Runnable() {
				@Override
				public void run() {
					String targetName = target.get();
					String targetUuid = playerManager.getUUID(targetName);
					//Check if the target player exists
					if(playerManager.playerExists(targetUuid)) {
						//Send a list of jobs to the player
						player.sendMessage(TextUtils.instruction("click on the job you would like to join"));
						for(String job : jobManager.getJobs()) {
							if(jobManager.jobExists(job)) {
								String displayName = jobManager.getDisplayName(job);
								if(!displayName.isEmpty()) {
									player.sendMessage(TextUtils.option(new Consumer<CommandSource>() {

										@Override
										public void accept(CommandSource source) {
											if(playerManager.getCurrentJob(targetUuid).equalsIgnoreCase(job)) {
												player.sendMessage(TextUtils.error(targetName+" is already a "+job+"!"));
												return;
											}
											player.sendMessage(TextUtils.success("Are you sure you want to set "+targetName+"'s job to a "+displayName+"?",TextColors.GREEN));
											player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

												@Override
												public void accept(CommandSource source) {
													if(!playerManager.setJob(targetUuid, job)) {
														player.sendMessage(TextUtils.error("An error has occured!"));
														return;
													} else {
														player.sendMessage(TextUtils.success(targetName+" is now a "+displayName+"!",TextColors.GREEN));
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
						player.sendMessage(TextUtils.error("An error has occurred!"));
						return;
					}
				}
			}).async().submit(Main.access);
		} else {
			player.sendMessage(TextUtils.error("An error has occurred!"));
			return CommandResult.success();
		}
		
		return CommandResult.success();
	}
}

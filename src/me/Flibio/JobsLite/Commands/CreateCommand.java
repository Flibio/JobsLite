package me.Flibio.JobsLite.Commands;

import me.Flibio.JobsLite.Objects.CreatingJob;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class CreateCommand implements CommandExecutor{
	
	@Override
	public CommandResult execute(CommandSource source, CommandContext args)
			throws CommandException {
		
		if(!(source instanceof Player)) {
			source.sendMessage(Text.builder("You must be a player to use /jobs!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		
		new CreatingJob((Player) source);
		
		return CommandResult.success();
	}
}

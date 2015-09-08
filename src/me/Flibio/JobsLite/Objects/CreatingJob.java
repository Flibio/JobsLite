package me.Flibio.JobsLite.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.TextUtils;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Consumer;
import org.spongepowered.api.util.command.CommandSource;

import com.google.common.base.Optional;

public class CreatingJob {
	
	private enum CurrentTask {
		CANCELLED,
		JOB_NAME,
		JOB_DISPLAY_NAME,
		MAX_LEVEL,
		COLOR,
		BREAK_CLICK,
		BREAK_CURRENCY,
		BREAK_EXP,
		PLACE_CLICK,
		PLACE_CURRENCY,
		PLACE_EXP,
		OPTION_CLICK,
		COMPLETE
	}
	
	private Player player;
	private String name = "";
	private String displayName = "";
	private HashMap<BlockState,HashMap<String,Integer>> blockBreaks = new HashMap<BlockState,HashMap<String,Integer>>();
	private HashMap<BlockState,HashMap<String,Integer>> blockPlaces = new HashMap<BlockState,HashMap<String,Integer>>();
	private BlockState currentBlock;
	private int currentAmount;
	private int maxLevel = 100;
	private TextColor color = TextColors.WHITE;
	private CurrentTask currentTask = CurrentTask.CANCELLED;
	
	private JobManager jobManager;
	
	public CreatingJob(Player player) {
		this.player = player;
		jobManager = Main.access.jobManager;
		
		player.sendMessage(TextUtils.cancelText());
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.nameQuestion("name", false, false));
		currentTask = CurrentTask.JOB_NAME;
		
		Main.access.game.getEventManager().registerListeners(Main.access, this);
	}
	
	@Listener
	public void onChat(MessageSinkEvent event) {
		Optional<Player> playerOptional = event.getCause().first(Player.class);
		if(!playerOptional.isPresent()) return;
		Player eventPlayer = playerOptional.get();
		if(eventPlayer.equals(player)&&currentTask!=CurrentTask.CANCELLED) {
			if(Texts.toPlain(event.getOriginalMessage()).toLowerCase().contains("[cancel]")) {
				event.setCancelled(true);
				player.sendMessage(TextUtils.cancelled());
				name = "";
				displayName = "";
				blockBreaks = new HashMap<BlockState,HashMap<String,Integer>>();
				blockPlaces = new HashMap<BlockState,HashMap<String,Integer>>();
				currentBlock = null;
				currentAmount = 0;
				currentTask = CurrentTask.CANCELLED;
			}
		}
		if(currentTask.equals(CurrentTask.CANCELLED)) return;
		if(currentTask.equals(CurrentTask.JOB_NAME)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				name = Texts.toPlain(event.getOriginalMessage()).replaceAll(" ", "").trim();
				if(!jobManager.jobExists(name)) {
					player.sendMessage(TextUtils.registered("Name", name));
					player.sendMessage(TextUtils.line());
					player.sendMessage(TextUtils.nameQuestion("display name", true, false));
					currentTask = CurrentTask.JOB_DISPLAY_NAME;
				} else {
					player.sendMessage(TextUtils.error("That job name is already in use!"));
				}
			}
		} else if(currentTask.equals(CurrentTask.JOB_DISPLAY_NAME)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				displayName = Texts.toPlain(event.getOriginalMessage()).trim();
				player.sendMessage(TextUtils.registered("Display Name", displayName));
				player.sendMessage(TextUtils.line());
				player.sendMessage(TextUtils.nameQuestion("max level", false, true));
				currentTask = CurrentTask.MAX_LEVEL;
			}
		} else if(currentTask.equals(CurrentTask.MAX_LEVEL)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = Texts.toPlain(event.getOriginalMessage()).trim();
				int amount;
				try {
					amount = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					player.sendMessage(TextUtils.error("Please input a number!"));
					return;
				}
				if(amount<0||amount>1000000) {
					player.sendMessage(TextUtils.error("Please input a number that greater than 0 and less than 1,000,000!"));
					return;
				}
				maxLevel = amount;
				player.sendMessage(TextUtils.registered("Max-Level", maxLevel+""));
				player.sendMessage(TextUtils.line());
				colorChoices();
				currentTask = CurrentTask.COLOR;
				
			}
		} else if(currentTask.equals(CurrentTask.BREAK_CURRENCY)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = Texts.toPlain(event.getOriginalMessage()).trim();
				int amount;
				try {
					amount = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					player.sendMessage(TextUtils.error("Please input a number!"));
					return;
				}
				if(amount<0||amount>1000000) {
					player.sendMessage(TextUtils.error("Please input a number that greater than 0 and less than 1,000,000!"));
					return;
				}
				currentAmount = amount;
				player.sendMessage(TextUtils.reward("breaking", currentBlock.toString(), currentAmount+"", ""));
				player.sendMessage(TextUtils.line());
				player.sendMessage(TextUtils.howMuch("expirience"));
				currentTask = CurrentTask.BREAK_EXP;
			}
		} else if(currentTask.equals(CurrentTask.PLACE_CURRENCY)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = Texts.toPlain(event.getOriginalMessage()).trim();
				int amount;
				try {
					amount = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					player.sendMessage(TextUtils.error("Please input a number!"));
					return;
				}
				if(amount<0||amount>1000000) {
					player.sendMessage(TextUtils.error("Please input a number that greater than 0 and less than 1,000,000!"));
					return;
				}
				currentAmount = amount;
				player.sendMessage(TextUtils.reward("placing", currentBlock.toString(), currentAmount+"", ""));
				player.sendMessage(TextUtils.line());
				player.sendMessage(TextUtils.howMuch("expirience"));
				currentTask = CurrentTask.PLACE_EXP;
			}
		} else if(currentTask.equals(CurrentTask.BREAK_EXP)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = Texts.toPlain(event.getOriginalMessage()).trim();
				int amount;
				try {
					amount = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					player.sendMessage(TextUtils.error("Please input a number!"));
					return;
				}
				if(amount<0||amount>1000000) {
					player.sendMessage(TextUtils.error("Please input a number that greater than 0 and less than 1,000,000!"));
					return;
				}
				int exp = amount;
				player.sendMessage(TextUtils.reward("breaking", currentBlock.toString(), exp+"", "experience"));
				player.sendMessage(TextUtils.line());
				//Add the block
				HashMap<String,Integer> data = new HashMap<String,Integer>();
				data.put("currency", currentAmount);
				data.put("exp", exp);
				blockBreaks.put(currentBlock, data);
				currentAmount = 0;
				currentBlock = null;
				//Ask if they want more
				currentTask = CurrentTask.OPTION_CLICK;
				againBreaks();
			}
		} else if(currentTask.equals(CurrentTask.PLACE_EXP)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = Texts.toPlain(event.getOriginalMessage()).trim();
				int amount;
				try {
					amount = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					player.sendMessage(TextUtils.error("Please input a number!"));
					return;
				}
				if(amount<0||amount>1000000) {
					player.sendMessage(TextUtils.error("Please input a number that greater than 0 and less than 1,000,000!"));
					return;
				}
				int exp = amount;
				player.sendMessage(TextUtils.reward("placing", currentBlock.toString(), exp+"", "experience"));
				player.sendMessage(TextUtils.line());
				//Add the block
				HashMap<String,Integer> data = new HashMap<String,Integer>();
				data.put("currency", currentAmount);
				data.put("exp", exp);
				blockPlaces.put(currentBlock, data);
				currentAmount = 0;
				currentBlock = null;
				//Ask if they want more
				currentTask = CurrentTask.OPTION_CLICK;
				againPlaces();	
			}
		}
	}
	
	@Listener
	public void onInteractBlock(InteractBlockEvent event) {
		if(currentTask.equals(CurrentTask.CANCELLED)) return;
		Optional<Player> playerOptional = event.getCause().first(Player.class);
		if(!playerOptional.isPresent()) return;
		Player eventPlayer = playerOptional.get();
		if(eventPlayer.equals(player)&&currentTask.equals(CurrentTask.BREAK_CLICK)&&!eventPlayer.getItemInHand().isPresent()) {
			currentBlock = event.getTargetBlock().getState();
			//TODO ask if they want to ignore data
			player.sendMessage(TextUtils.howMuch("currency"));
			currentTask = CurrentTask.BREAK_CURRENCY;
		}
		if(eventPlayer.equals(player)&&currentTask.equals(CurrentTask.PLACE_CLICK)&&!eventPlayer.getItemInHand().isPresent()) {
			currentBlock = event.getTargetBlock().getState();
			//TODO ask if they want to ignore data
			player.sendMessage(TextUtils.howMuch("currency"));
			currentTask = CurrentTask.PLACE_CURRENCY;
		}
	}
	
	private void colorChoices() {
		//4x4
		ArrayList<TextColor> colors1 = new ArrayList<TextColor>(Arrays.asList(TextColors.AQUA, TextColors.BLACK, TextColors.BLUE, TextColors.DARK_AQUA));
		Text msg1 = Texts.builder().build();
		for(TextColor currentColor : colors1) {
			msg1 = msg1.builder().append(Texts.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(TextUtils.success("Set color to "+currentColor.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		ArrayList<TextColor> colors2 = new ArrayList<TextColor>(Arrays.asList(TextColors.DARK_BLUE, TextColors.DARK_GRAY, TextColors.DARK_GREEN, TextColors.DARK_PURPLE));
		Text msg2 = Texts.builder().build();
		for(TextColor currentColor : colors2) {
			msg2 = msg2.builder().append(Texts.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(TextUtils.success("Set color to "+color.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		ArrayList<TextColor> colors3 = new ArrayList<TextColor>(Arrays.asList(TextColors.DARK_RED, TextColors.GOLD, TextColors.GRAY, TextColors.GREEN));
		Text msg3 = Texts.builder().build();
		for(TextColor currentColor : colors3) {
			msg3 = msg3.builder().append(Texts.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(TextUtils.success("Set color to "+currentColor.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		ArrayList<TextColor> colors4 = new ArrayList<TextColor>(Arrays.asList(TextColors.LIGHT_PURPLE, TextColors.RED, TextColors.WHITE, TextColors.YELLOW));
		Text msg4 = Texts.builder().build();
		for(TextColor currentColor : colors4) {
			msg4 = msg4.builder().append(Texts.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(TextUtils.success("Set color to "+color.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		player.sendMessage(TextUtils.instruction("click on the color for this job"));
		player.sendMessage(msg1);
		player.sendMessage(msg2);
		player.sendMessage(msg3);
		player.sendMessage(msg4);
	}
	
	private void askAboutBreaks() {
		player.sendMessage(TextUtils.addQuestion("currency", "experience", "breaking blocks"));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(TextUtils.clickInstruction("break"));
				currentTask = CurrentTask.BREAK_CLICK;
			}
			
		}));
		player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				askAboutPlaces();
			}
			
		}));
	}
	
	private void againBreaks() {
		player.sendMessage(TextUtils.another("block"));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(TextUtils.clickInstruction("break"));
				currentTask = CurrentTask.BREAK_CLICK;
			}
			
		}));
		player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				askAboutPlaces();
			}
			
		}));
	}
	
	private void askAboutPlaces() {
		player.sendMessage(TextUtils.addQuestion("currency", "experience", "placing blocks"));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(TextUtils.clickInstruction("place"));
				currentTask = CurrentTask.PLACE_CLICK;
			}
			
		}));
		player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				complete();
			}
			
		}));
	}
	
	private void againPlaces() {
		player.sendMessage(TextUtils.another("block"));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(TextUtils.clickInstruction("place"));
				currentTask = CurrentTask.PLACE_CLICK;
			}
			
		}));
		player.sendMessage(TextUtils.noOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				complete();
			}
			
		}));
	}
	
	private void complete() {
		currentTask = CurrentTask.COMPLETE;
		if(jobManager.newJob(name, displayName, blockBreaks, blockPlaces, maxLevel, color)){
			player.sendMessage(TextUtils.saved(displayName));
		} else {
			player.sendMessage(TextUtils.error("An error occured saving "+displayName+"!"));
		}
	}
}

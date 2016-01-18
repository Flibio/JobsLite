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
package me.flibio.jobslite.objects;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.utils.JobCreationMessages;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.TextUtils;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

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
		jobManager = JobsLite.access.jobManager;
		
		player.sendMessage(JobCreationMessages.cancelNotification());
		player.sendMessage(TextUtils.line());
		player.sendMessage(JobCreationMessages.genericQuestion("What would you like the name of the job to be", "(No Spaces)"));
		currentTask = CurrentTask.JOB_NAME;
		
		JobsLite.access.game.getEventManager().registerListeners(JobsLite.access, this);
	}
	
	@Listener
	public void onChat(MessageChannelEvent.Chat event) {
		Optional<Player> playerOptional = event.getCause().first(Player.class);
		if(!playerOptional.isPresent()) return;
		Player eventPlayer = playerOptional.get();
		if(eventPlayer.equals(player)&&currentTask!=CurrentTask.CANCELLED) {
			if(event.getRawMessage().toPlain().toLowerCase().contains("[cancel]")) {
				event.setCancelled(true);
				player.sendMessage(JobCreationMessages.genericSuccess("Successfully cancelled job creation!"));
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
				name = event.getRawMessage().toPlain().replaceAll(" ", "").trim();
				if(!jobManager.jobExists(name)) {
					player.sendMessage(JobCreationMessages.genericSuccess("Successfully registered name: "+name+"!"));
					player.sendMessage(TextUtils.line());
					player.sendMessage(JobCreationMessages.genericQuestion("What would you like the display name of the job to be", "(Spaces)"));
					currentTask = CurrentTask.JOB_DISPLAY_NAME;
				} else {
					player.sendMessage(TextUtils.error("That job name is already in use!"));
				}
			}
		} else if(currentTask.equals(CurrentTask.JOB_DISPLAY_NAME)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				displayName = event.getRawMessage().toPlain().trim();
				player.sendMessage(JobCreationMessages.genericSuccess("Successfully registered display name: "+displayName+"!"));
				player.sendMessage(TextUtils.line());
				player.sendMessage(JobCreationMessages.genericQuestion("What would you like the max-level of the job to be", ""));
				currentTask = CurrentTask.MAX_LEVEL;
			}
		} else if(currentTask.equals(CurrentTask.MAX_LEVEL)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = event.getRawMessage().toPlain().trim();
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
				player.sendMessage(JobCreationMessages.genericSuccess("Successfully registered max level: "+maxLevel+"!"));
				player.sendMessage(TextUtils.line());
				colorChoices();
				currentTask = CurrentTask.COLOR;
				
			}
		} else if(currentTask.equals(CurrentTask.BREAK_CURRENCY)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = event.getRawMessage().toPlain().trim();
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
				player.sendMessage(JobCreationMessages.genericSuccess("Player will receive a base of "+currentAmount+" for breaking "+currentBlock.toString()));
				player.sendMessage(TextUtils.line());
				player.sendMessage(JobCreationMessages.genericQuestion("How much experience should breaking "+currentBlock.toString()+" give", ""));
				currentTask = CurrentTask.BREAK_EXP;
			}
		} else if(currentTask.equals(CurrentTask.PLACE_CURRENCY)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = event.getRawMessage().toPlain().trim();
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
				player.sendMessage(JobCreationMessages.genericSuccess("Player will receive a base of "+currentAmount+" for placing "+currentBlock.toString()));
				player.sendMessage(TextUtils.line());
				player.sendMessage(JobCreationMessages.genericQuestion("How much experience should placing "+currentBlock.toString()+" give", ""));
				currentTask = CurrentTask.PLACE_EXP;
			}
		} else if(currentTask.equals(CurrentTask.BREAK_EXP)) {
			if(eventPlayer.equals(player)) {
				event.setCancelled(true);
				String input = event.getRawMessage().toPlain().trim();
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
				player.sendMessage(JobCreationMessages.genericSuccess("Player will receive a reward of "+exp+" exp for breaking "+currentBlock.toString()));
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
				String input = event.getRawMessage().toPlain().trim();
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
				player.sendMessage(JobCreationMessages.genericSuccess("Player will receive a reward of "+exp+" exp for placing "+currentBlock.toString()));
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
			player.sendMessage(JobCreationMessages.genericSuccess("How much currency should breaking this block give?"));
			currentTask = CurrentTask.BREAK_CURRENCY;
		}
		if(eventPlayer.equals(player)&&currentTask.equals(CurrentTask.PLACE_CLICK)&&!eventPlayer.getItemInHand().isPresent()) {
			currentBlock = event.getTargetBlock().getState();
			//TODO ask if they want to ignore data
			player.sendMessage(JobCreationMessages.genericSuccess("How much currency should placing this block give?"));
			currentTask = CurrentTask.PLACE_CURRENCY;
		}
	}
	
	private void colorChoices() {
		//4x4
		ArrayList<TextColor> colors1 = new ArrayList<TextColor>(Arrays.asList(TextColors.AQUA, TextColors.BLACK, TextColors.BLUE, TextColors.DARK_AQUA));
		Text msg1 = Text.builder().build();
		for( TextColor currentColor : colors1) {
			msg1 = msg1.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(JobCreationMessages.genericSuccess("Set color to "+currentColor.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		ArrayList<TextColor> colors2 = new ArrayList<TextColor>(Arrays.asList(TextColors.DARK_BLUE, TextColors.DARK_GRAY, TextColors.DARK_GREEN, TextColors.DARK_PURPLE));
		Text msg2 = Text.builder().build();
		for( TextColor currentColor : colors2) {
			msg2 = msg2.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(JobCreationMessages.genericSuccess("Set color to "+color.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		ArrayList<TextColor> colors3 = new ArrayList<TextColor>(Arrays.asList(TextColors.DARK_RED, TextColors.GOLD, TextColors.GRAY, TextColors.GREEN));
		Text msg3 = Text.builder().build();
		for( TextColor currentColor : colors3) {
			msg3 = msg3.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(JobCreationMessages.genericSuccess("Set color to "+currentColor.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		ArrayList<TextColor> colors4 = new ArrayList<TextColor>(Arrays.asList(TextColors.LIGHT_PURPLE, TextColors.RED, TextColors.WHITE, TextColors.YELLOW));
		Text msg4 = Text.builder().build();
		for( TextColor currentColor : colors4) {
			msg4 = msg4.toBuilder().append(Text.builder(" ").build(), TextUtils.option(new Consumer<CommandSource>() {@Override public void accept(CommandSource source) {
							if(!currentTask.equals(CurrentTask.COLOR)) return;
							source.sendMessage(JobCreationMessages.genericSuccess("Set color to "+color.getName(), currentColor));
							source.sendMessage(TextUtils.line());
							color = currentColor;
							currentTask = CurrentTask.OPTION_CLICK;
							askAboutBreaks();
							}
					}, currentColor, currentColor.getName())).build();
		}
		player.sendMessage(JobCreationMessages.genericInstruction("click on the color you wish this job to be:"));
		player.sendMessage(msg1);
		player.sendMessage(msg2);
		player.sendMessage(msg3);
		player.sendMessage(msg4);
	}
	
	private void askAboutBreaks() {
		player.sendMessage(JobCreationMessages.genericQuestion("Would you like the player to earn currency and expirience from breaking blocks", ""));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(JobCreationMessages.genericInstruction("click on the block you want the player to earn from:"));
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
		player.sendMessage(JobCreationMessages.genericQuestion("Would you like to add another block", ""));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(JobCreationMessages.genericInstruction("click on the block you want the player to earn from:"));
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
		player.sendMessage(JobCreationMessages.genericQuestion("Would you like the player to earn currency and expirience from placing blocks", ""));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(JobCreationMessages.genericInstruction("click on the block you want the player to earn from:"));
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
		player.sendMessage(JobCreationMessages.genericQuestion("Would you like to add another block", ""));
		player.sendMessage(TextUtils.line());
		player.sendMessage(TextUtils.yesOption(new Consumer<CommandSource>() {

			@Override
			public void accept(CommandSource source) {
				if(!currentTask.equals(CurrentTask.OPTION_CLICK)) return;
				source.sendMessage(JobCreationMessages.genericInstruction("click on the block you want the player to earn from:"));
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
			player.sendMessage(JobCreationMessages.genericSuccess("Successfully registered "+displayName+" as a job!",color));
		} else {
			player.sendMessage(TextUtils.error("An error occured saving "+displayName+"!"));
		}
	}
}

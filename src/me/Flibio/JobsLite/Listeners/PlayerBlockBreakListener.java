package me.Flibio.JobsLite.Listeners;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.JobManager.ActionType;
import me.Flibio.JobsLite.Utils.NumberUtils;
import me.Flibio.JobsLite.Utils.PlayerManager;
import me.Flibio.JobsLite.Utils.TextUtils;

import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;

public class PlayerBlockBreakListener {
	
	@Subscribe
	public void onBlockBreak(PlayerBreakBlockEvent event) {
		Player player = event.getUser();
		String uuid = player.getUniqueId().toString();
		//Load managers
		PlayerManager playerManager = Main.access.playerManager;
		JobManager jobManager = Main.access.jobManager;
		
		if(playerManager.playerExists(uuid)) {
			String job = playerManager.getCurrentJob(uuid).trim();
			if(!job.isEmpty()) {
				if(jobManager.jobExists(job)) {
					String displayName = jobManager.getDisplayName(job);
					if(displayName.isEmpty()) return;
					for(String block : jobManager.getBreakBlocks(job)) {
						if(block.equalsIgnoreCase(event.getBlock().toString())||block.equalsIgnoreCase(event.getBlock().getType().getName())) {
							//Block is a match!
							//Get all variables
							//Max Level
							int maxLevel = jobManager.getMaxLevel(job);
							if(maxLevel<0) return;
							//Current Level
							int playerLevel = playerManager.getCurrentLevel(uuid, job);
							if(playerLevel<0) return;
							//Current Exp
							int playerExp = playerManager.getCurrentExp(uuid, job);
							if(playerExp<0) return;
							//Base exp reward
							int baseExpReward = jobManager.getExpReward(job, block, ActionType.BREAK);
							if(baseExpReward<0) return;
							//Base currency reward
							int baseCurrencyReward = jobManager.getCurrencyReward(job, block, ActionType.BREAK);
							if(baseCurrencyReward<0) return;
							//Get the equations
							String rewardEquation = jobManager.getRewardEquation(job);
							if(rewardEquation.isEmpty()) return;
							String rewardCurrencyEquation = rewardEquation;
							String expEquation = jobManager.getExpRequiredEquation(job);
							if(expEquation.isEmpty()) return;
							//Replace the variables in the equation
							rewardEquation = rewardEquation.replaceAll("startingPoint", baseExpReward+"").replaceAll("currentLevel", playerLevel+"");
							rewardCurrencyEquation = rewardCurrencyEquation.replaceAll("startingPoint", baseCurrencyReward+"").replaceAll("currentLevel", playerLevel+"");
							expEquation = expEquation.replaceAll("currentLevel", playerLevel+"");
							//Calculate the data
							int reward;
							int expRequired = (int) Math.round(NumberUtils.eval(expEquation));
							int currencyReward;
							if(playerLevel==0) {
								reward = baseExpReward;
								currencyReward = baseCurrencyReward;
							} else {
								reward = (int) Math.round(NumberUtils.eval(rewardEquation));
								currencyReward = (int) Math.round(NumberUtils.eval(rewardCurrencyEquation));
							}
							//Figure it out
							if(playerExp+reward>=expRequired) {
								//Player is leveling up
								if(playerLevel==maxLevel) {
									//Already at max level
									Main.access.econManager.addBalance(uuid, currencyReward);
									return;
								} else {
									if(playerLevel+1==maxLevel) {
										playerManager.setLevel(uuid, job, playerLevel+1);
										//Sound
										player.playSound(SoundTypes.LEVEL_UP, player.getLocation().getPosition(), 1);
										Main.access.econManager.addBalance(uuid, currencyReward);
										player.sendMessage(TextUtils.levelUp(player.getName(), playerLevel+1, displayName));
										//Tell them they are now at the max level
										player.sendMessage(TextUtils.maxLevel(displayName));
									} else {
										int expLeft = reward-(expRequired - playerExp);
										playerManager.setExp(uuid, job, expLeft);
										playerManager.setLevel(uuid, job, playerLevel+1);
										//Sound
										player.playSound(SoundTypes.LEVEL_UP, player.getLocation().getPosition(), 1);
										Main.access.econManager.addBalance(uuid, currencyReward);
										player.sendMessage(TextUtils.levelUp(player.getName(), playerLevel+1, displayName));
										//Tell them their new statistics
										String newExpEq = expEquation.replaceAll("currentLevel", (playerLevel+2)+"");
										int newExp = (int) Math.round(NumberUtils.eval(newExpEq));
										player.sendMessage(TextUtils.toGo(newExp-expLeft, playerLevel+2, displayName));
									}
								}
							} else {
								//Player isn't leveling up
								Main.access.econManager.addBalance(uuid, currencyReward);
								//If the player is at the max level don't give them exp
								if(maxLevel==playerLevel) return;
								playerManager.setExp(uuid, job, playerExp+reward);
							}
						}
					}
				}
			}
		}
	}
	
}

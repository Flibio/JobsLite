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
package me.flibio.jobslite;

import me.flibio.jobslite.commands.CreateCommand;
import me.flibio.jobslite.commands.JoinCommand;
import me.flibio.jobslite.commands.SetCommand;
import me.flibio.jobslite.listeners.PlayerBlockBreakListener;
import me.flibio.jobslite.listeners.PlayerChatListener;
import me.flibio.jobslite.listeners.PlayerJoinListener;
import me.flibio.jobslite.listeners.PlayerPlaceBlockListener;
import me.flibio.jobslite.utils.FileManager;
import me.flibio.jobslite.utils.HttpUtils;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.JsonUtils;
import me.flibio.jobslite.utils.PlayerManager;
import me.flibio.jobslite.utils.FileManager.FileType;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.HashMap;

import static me.flibio.jobslite.PluginInfo.*;

@Plugin(id = ID, name = NAME, version = VERSION, dependencies = DEPENDENCIES)
public class JobsLite {
	
	public static JobsLite access;
	
	@Inject
	public Logger logger;
	
	@Inject
	public Game game;
	
	public String version = JobsLite.class.getAnnotation(Plugin.class).version();
	
	public FileManager fileManager;
	public JobManager jobManager;
	public PlayerManager playerManager;
	public EconomyService economyService;
	
	private HttpUtils httpUtils;
	
	private static HashMap<String, String> configOptions = new HashMap<String, String>();
	
	private boolean foundProvider = false;
	private boolean late = false;
	
	@Listener
	public void onPreInitialize(GamePreInitializationEvent event) {
		access = this;
		//Initialze basic plugin managers needed for further initialization
		fileManager = new FileManager(logger);
		jobManager = new JobManager();
		playerManager = new PlayerManager(logger);
	}
	
	@Listener
	public void onServerInitialize(GameInitializationEvent event) {
		logger.info("JobsLite by Flibio initializing!");
		fileManager.loadFile(FileType.CONFIGURATION);
		fileManager.loadFile(FileType.PLAYER_DATA);
		fileManager.loadFile(FileType.JOBS_DATA);	
		
		initializeFiles();
		loadConfigurationOptions();
	}
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if(event.getService().equals(EconomyService.class)&&!late) {
			Object raw = event.getNewProviderRegistration().getProvider();
			if(raw instanceof EconomyService) {
				foundProvider = true;
				economyService = (EconomyService) raw;
			} else {
				foundProvider = false;
			}
		}
	}
	
	@Listener
	public void onPostInitialization(GamePostInitializationEvent event) {
		httpUtils = new HttpUtils();
		if(foundProvider) {
			//Register events and commands
			registerEvents();
			registerCommands();
			//Schedule async task to auto-save files
			game.getScheduler().createTaskBuilder().execute(new Runnable() {
				public void run() {
					//Save all of the files
					fileManager.saveAllFiles();
				}
			}).async().delayTicks(10000).intervalTicks(5000).submit(this);
			
			//Plugin Metrics
			try {
				JobsLiteMetrics metrics = new JobsLiteMetrics();
				if (!metrics.isOptOut()&&optionEnabled("pluginMetrics")) {
					logger.info("PluginMetrics enabled!");
					metrics.start();
				} else {
					logger.info("PluginMetrics disabled!");
				}
			} catch (IOException e) {
				logger.error("Error enabling plugin metrics!");
			}
		}
	}
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		late = true;
		if(!foundProvider) {
			logger.error("JobsLite failed to load an economy plugin!");
			logger.error("It will no longer function!");
			return;
		}
		if(!optionEnabled("updateNotifications")) return;
		game.getScheduler().createTaskBuilder().execute(new Runnable() {
			public void run() {
				//Check for an update
				JsonUtils jsonUtils = new JsonUtils();
				//Check for an update
				String latest = httpUtils.requestData("https://api.github.com/repos/Flibio/JobsLite/releases/latest");
				if(latest.isEmpty()) return;
				String version = jsonUtils.getVersion(latest).replace("v", "");
				String changes = httpUtils.requestData("https://flibio.github.io/JobsLite/changelogs/"+version.replaceAll("\\.", "-")+".txt");
				String[] iChanges = changes.split(";");
				String url = jsonUtils.getUrl(latest);
				boolean prerelease = jsonUtils.isPreRelease(latest);
				//Make sure the latest update is not a prerelease
				if(!prerelease) {
					//Check if the latest update is newer than the current one
					String currentVersion = JobsLite.access.version;
					if(jsonUtils.versionCompare(version, currentVersion)>0) {
						logger.info("JobsLite v"+version+" is now available to download!");
						logger.info(url);
						for(String change : iChanges) {
							if(!change.trim().isEmpty()) {
								logger.info("+ "+change);
							}
						}
					}
				}
			}
		}).async().submit(this);
	}
	
	@Listener
	public void serverStop(GameStoppingServerEvent event) {
		if(!foundProvider) return;
		fileManager.saveAllFiles();
	}
	
	@Listener
	public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
		if(!foundProvider) return;
		fileManager.saveAllFiles();
	}
	
	private void registerEvents() {
		game.getEventManager().registerListeners(this, new PlayerChatListener());
		game.getEventManager().registerListeners(this, new PlayerJoinListener());
		game.getEventManager().registerListeners(this, new PlayerBlockBreakListener());
		game.getEventManager().registerListeners(this, new PlayerPlaceBlockListener());
	}
	
	private void initializeFiles() {
		fileManager.generateFolder("config/JobsLite");
		fileManager.generateFile("config/JobsLite/config.conf");
		fileManager.generateFile("config/JobsLite/playerData.conf");
		fileManager.generateFile("config/JobsLite/jobsData.conf");
		
		fileManager.loadFile(FileType.CONFIGURATION);
		
		fileManager.testDefault("Display-Level", "enabled");
		fileManager.testDefault("Chat-Prefixes", "enabled");
		fileManager.testDefault("Plugin-Metrics", "enabled");
		fileManager.testDefault("Update-Notifications", "enabled");
	}
	
	private void loadConfigurationOptions() {
		configOptions.put("displayLevel", fileManager.getConfigValue("Display-Level"));
		configOptions.put("chatPrefixes", fileManager.getConfigValue("Chat-Prefixes"));
		configOptions.put("pluginMetrics", fileManager.getConfigValue("Plugin-Metrics"));
		configOptions.put("updateNotifications", fileManager.getConfigValue("Update-Notifications"));
	}
	
	private void registerCommands() {
		CommandSpec createCommand = CommandSpec.builder()
		    .description(Text.of("Create a new job"))
		    .permission("jobs.admin.create")
		    .executor(new CreateCommand())
		    .build();
		CommandSpec joinCommand = CommandSpec.builder()
		    .description(Text.of("Join a job"))
		    .permission("jobs.join")
		    .executor(new JoinCommand())
		    .build();
		CommandSpec setCommand = CommandSpec.builder()
			    .description(Text.of("Set a player's job"))
			    .permission("jobs.set")
			     .arguments(GenericArguments.string(Text.of("target")))
			    .executor(new SetCommand())
			    .build();
		CommandSpec jobsCommand = CommandSpec.builder()
		    .description(Text.of("Jobs commands"))
		    .child(createCommand, "create")
		    .child(joinCommand, "join")
		    .child(setCommand, "set")
		    .build();
		game.getCommandManager().register(this, jobsCommand, "jobs");
	}
	
	public static boolean optionEnabled(String optionName) {
		if(configOptions.get(optionName).equalsIgnoreCase("enabled")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getOption(String optionName) {
		if(!configOptions.containsKey(optionName)) return "";
		return configOptions.get(optionName);
	}

}

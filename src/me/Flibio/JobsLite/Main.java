package me.Flibio.JobsLite;

import java.io.IOException;
import java.util.HashMap;

import me.Flibio.EconomyLite.API.EconomyLiteAPI;
import me.Flibio.JobsLite.Commands.CreateCommand;
import me.Flibio.JobsLite.Commands.JoinCommand;
import me.Flibio.JobsLite.Listeners.PlayerBlockBreakListener;
import me.Flibio.JobsLite.Listeners.PlayerChatListener;
import me.Flibio.JobsLite.Listeners.PlayerJoinListener;
import me.Flibio.JobsLite.Listeners.PlayerPlaceBlockListener;
import me.Flibio.JobsLite.Utils.EconManager;
import me.Flibio.JobsLite.Utils.EconManager.EconType;
import me.Flibio.JobsLite.Utils.FileManager;
import me.Flibio.JobsLite.Utils.FileManager.FileType;
import me.Flibio.JobsLite.Utils.HttpUtils;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.JsonUtils;
import me.Flibio.JobsLite.Utils.PlayerManager;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.PostInitializationEvent;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.spec.CommandSpec;

import com.erigitic.service.TEService;
import com.google.common.base.Optional;
import com.google.inject.Inject;

@Plugin(id = "JobsLite", name = "JobsLite", version = "1.0.0", dependencies = "after:EconomyLite;after:TotalEconomy")
public class Main {
	
	public static Main access;
	
	@Inject
	public Logger logger;
	
	@Inject
	public Game game;
	
	public String version = Main.class.getAnnotation(Plugin.class).version();
	
	public FileManager fileManager;
	public JobManager jobManager;
	public PlayerManager playerManager;
	public EconManager econManager;
	
	private static HashMap<String, String> configOptions = new HashMap<String, String>();
	
	@Subscribe
	public void onServerInitialize(InitializationEvent event) {
		logger.info("JobsLite by Flibio initializing!");
		//Set the access
		access = this;
		
		fileManager = new FileManager();
		fileManager.loadFile(FileType.CONFIGURATION);
		fileManager.loadFile(FileType.PLAYER_DATA);
		fileManager.loadFile(FileType.JOBS_DATA);
		jobManager = new JobManager();
		playerManager = new PlayerManager();
		econManager = new EconManager();
		
		initializeFiles();
		loadConfigurationOptions();
	}
	
	@Subscribe
	public void onPostInitialization(PostInitializationEvent event) {
		if(game.getPluginManager().getPlugin("EconomyLite").isPresent()&&game.getPluginManager().getPlugin("TotalEconomy").isPresent()) {
			logger.error("You have two economy plugins installed!... JobsLite will not function!");
			return;
		}
		if(!game.getPluginManager().getPlugin("EconomyLite").isPresent()&&!game.getPluginManager().getPlugin("TotalEconomy").isPresent()) {
			//Disable plugin
			logger.error("You have no economy plugins installed!... JobsLite will not function!");
			return;
		}
		if(game.getPluginManager().getPlugin("EconomyLite").isPresent()) {
			Optional<EconomyLiteAPI> service = Main.access.game.getServiceManager().provide(EconomyLiteAPI.class);
			if(service.isPresent()) {
				EconomyLiteAPI economyLite = service.get();
				econManager.initialize(EconType.ECONOMY_LITE, economyLite, null);
				logger.info("Using EconomyLite as an economy!");
			} else {
				//Disable plugin
				logger.error("Could not load an Economy API... JobsLite will not function!");
				return;
			}
		} else if(game.getPluginManager().getPlugin("TotalEconomy").isPresent()) {
			Optional<TEService> service = Main.access.game.getServiceManager().provide(TEService.class);
			if(service.isPresent()) {
				TEService totalEconomy = service.get();
				econManager.initialize(EconType.TOTAL_ECONOMY, null, totalEconomy);
				logger.info("Using TotalEconomy as an economy!");
			} else {
				//Disable plugin
				logger.error("Could not load an Economy API... JobsLite will not function!");
				return;
			}
		} else {
			//Disable plugin
			logger.error("Error loading economy plugins... JobsLite will not function!");
			return;
		}
		
		registerEvents();
		registerCommands();
		
		//Schedule async task to auto-save files
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {}
				while(true) {
					//Save all of the files
					fileManager.saveAllFiles();
					try {
						//Sleep for 5 seconds (5000 milliseconds)
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
				}
			}
		});
		thread.start();
		
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
	
	@Subscribe
	public void onServerStart(ServerStartedEvent event) {
		if(!optionEnabled("updateNotifications")) return;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				//Check for an update
				JsonUtils jsonUtils = new JsonUtils();
				//Check for an update
				String latest = HttpUtils.requestData("https://api.github.com/repos/Flibio/JobsLite/releases/latest");
				if(latest.isEmpty()) return;
				String version = jsonUtils.getVersion(latest).replace("v", "");
				String changes = HttpUtils.requestData("https://flibio.github.io/JobsLite/changelogs/"+version.replaceAll("\\.", "-")+".txt");
				String[] iChanges = changes.split(";");
				String url = jsonUtils.getUrl(latest);
				boolean prerelease = jsonUtils.isPreRelease(latest);
				//Make sure the latest update is not a prerelease
				if(!prerelease) {
					//Check if the latest update is newer than the current one
					String currentVersion = Main.access.version;
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
		});
		thread.start();
	}
	
	@Subscribe
	public void serverStop(ServerStoppingEvent event) {
		fileManager.saveAllFiles();
	}
	
	@Subscribe
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		fileManager.saveAllFiles();
	}
	
	private void registerEvents() {
		game.getEventManager().register(this, new PlayerChatListener());
		game.getEventManager().register(this, new PlayerJoinListener());
		game.getEventManager().register(this, new PlayerBlockBreakListener());
		game.getEventManager().register(this, new PlayerPlaceBlockListener());
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
		    .description(Texts.of("Create a new job"))
		    .permission("jobs.admin.create")
		    .executor(new CreateCommand())
		    .build();
		CommandSpec joinCommand = CommandSpec.builder()
		    .description(Texts.of("Join a job"))
		    .permission("jobs.join")
		    .executor(new JoinCommand())
		    .build();
		CommandSpec jobsCommand = CommandSpec.builder()
		    .description(Texts.of("Jobs commands"))
		    .child(createCommand, "create")
		    .child(joinCommand, "join")
		    .build();
		game.getCommandDispatcher().register(this, jobsCommand, "jobs");
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

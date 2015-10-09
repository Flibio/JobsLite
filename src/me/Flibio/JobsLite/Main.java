package me.Flibio.JobsLite;

import me.Flibio.EconomyLite.API.EconomyLiteAPI;
import me.Flibio.JobsLite.Commands.CreateCommand;
import me.Flibio.JobsLite.Commands.JoinCommand;
import me.Flibio.JobsLite.Commands.SetCommand;
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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;

import com.erigitic.service.TEService;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Plugin(id = "JobsLite", name = "JobsLite", version = "1.1.0", dependencies = "after:EconomyLite;after:TotalEconomy")
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
	
	@Listener
	public void onServerInitialize(GameInitializationEvent event) {
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
	
	@Listener
	public void onPostInitialization(GamePostInitializationEvent event) {
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
		game.getScheduler().createTaskBuilder().execute(new Runnable() {
			public void run() {
				//Save all of the files
				fileManager.saveAllFiles();
			}
		}).async().delay(10000).interval(5000).submit(this);
		
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
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		if(!optionEnabled("updateNotifications")) return;
		game.getScheduler().createTaskBuilder().execute(new Runnable() {
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
		}).async().submit(this);
	}
	
	@Listener
	public void serverStop(GameStoppingServerEvent event) {
		fileManager.saveAllFiles();
	}
	
	@Listener
	public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
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
		    .description(Texts.of("Create a new job"))
		    .permission("jobs.admin.create")
		    .executor(new CreateCommand())
		    .build();
		CommandSpec joinCommand = CommandSpec.builder()
		    .description(Texts.of("Join a job"))
		    .permission("jobs.join")
		    .executor(new JoinCommand())
		    .build();
		CommandSpec setCommand = CommandSpec.builder()
			    .description(Texts.of("Set a player's job"))
			    .permission("jobs.set")
			     .arguments(GenericArguments.string(Texts.of("target")))
			    .executor(new SetCommand())
			    .build();
		CommandSpec jobsCommand = CommandSpec.builder()
		    .description(Texts.of("Jobs commands"))
		    .child(createCommand, "create")
		    .child(joinCommand, "join")
		    .child(setCommand, "set")
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

/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2017 Flibio
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

import static me.flibio.jobslite.PluginInfo.DESCRIPTION;
import static me.flibio.jobslite.PluginInfo.ID;
import static me.flibio.jobslite.PluginInfo.NAME;
import static me.flibio.jobslite.PluginInfo.VERSION;

import com.google.inject.Inject;
import io.github.flibio.utils.commands.CommandLoader;
import io.github.flibio.utils.file.FileManager;
import io.github.flibio.utils.message.MessageStorage;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.PlayerManager;
import me.flibio.jobslite.bstats.Metrics;
import me.flibio.jobslite.commands.AddCommand;
import me.flibio.jobslite.commands.CreateCommand;
import me.flibio.jobslite.commands.DeleteCommand;
import me.flibio.jobslite.commands.InfoCommand;
import me.flibio.jobslite.commands.JobsCommand;
import me.flibio.jobslite.commands.JoinCommand;
import me.flibio.jobslite.commands.LeaveCommand;
import me.flibio.jobslite.commands.RemoveCommand;
import me.flibio.jobslite.data.ImmutableJobData;
import me.flibio.jobslite.data.ImmutableSignJobData;
import me.flibio.jobslite.data.JobData;
import me.flibio.jobslite.data.JobDataManipulatorBuilder;
import me.flibio.jobslite.data.SignJobData;
import me.flibio.jobslite.data.SignJobDataManipulatorBuilder;
import me.flibio.jobslite.impl.LocalJobManager;
import me.flibio.jobslite.impl.LocalPlayerManager;
import me.flibio.jobslite.impl.RemoteJobManager;
import me.flibio.jobslite.impl.RemotePlayerManager;
import me.flibio.jobslite.listeners.MobDeathListener;
import me.flibio.jobslite.listeners.PlayerBlockBreakListener;
import me.flibio.jobslite.listeners.PlayerJoinListener;
import me.flibio.jobslite.listeners.PlayerPlaceBlockListener;
import me.flibio.jobslite.listeners.SignListeners;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("deprecation")
@Plugin(id = ID, name = NAME, version = VERSION, dependencies = {}, description = DESCRIPTION)
public class JobsLite {

    private static JobsLite instance;

    @Inject @ConfigDir(sharedRoot = false) Path configDir;

    @SuppressWarnings("unused") @Inject private Metrics metrics;

    @Inject public Logger logger;

    @Inject public Game game;

    public String version = JobsLite.class.getAnnotation(Plugin.class).version();

    private static FileManager fileManager;
    private static JobManager jobManager;
    private static PlayerManager playerManager;
    private static EconomyService economyService;
    private static MessageStorage messageStorage;
    public static List<UUID> msgCache = new ArrayList<>();

    private boolean foundProvider = false;
    private boolean late = false;

    @Listener
    public void onPreInitialize(GamePreInitializationEvent event) {
        instance = this;
        // Register the custom data
        Sponge.getDataManager().register(JobData.class, ImmutableJobData.class, new JobDataManipulatorBuilder());
        Sponge.getDataManager().register(SignJobData.class, ImmutableSignJobData.class, new SignJobDataManipulatorBuilder());
        // Initialze basic plugin managers needed for further initialization
        if (new File(configDir.toString()).isAbsolute()) {
            fileManager = FileManager.createInstance(this, configDir.toString());
        } else {
            fileManager = FileManager.createInstance(this, "./" + configDir.toString());
        }

        messageStorage = MessageStorage.createInstance(this, configDir.toString());
        messageStorage.defaultMessages("jobslitemessages");
    }

    @Listener
    public void onServerInitialize(GameInitializationEvent event) {
        logger.info("JobsLite " + version + " by Flibio initializing!");
        initializeFiles();
        // Initialize the player and job managers
        boolean flatfile = true;
        if (optionEnabled("mysql.enabled")) {
            // Load the remote managers
            RemotePlayerManager tempPlayerManager =
                    new RemotePlayerManager(getOption("mysql.hostname"), getOption("mysql.port"), getOption("mysql.database"),
                            getOption("mysql.username"), getOption("mysql.password"));
            RemoteJobManager tempJobManager =
                    new RemoteJobManager(getOption("mysql.hostname"), getOption("mysql.port"), getOption("mysql.database"),
                            getOption("mysql.username"), getOption("mysql.password"));
            if (tempPlayerManager.isWorking() && tempJobManager.isWorking()) {
                playerManager = tempPlayerManager;
                jobManager = tempJobManager;
                flatfile = false;
            }
        }
        if (flatfile) {
            // Load the local managers
            playerManager = new LocalPlayerManager();
            jobManager = new LocalJobManager();
        }
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class) && !late) {
            Object raw = event.getNewProviderRegistration().getProvider();
            if (raw instanceof EconomyService) {
                foundProvider = true;
                economyService = (EconomyService) raw;
            } else {
                foundProvider = false;
            }
        }
    }

    @Listener
    public void onPostInitialization(GamePostInitializationEvent event) {
        if (foundProvider) {
            // Register events and commands
            registerEvents();
            registerCommands();
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        late = true;
        if (!foundProvider) {
            logger.error("JobsLite failed to load an economy plugin!");
            logger.error("It will no longer function!");
            return;
        }
    }

    @Listener
    public void reload(GameReloadEvent event) {
        // Force a reload of the configuration file
        fileManager.reloadFile("config.conf");
        messageStorage.reloadMessages();
    }

    private void registerEvents() {
        game.getEventManager().registerListeners(this, new PlayerJoinListener());
        game.getEventManager().registerListeners(this, new PlayerBlockBreakListener());
        game.getEventManager().registerListeners(this, new PlayerPlaceBlockListener());
        game.getEventManager().registerListeners(this, new MobDeathListener());
        game.getEventManager().registerListeners(this, new SignListeners());
    }

    private void initializeFiles() {
        fileManager.setDefault("config.conf", "max-jobs", Integer.class, 1);
        fileManager.setDefault("config.conf", "virtual-draw", String.class, "none");
        fileManager.setDefault("config.conf", "mysql.enabled", Boolean.class, false);
        fileManager.setDefault("config.conf", "mysql.hostname", String.class, "host");
        fileManager.setDefault("config.conf", "mysql.port", Integer.class, 3306);
        fileManager.setDefault("config.conf", "mysql.database", String.class, "database");
        fileManager.setDefault("config.conf", "mysql.username", String.class, "username");
        fileManager.setDefault("config.conf", "mysql.password", String.class, "password");
    }

    private void registerCommands() {
        CommandLoader.registerCommands(this, TextSerializers.FORMATTING_CODE.serialize(messageStorage.getMessage("command.invalidsource")),
                new JobsCommand(),
                new CreateCommand(),
                new DeleteCommand(),
                new JoinCommand(),
                new AddCommand(),
                new RemoveCommand(),
                new LeaveCommand(),
                new InfoCommand()
                );
    }

    // Static Methods

    public static JobsLite getInstance() {
        return instance;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public static JobManager getJobManager() {
        return jobManager;
    }

    public static PlayerManager getPlayerManager() {
        return playerManager;
    }

    public static EconomyService getEconomyService() {
        return economyService;
    }

    public static MessageStorage getMessageStorage() {
        return messageStorage;
    }

    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    }

    public static String getCurrent() {
        return getFormatter().parse(new Date().toString()).toString();
    }

    public static boolean optionEnabled(String optionName) {
        Optional<String> value = fileManager.getValue("config.conf", optionName, String.class);
        if (value.isPresent()) {
            return value.get().equalsIgnoreCase("enabled") || value.get().equalsIgnoreCase("true");
        }
        return false;
    }

    public static String getOption(String optionName) {
        Optional<String> value = fileManager.getValue("config.conf", optionName, String.class);
        return value.isPresent() ? value.get() : "";
    }
}

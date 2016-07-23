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

import static me.flibio.jobslite.PluginInfo.DESCRIPTION;
import static me.flibio.jobslite.PluginInfo.ID;
import static me.flibio.jobslite.PluginInfo.NAME;
import static me.flibio.jobslite.PluginInfo.VERSION;
import com.google.inject.Inject;
import io.github.flibio.utils.commands.CommandLoader;
import io.github.flibio.utils.file.ConfigManager;
import me.flibio.jobslite.commands.CreateCommand;
import me.flibio.jobslite.commands.DeleteCommand;
import me.flibio.jobslite.commands.JobsCommand;
import me.flibio.jobslite.commands.JoinCommand;
import me.flibio.jobslite.commands.SetCommand;
import me.flibio.jobslite.data.ImmutableJobData;
import me.flibio.jobslite.data.ImmutableSignJobData;
import me.flibio.jobslite.data.JobData;
import me.flibio.jobslite.data.JobDataManipulatorBuilder;
import me.flibio.jobslite.data.SignJobData;
import me.flibio.jobslite.data.SignJobDataManipulatorBuilder;
import me.flibio.jobslite.listeners.PlayerBlockBreakListener;
import me.flibio.jobslite.listeners.PlayerChatListener;
import me.flibio.jobslite.listeners.PlayerJoinListener;
import me.flibio.jobslite.listeners.PlayerPlaceBlockListener;
import me.flibio.jobslite.listeners.SignListeners;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.PlayerManager;
import me.flibio.updatifier.Updatifier;
import net.minecrell.mcstats.SpongeStatsLite;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("deprecation")
@Updatifier(repoName = "JobsLite", repoOwner = "Flibio", version = "v" + VERSION)
@Plugin(id = ID, name = NAME, version = VERSION, dependencies = {}, description = DESCRIPTION)
public class JobsLite {

    public static JobsLite access;

    @Inject public Logger logger;

    @Inject public Game game;

    @Inject private SpongeStatsLite statsLite;

    public String version = JobsLite.class.getAnnotation(Plugin.class).version();

    public ConfigManager configManager;
    public JobManager jobManager;
    public PlayerManager playerManager;
    public EconomyService economyService;

    private static HashMap<String, String> configOptions = new HashMap<String, String>();

    private boolean foundProvider = false;
    private boolean late = false;

    @Listener
    public void onPreInitialize(GamePreInitializationEvent event) {
        access = this;
        // Register the custom data
        Sponge.getDataManager().register(JobData.class, ImmutableJobData.class, new JobDataManipulatorBuilder());
        Sponge.getDataManager().register(SignJobData.class, ImmutableSignJobData.class, new SignJobDataManipulatorBuilder());
        // Initialze basic plugin managers needed for further initialization
        configManager = ConfigManager.createInstance(this).get();
        jobManager = new JobManager();
        playerManager = new PlayerManager();
    }

    @Listener
    public void onServerInitialize(GameInitializationEvent event) {
        logger.info("JobsLite by Flibio initializing!");
        this.statsLite.start();
        initializeFiles();
        loadConfigurationOptions();
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

    private void registerEvents() {
        game.getEventManager().registerListeners(this, new PlayerChatListener());
        game.getEventManager().registerListeners(this, new PlayerJoinListener());
        game.getEventManager().registerListeners(this, new PlayerBlockBreakListener());
        game.getEventManager().registerListeners(this, new PlayerPlaceBlockListener());
        game.getEventManager().registerListeners(this, new SignListeners());
    }

    private void initializeFiles() {
        configManager.setDefault("config.conf", "Display-Level", String.class, "enabled");
        configManager.setDefault("config.conf", "Chat-Prefixes", String.class, "enabled");

        configManager.getFile("playerjobdata.conf");
    }

    private void loadConfigurationOptions() {
        Optional<String> lOpt = configManager.getValue("config.conf", "Display-Level", String.class);
        Optional<String> pOpt = configManager.getValue("config.conf", "Chat-Prefixes", String.class);
        if (!lOpt.isPresent()) {
            logger.error("Error loading display level option!");
            configOptions.put("displayLevel", "enabled");
        } else {
            configOptions.put("displayLevel", lOpt.get());
        }
        if (!pOpt.isPresent()) {
            logger.error("Error loading chat prefix option!");
            configOptions.put("chatPrefixes", "enabled");
        } else {
            configOptions.put("chatPrefixes", pOpt.get());
        }
    }

    private void registerCommands() {
        CommandLoader.registerCommands(this, "REPLACE ME",
                new JobsCommand(),
                new CreateCommand(),
                new DeleteCommand(),
                new JoinCommand(),
                new SetCommand());
    }

    public static boolean optionEnabled(String optionName) {
        if (configOptions.get(optionName).equalsIgnoreCase("enabled")) {
            return true;
        } else {
            return false;
        }
    }

    public static String getOption(String optionName) {
        if (!configOptions.containsKey(optionName))
            return "";
        return configOptions.get(optionName);
    }

}

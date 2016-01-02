package me.Flibio.JobsLite;

import org.spongepowered.api.Game;

import java.io.File;
import java.io.IOException;


public class JobsLiteMetrics extends Metrics {

    private Game game = JobsLite.access.game;

    public JobsLiteMetrics() throws IOException {
        super("JobsLite", JobsLite.access.version);
    }

    @Override
    public String getFullServerVersion() {
        return "1.8";
    }

    @Override
    public int getPlayersOnline() {
        return game.getServer().getOnlinePlayers().size();
    }

    @Override
    public File getConfigFile() {
        return new File("config/JobsLite/metrics.yml");
    }
}
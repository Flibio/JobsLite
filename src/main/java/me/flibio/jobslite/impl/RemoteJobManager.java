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
package me.flibio.jobslite.impl;

import io.github.flibio.utils.sql.RemoteSqlManager;
import io.github.flibio.utils.sql.SqlManager;
import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.Job;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.Reward;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.format.TextColor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class RemoteJobManager implements JobManager {

    private SqlManager manager;

    public RemoteJobManager(String hostname, String port, String database, String username, String password) {
        manager = RemoteSqlManager.createInstance(JobsLite.getInstance(), hostname, port, database, username, password).get();
        if (manager.initialTestConnection()) {
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS jobslitejobs(id VARCHAR(1024), name VARCHAR(1024), maxlevel INT(11), color VARCHAR(1024)"
                    + ", silktouch BOOLEAN, worldgen BOOLEAN, ignoredata BOOLEAN, expeq VARCHAR(1024), reweq VARCHAR(1024))");
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS jobslitebreaks(job VARCHAR(1024), type VARCHAR(1024), cur DOUBLE, exp DOUBLE)");
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS jobsliteplaces(job VARCHAR(1024), type VARCHAR(1024), cur DOUBLE, exp DOUBLE)");
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS jobslitekills(job VARCHAR(1024), type VARCHAR(1024), cur DOUBLE, exp DOUBLE)");
        }
    }

    public boolean isWorking() {
        return manager.testConnection();
    }

    @Override
    public boolean jobExists(String job) {
        return manager.queryExists("SELECT id FROM jobslitejobs WHERE id = ?", job);
    }

    @Override
    public Optional<Job> getJob(String job) {
        if (jobExists(job)) {
            Optional<ResultSet> oRs = manager.executeQuery("SELECT * FROM jobslitejobs WHERE id = ?", job);
            Optional<ResultSet> bRs = manager.executeQuery("SELECT * FROM jobslitebreaks WHERE job = ?", job);
            Optional<ResultSet> pRs = manager.executeQuery("SELECT * FROM jobsliteplaces WHERE job = ?", job);
            Optional<ResultSet> kRs = manager.executeQuery("SELECT * FROM jobslitekills WHERE job = ?", job);
            if (oRs.isPresent() && bRs.isPresent() && pRs.isPresent() && kRs.isPresent()) {
                ResultSet rs = oRs.get();
                ResultSet brs = bRs.get();
                ResultSet prs = pRs.get();
                ResultSet krs = kRs.get();
                try {
                    rs.first();
                    String name = rs.getString("name");
                    int maxLevel = rs.getInt("maxlevel");
                    String color = rs.getString("color");
                    boolean silkTouch = rs.getBoolean("silktouch");
                    boolean worldGen = rs.getBoolean("worldgen");
                    boolean ignoreData = rs.getBoolean("ignoredata");
                    String expEq = rs.getString("expeq");
                    String rewEq = rs.getString("reweq");
                    Map<String, Reward> blockBreaks = new HashMap<>();
                    Map<String, Reward> blockPlaces = new HashMap<>();
                    Map<String, Reward> mobKills = new HashMap<>();

                    while (brs.next()) {
                        blockBreaks.put(brs.getString("type"), new Reward(brs.getDouble("cur"), brs.getDouble("exp")));
                    }
                    while (prs.next()) {
                        blockPlaces.put(prs.getString("type"), new Reward(prs.getDouble("cur"), prs.getDouble("exp")));
                    }
                    while (krs.next()) {
                        mobKills.put(krs.getString("type"), new Reward(krs.getDouble("cur"), krs.getDouble("exp")));
                    }

                    // Finalize
                    Optional<TextColor> tOpt = Sponge.getRegistry().getType(TextColor.class, color);
                    if (tOpt.isPresent()) {
                        return Optional.of(new Job(job, name, maxLevel, tOpt.get(), silkTouch, worldGen, ignoreData, expEq, rewEq, blockBreaks,
                                blockPlaces, mobKills));
                    }

                } catch (Exception e) {
                    JobsLite.getInstance().logger.error(e.getMessage());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Job> getJobs() {
        List<Job> jobs = new ArrayList<>();
        List<String> jobStrings = manager.queryTypeList("id", String.class, "SELECT id FROM jobslitejobs");
        jobStrings.forEach(jobString -> {
            Optional<Job> jOpt = getJob(jobString);
            if (jOpt.isPresent())
                jobs.add(jOpt.get());
        });
        return jobs;
    }

    @Override
    public boolean createJob(String id, String displayName, int maxLevel, TextColor textColor, boolean silkTouch, boolean worldGen,
            boolean ignoreData, HashMap<BlockState, Reward> blockBreaks, HashMap<BlockState, Reward> blockPlaces,
            HashMap<EntityType, Reward> mobKills) {
        if (!jobExists(id)) {
            if (!manager.executeUpdate(
                    "INSERT INTO jobslitejobs (`id`, `name`, `maxlevel`, `color`, `silktouch`, `worldgen`, `ignoredata`, `expeq`, "
                            + "`reweq`) VALUES (?,?,?,?,?,?,?,?,?)", id, displayName, Integer.toString(maxLevel), textColor.toString(),
                    Integer.toString((silkTouch) ? 1 : 0), Integer.toString((worldGen) ? 1 : 0), Integer.toString((ignoreData) ? 1 : 0),
                    "100*((1.12)^((currentLevel-1)*1.88))", "startingPoint*((1.048)^(currentLevel-1))"))
                return false;
            for (Entry<BlockState, Reward> entry : blockBreaks.entrySet()) {
                Reward r = entry.getValue();
                if (!manager.executeUpdate("INSERT INTO jobslitebreaks (`job`, `type`, `cur`, `exp`) VALUES (?,?,?,?)", id, entry.getKey()
                        .toString(), Double.toString(r.getCurrency()), Double.toString(r.getExp())))
                    return false;
            }
            for (Entry<BlockState, Reward> entry : blockPlaces.entrySet()) {
                Reward r = entry.getValue();
                if (!manager.executeUpdate("INSERT INTO jobsliteplaces (`job`, `type`, `cur`, `exp`) VALUES (?,?,?,?)", id, entry.getKey()
                        .toString(), Double.toString(r.getCurrency()), Double.toString(r.getExp())))
                    return false;
            }
            for (Entry<EntityType, Reward> entry : mobKills.entrySet()) {
                Reward r = entry.getValue();
                if (!manager.executeUpdate("INSERT INTO jobslitekills (`job`, `type`, `cur`, `exp`) VALUES (?,?,?,?)", id, entry.getKey()
                        .toString(), Double.toString(r.getCurrency()), Double.toString(r.getExp())))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteJob(String job) {
        return (manager.executeUpdate("DELETE FROM jobslitejobs WHERE id = ?", job) && manager.executeUpdate(
                "DELETE FROM jobslitebreaks WHERE job = ?", job) && manager.executeUpdate("DELETE FROM jobsliteplaces WHERE job = ?", job)
                && manager.executeUpdate("DELETE FROM jobslitekills WHERE job = ?", job));
    }
}

/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2018 Flibio
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
import me.flibio.jobslite.api.PlayerManager;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RemotePlayerManager implements PlayerManager {

    private SqlManager manager;

    public RemotePlayerManager(String hostname, String port, String database, String username, String password) {
        manager = RemoteSqlManager.createInstance(JobsLite.getInstance(), hostname, port, database, username, password).get();
        if (manager.initialTestConnection())
            manager.executeUpdate("CREATE TABLE IF NOT EXISTS jobsliteplayers(uuid VARCHAR(36), job VARCHAR(1024), exp DECIMAL(11,2), level INT(11))");
    }

    public boolean isWorking() {
        return manager.testConnection();
    }

    @Override
    public List<String> getCurrentJobs(UUID uuid) {
        List<String> jobs = new ArrayList<>();
        Optional<ResultSet> rOpt = manager.executeQuery("SELECT job FROM jobsliteplayers WHERE uuid = ?", uuid.toString());
        if (rOpt.isPresent()) {
            ResultSet rs = rOpt.get();
            try {
                while (rs.next()) {
                    jobs.add(rs.getString("job"));
                }
            } catch (SQLException e) {
                return jobs;
            }
        }
        return jobs;
    }

    @Override
    public int getCurrentLevel(UUID uuid, String job) {
        if (getCurrentJobs(uuid).contains(job)) {
            Optional<Integer> iOpt =
                    manager.queryType("level", Integer.class, "SELECT level FROM jobsliteplayers WHERE uuid = ? and job = ?", uuid.toString(), job);
            if (iOpt.isPresent()) {
                return iOpt.get();
            }
        }
        return 0;
    }

    @Override
    public Optional<Double> getCurrentExp(UUID uuid, String job) {
        if (getCurrentJobs(uuid).contains(job)) {
            Optional<BigDecimal> bOpt =
                    manager.queryType("exp", BigDecimal.class, "SELECT exp FROM jobsliteplayers WHERE uuid = ? and job = ?", uuid.toString(), job);
            if (bOpt.isPresent()) {
                return Optional.of(Double.parseDouble(bOpt.get().toString()));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean setExp(UUID uuid, String job, double exp) {
        if (getCurrentJobs(uuid).contains(job)) {
            return manager.executeUpdate("UPDATE jobsliteplayers SET exp = ? WHERE uuid = ? AND job = ?", Double.toString(exp), uuid.toString(),
                    job);
        }
        return false;
    }

    @Override
    public boolean setLevel(UUID uuid, String job, int level) {
        if (getCurrentJobs(uuid).contains(job)) {
            return manager.executeUpdate("UPDATE jobsliteplayers SET level = ? WHERE uuid = ? AND job = ?", Integer.toString(level), uuid.toString(),
                    job);
        }
        return false;
    }

    @Override
    public boolean addJob(UUID uuid, String job) {
        if (!getCurrentJobs(uuid).contains(job)) {
            return manager.executeUpdate("INSERT INTO jobsliteplayers (`uuid`, `job`, `exp`, `level`) VALUES (?, ?, ?, ?)", uuid.toString(), job,
                    "0", "0");
        }
        return false;
    }

    @Override
    public boolean removeAllJobs(UUID uuid) {
        return manager.executeUpdate("DELETE FROM jobsliteplayers WHERE uuid = ?", uuid.toString());
    }

    @Override
    public boolean removeJob(UUID uuid, String job) {
        return manager.executeUpdate("DELETE FROM jobsliteplayers WHERE uuid = ? AND job = ?", uuid.toString(), job);
    }

    @Override
    public boolean removeJob(String job) {
        return manager.executeUpdate("DELETE FROM jobsliteplayers WHERE AND job = ?", job);
    }

}

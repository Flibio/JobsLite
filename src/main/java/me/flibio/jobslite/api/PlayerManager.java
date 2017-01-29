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
package me.flibio.jobslite.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {

    /**
     * Gets the current jobs of a player.
     * 
     * @param uuid The UUID of the player.
     * @return The current jobs of the player.
     */
    List<String> getCurrentJobs(UUID uuid);

    /**
     * Checks if a player has a specific job.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @return If the player has the job.
     */
    default boolean hasJob(UUID uuid, String job) {
        return getCurrentJobs(uuid).contains(job);
    }

    /**
     * Gets the current level of a player.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @return The current level of the player.
     */
    int getCurrentLevel(UUID uuid, String job);

    /**
     * Gets the current experience of a player.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @return The current experience of a player.
     */
    Optional<Double> getCurrentExp(UUID uuid, String job);

    /**
     * Sets the experience of a player.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @param exp The amount of experience.
     * @return If the method succeeded.
     */
    boolean setExp(UUID uuid, String job, double exp);

    /**
     * Sets the level of a player.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @param level The new level of the player.
     * @return If the method succeeded.
     */
    boolean setLevel(UUID uuid, String job, int level);

    /**
     * Adds a job to a player.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @return If the method succeeded.
     */
    boolean addJob(UUID uuid, String job);

    /**
     * Removes all jobs from a player.
     * 
     * @param uuid The UUID of the player.
     * @return If the method succeeded.
     */
    boolean removeAllJobs(UUID uuid);

    /**
     * Removes a job from a player.
     * 
     * @param uuid The UUID of the player.
     * @param job The name of the job.
     * @return If the method succeeded.
     */
    boolean removeJob(UUID uuid, String job);

    /**
     * Removes a job from all players.
     * 
     * @param job The job to remove.
     * @return If the method was successful.
     */
    boolean removeJob(String job);
}

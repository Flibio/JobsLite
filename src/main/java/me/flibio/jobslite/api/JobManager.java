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
package me.flibio.jobslite.api;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.format.TextColor;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface JobManager {

    /**
     * Checks if a job exists.
     * 
     * @param job The job to check for.
     * @return If the job exists.
     */
    boolean jobExists(String job);

    /**
     * Gets a job.
     * 
     * @param job The job to get.
     * @return The job, if found.
     */
    Optional<Job> getJob(String job);

    /**
     * Gets all jobs.
     * 
     * @return All jobs.
     */
    List<Job> getJobs();

    /**
     * Creates a new job.
     * 
     * @param id The job id.
     * @param displayName The job display name.
     * @param maxLevel The max level of the job.
     * @param textColor The color of the job.
     * @param silkTouch If the job allows silk touch tools.
     * @param worldGen If the job allows only world generated blocks.
     * @param ignoreData If the job ignores block data.
     * @param blockBreaks The blocks that give rewards for breaking.
     * @param blockPlaces The blocks that give rewards for placing.
     * @param mobKills The mobs that give rewards for killing.
     * @return If the job was created successfully.
     */
    boolean createJob(String id, String displayName, int maxLevel, TextColor textColor, boolean silkTouch, boolean worldGen, boolean ignoreData,
            HashMap<BlockState, Reward> blockBreaks, HashMap<BlockState, Reward> blockPlaces, HashMap<EntityType, Reward> mobKills);

    /**
     * Deletes a job.
     * 
     * @param job The job to delete.
     * @return If the job was deleted successfully.
     */
    boolean deleteJob(String job);
}

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
package me.flibio.jobslite.creation.task;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.api.JobManager;
import me.flibio.jobslite.api.Reward;
import me.flibio.jobslite.creation.CreatingJob;
import me.flibio.jobslite.creation.data.DataTypes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.format.TextColor;

import java.util.HashMap;

public class FinalTask extends Task {

    public FinalTask(CreatingJob parent) {
        super(parent);
    }

    @SuppressWarnings("unchecked")
    public void initialize() {
        Player player = getPlayer();
        CreatingJob parent = getParent();
        JobManager jobManager = JobsLite.getJobManager();
        if (jobManager.createJob((String) parent.getData(DataTypes.NAME).getValue(), (String) parent.getData(DataTypes.DISPLAY_NAME).getValue(),
                (Integer) parent.getData(DataTypes.MAX_LEVEL).getValue(), (TextColor) parent.getData(DataTypes.COLOR).getValue(),
                (Boolean) parent.getData(DataTypes.SILK_TOUCH).getValue(), (Boolean) parent.getData(DataTypes.WORLD_GEN).getValue(),
                (Boolean) parent.getData(DataTypes.BLOCK_DATA).getValue(),
                (HashMap<BlockState, Reward>) parent.getData(DataTypes.BREAK_REWARDS).getValue(),
                (HashMap<BlockState, Reward>) parent.getData(DataTypes.PLACE_REWARDS).getValue(),
                (HashMap<EntityType, Reward>) parent.getData(DataTypes.KILL_REWARDS).getValue())) {
            getParent().cancel();
            player.sendMessage(messages.getMessage("creation.success"));
        } else {
            player.sendMessage(messages.getMessage("creation.fail"));
        }
    }
}

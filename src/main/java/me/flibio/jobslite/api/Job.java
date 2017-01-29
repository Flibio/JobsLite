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

import me.flibio.jobslite.utils.NumberUtils;
import org.spongepowered.api.text.format.TextColor;

import java.util.Map;

public class Job {

    private String id;
    private String displayName;
    private int maxLevel;
    private TextColor textColor;
    private boolean silkTouch;
    private boolean worldGen;
    private boolean ignoreData;
    private String expEquation;
    private String curEquation;
    private Map<String, Reward> blockBreaks;
    private Map<String, Reward> blockPlaces;
    private Map<String, Reward> mobKills;

    public Job(String id, String displayName, int maxLevel, TextColor textColor, boolean silkTouch, boolean worldGen, boolean ignoreData,
            String expEquation, String curEquation, Map<String, Reward> blockBreaks, Map<String, Reward> blockPlaces,
            Map<String, Reward> mobKills) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.textColor = textColor;
        this.silkTouch = silkTouch;
        this.worldGen = worldGen;
        this.ignoreData = ignoreData;
        this.expEquation = expEquation;
        this.curEquation = curEquation;
        this.blockBreaks = blockBreaks;
        this.blockPlaces = blockPlaces;
        this.mobKills = mobKills;
    }

    public double getExpRequired(int level, double exp) {
        String expEquation = this.expEquation.replaceAll("currentLevel", level + "");
        double expRequired = NumberUtils.eval(expEquation);
        return expRequired - exp;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public boolean isSilkTouch() {
        return silkTouch;
    }

    public boolean isWorldGen() {
        return worldGen;
    }

    public boolean isIgnoreData() {
        return ignoreData;
    }

    public String getExpEquation() {
        return expEquation;
    }

    public String getCurEquation() {
        return curEquation;
    }

    public Map<String, Reward> getBlockBreaks() {
        return blockBreaks;
    }

    public Map<String, Reward> getBlockPlaces() {
        return blockPlaces;
    }

    public Map<String, Reward> getMobKills() {
        return mobKills;
    }
}

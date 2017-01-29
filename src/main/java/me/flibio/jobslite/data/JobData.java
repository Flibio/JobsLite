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
package me.flibio.jobslite.data;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

@Deprecated
public class JobData extends AbstractData<JobData, ImmutableJobData> implements Comparable<JobData> {

    private int level;
    private int exp;
    private String jobName;

    protected JobData(String jobName, int level, int exp) {
        this.jobName = jobName;
        this.level = level;
        this.exp = exp;
        registerGettersAndSetters();
    }

    public Value<String> jobName() {
        return Sponge.getRegistry().getValueFactory().createValue(LiteKeys.JOB_NAME, this.jobName, "");
    }

    public Value<Integer> level() {
        return Sponge.getRegistry().getValueFactory().createValue(LiteKeys.LEVEL, this.level, 0);
    }

    public Value<Integer> exp() {
        return Sponge.getRegistry().getValueFactory().createValue(LiteKeys.EXP, this.exp, 0);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(LiteKeys.LEVEL, () -> this.level);
        registerFieldSetter(LiteKeys.LEVEL, value -> this.level = value);
        registerKeyValue(LiteKeys.LEVEL, this::level);

        registerFieldGetter(LiteKeys.EXP, () -> this.exp);
        registerFieldSetter(LiteKeys.EXP, value -> this.exp = value);
        registerKeyValue(LiteKeys.EXP, this::exp);

        registerFieldGetter(LiteKeys.JOB_NAME, () -> this.jobName);
        registerFieldSetter(LiteKeys.JOB_NAME, value -> this.jobName = value);
        registerKeyValue(LiteKeys.JOB_NAME, this::jobName);
    }

    @Override
    public JobData copy() {
        return new JobData(jobName, level, exp);
    }

    @Override
    public Optional<JobData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
        if (!dataHolder.getKeys().contains(LiteKeys.JOB_NAME) || !dataHolder.getKeys().contains(LiteKeys.LEVEL) ||
                !dataHolder.getKeys().contains(LiteKeys.EXP)) {
            return Optional.empty();
        }
        final String jobName = dataHolder.get(LiteKeys.JOB_NAME).get();
        final int exp = dataHolder.get(LiteKeys.EXP).get();
        final int level = dataHolder.get(LiteKeys.LEVEL).get();
        this.jobName = jobName;
        this.exp = exp;
        this.level = level;
        return Optional.of(this);
    }

    @Override
    public Optional<JobData> from(DataContainer container) {
        if (!container.contains(LiteKeys.JOB_NAME.getQuery(), LiteKeys.LEVEL.getQuery(), LiteKeys.EXP.getQuery())) {
            return Optional.empty();
        }
        final String jobName = container.getString(LiteKeys.JOB_NAME.getQuery()).get();
        final int exp = container.getInt(LiteKeys.EXP.getQuery()).get();
        final int level = container.getInt(LiteKeys.LEVEL.getQuery()).get();
        this.jobName = jobName;
        this.exp = exp;
        this.level = level;
        return Optional.of(this);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(LiteKeys.JOB_NAME, this.jobName)
                .set(LiteKeys.LEVEL, this.level)
                .set(LiteKeys.EXP, this.exp);
    }

    @Override
    public ImmutableJobData asImmutable() {
        return new ImmutableJobData(jobName, level, exp);
    }

    @Override
    public int compareTo(JobData o) {
        return ComparisonChain.start()
                .compare(o.jobName, this.jobName)
                .compare(o.level, this.level)
                .compare(o.exp, this.exp)
                .result();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("jobName", this.jobName)
                .add("level", this.level)
                .add("exp", this.exp)
                .toString();
    }

}

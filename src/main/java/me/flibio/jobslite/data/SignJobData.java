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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class SignJobData extends AbstractData<SignJobData, ImmutableSignJobData> implements Comparable<SignJobData> {

    private String jobName;

    protected SignJobData(String jobName) {
        this.jobName = jobName;
        registerGettersAndSetters();
    }

    public Value<String> jobName() {
        return Sponge.getRegistry().getValueFactory().createValue(LiteKeys.JOB_NAME, this.jobName, "");
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(LiteKeys.JOB_NAME, () -> this.jobName);
        registerFieldSetter(LiteKeys.JOB_NAME, value -> this.jobName = value);
        registerKeyValue(LiteKeys.JOB_NAME, this::jobName);
    }

    @Override
    public SignJobData copy() {
        return new SignJobData(jobName);
    }

    @Override
    public Optional<SignJobData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
        if (!dataHolder.getKeys().contains(LiteKeys.JOB_NAME)) {
            return Optional.empty();
        }
        final String jobName = dataHolder.get(LiteKeys.JOB_NAME).get();
        this.jobName = jobName;
        return Optional.of(this);
    }

    @Override
    public Optional<SignJobData> from(DataContainer container) {
        if (!container.contains(LiteKeys.JOB_NAME.getQuery())) {
            return Optional.empty();
        }
        final String jobName = container.getString(LiteKeys.JOB_NAME.getQuery()).get();
        this.jobName = jobName;
        return Optional.of(this);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(LiteKeys.JOB_NAME, this.jobName);
    }

    @Override
    public ImmutableSignJobData asImmutable() {
        return new ImmutableSignJobData(jobName);
    }

    @Override
    public int compareTo(SignJobData o) {
        return ComparisonChain.start()
                .compare(o.jobName, this.jobName)
                .result();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jobName", this.jobName)
                .toString();
    }

}

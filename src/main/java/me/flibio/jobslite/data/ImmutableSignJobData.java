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
package me.flibio.jobslite.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import com.google.common.collect.ComparisonChain;

import java.util.Optional;

public class ImmutableSignJobData extends AbstractImmutableData<ImmutableSignJobData, SignJobData> implements Comparable<ImmutableSignJobData> {

    private String jobName;

    protected ImmutableSignJobData(String jobName) {
        this.jobName = jobName;
        registerGetters();
    }

    public ImmutableValue<String> jobName() {
        return Sponge.getRegistry().getValueFactory().createValue(LiteKeys.JOB_NAME, this.jobName, "").asImmutable();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(LiteKeys.JOB_NAME, () -> this.jobName);
        registerKeyValue(LiteKeys.JOB_NAME, this::jobName);
    }

    @Override
    public <E> Optional<ImmutableSignJobData> with(Key<? extends BaseValue<E>> key, E value) {
        return Optional.empty();
    }

    @Override
    public int compareTo(ImmutableSignJobData o) {
        return ComparisonChain.start()
                .compare(o.jobName, this.jobName)
                .result();
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
    public SignJobData asMutable() {
        return new SignJobData(jobName);
    }

}

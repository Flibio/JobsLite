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
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import com.google.common.base.Preconditions;

import java.util.Optional;

public class JobData extends AbstractSingleData<JobInfo, JobData, ImmutableJobData> implements DataSerializable {

    protected JobData(JobInfo value) {
        super(value, Keys.JOB);
    }

    @Override
    public JobData copy() {
        return new JobData(this.getValue());
    }

    @Override
    public Optional<JobData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
        JobData balanceData = Preconditions.checkNotNull(mergeFn).merge(copy(), from(dataHolder.toContainer()).orElse(null));
        return Optional.of(set(Keys.JOB, balanceData.get(Keys.JOB).get()));
    }

    @Override
    public Optional<JobData> from(DataContainer container) {
        if (container.contains(Keys.JOB.getQuery())) {
            return Optional.of(set(Keys.JOB, (JobInfo) container.get(Keys.JOB.getQuery()).orElse(null)));
        }
        return Optional.empty();
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public ImmutableJobData asImmutable() {
        return new ImmutableJobData(this.getValue());
    }

    @Override
    public int compareTo(JobData arg0) {
        return 0;
    }

    @Override
    protected Value<JobInfo> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(Keys.JOB, getValue());
    }

}
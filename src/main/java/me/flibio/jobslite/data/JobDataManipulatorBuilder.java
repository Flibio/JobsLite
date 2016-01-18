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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.util.persistence.InvalidDataException;

import java.util.Optional;

public class JobDataManipulatorBuilder implements DataManipulatorBuilder<JobData, ImmutableJobData> {
    
    private JobInfo data;

    @Override
    public Optional<JobData> build(DataView container) throws InvalidDataException {
        if(!container.contains(Keys.JOB.getQuery())) {
            return Optional.empty();
        }
        JobData balanceData = create();
        balanceData = balanceData.set(Keys.JOB, (JobInfo) container.get(Keys.JOB.getQuery()).get());
        return Optional.of(balanceData);
    }
    
    public JobDataManipulatorBuilder setJobInfo(JobInfo data) {
        this.data = data;
        return this;
    }

    @Override
    public JobData create() {
        return new JobData(data);
    }

    @Override
    public Optional<JobData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

}
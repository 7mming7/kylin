/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.storage;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.cube.cuboid.Cuboid;
import org.apache.kylin.metadata.realization.IRealization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

/**
 * @author xjiang
 */
public class StorageContext {
    private static final Logger logger = LoggerFactory.getLogger(StorageContext.class);

    private String connUrl;
    private int threshold;
    private int limit = Integer.MAX_VALUE;
    private int offset = 0;
    private int finalPushDownLimit = Integer.MAX_VALUE;
    private boolean hasSort = false;
    private boolean acceptPartialResult = false;

    private boolean exactAggregation = false;
    private boolean needStorageAggregation = false;
    private boolean limitEnabled = false;
    private boolean enableCoprocessor = false;

    private AtomicLong totalScanCount = new AtomicLong();
    private Cuboid cuboid;
    private boolean partialResultReturned = false;

    public StorageContext() {
        this.threshold = KylinConfig.getInstanceFromEnv().getScanThreshold();
    }

    private Range<Long> reusedPeriod;

    public String getConnUrl() {
        return connUrl;
    }

    public void setConnUrl(String connUrl) {
        this.connUrl = connUrl;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int t) {
        threshold = t;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int l) {
        if (limit != Integer.MAX_VALUE) {
            logger.warn("Setting limit to {} but in current olap context, the limit is already {}, won't apply", l, limit);
        } else {
            limit = l;
        }
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void enableLimit() {
        this.limitEnabled = true;
    }

    public boolean isLimitEnabled() {
        return this.limitEnabled;
    }

    public int getFinalPushDownLimit() {
        return finalPushDownLimit;
    }

    public void setFinalPushDownLimit(IRealization realization) {

        if (this.getLimit() == Integer.MAX_VALUE) {
            return;
        }

        int tempPushDownLimit = this.getOffset() + this.getLimit();

        if (!realization.supportsLimitPushDown()) {
            logger.warn("Not enabling limit push down because cube storage type not supported");
        } else {
            this.limitEnabled = true;
            this.finalPushDownLimit = tempPushDownLimit;
            logger.info("Enable limit: " + tempPushDownLimit);
        }
    }

    public void markSort() {
        this.hasSort = true;
    }

    public boolean hasSort() {
        return this.hasSort;
    }

    public void setCuboid(Cuboid c) {
        cuboid = c;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public long getTotalScanCount() {
        return totalScanCount.get();
    }

    public long increaseTotalScanCount(long count) {
        return this.totalScanCount.addAndGet(count);
    }

    public boolean isAcceptPartialResult() {
        return acceptPartialResult;
    }

    public void setAcceptPartialResult(boolean acceptPartialResult) {
        this.acceptPartialResult = acceptPartialResult;
    }

    public boolean isPartialResultReturned() {
        return partialResultReturned;
    }

    public void setPartialResultReturned(boolean partialResultReturned) {
        this.partialResultReturned = partialResultReturned;
    }

    public boolean isNeedStorageAggregation() {
        return needStorageAggregation;
    }

    public void setNeedStorageAggregation(boolean needStorageAggregation) {
        this.needStorageAggregation = needStorageAggregation;
    }

    public void setExactAggregation(boolean isExactAggregation) {
        this.exactAggregation = isExactAggregation;
    }

    public boolean isExactAggregation() {
        return this.exactAggregation;
    }

    public void enableCoprocessor() {
        this.enableCoprocessor = true;
    }

    public boolean isCoprocessorEnabled() {
        return this.enableCoprocessor;
    }

    public Range<Long> getReusedPeriod() {
        return reusedPeriod;
    }

    public void setReusedPeriod(Range<Long> reusedPeriod) {
        this.reusedPeriod = reusedPeriod;
    }
}

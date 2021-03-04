/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.durg.tsaheylu.circuitbreaker.config.hystrix;

import lombok.Builder;
import lombok.Data;

/**
 * @author phaneesh
 * @author Manas Mulay
 */

// MARCH-2021 - Manas - no changes
@Data
public class CommandThreadPoolConfig {

    private boolean semaphoreIsolation = false;

    private String pool;

    private int concurrency = 8;

    private int maxRequestQueueSize = 128;

    private int dynamicRequestQueueSize = 16;

    private int timeout = 1000;

    @Builder
    public CommandThreadPoolConfig(boolean semaphoreIsolation,
                                   String pool,
                                   int concurrency,
                                   int maxRequestQueueSize,
                                   int dynamicRequestQueueSize,
                                   int timeout) {
        this.semaphoreIsolation = semaphoreIsolation;
        this.pool = pool;
        this.concurrency = concurrency;
        this.maxRequestQueueSize = maxRequestQueueSize;
        this.dynamicRequestQueueSize = dynamicRequestQueueSize;
        this.timeout = timeout;
    }

    //Default values
    public static class CommandThreadPoolConfigBuilder {

        private String pool;

        private boolean semaphoreIsolation = false;

        private int concurrency = Runtime.getRuntime().availableProcessors();

        private int maxRequestQueueSize = Runtime.getRuntime().availableProcessors() * 4;

        private int dynamicRequestQueueSize = Runtime.getRuntime().availableProcessors() * 2;

        private int timeout = 1000;

    }

    public boolean isEqual(CommandThreadPoolConfig config) {
        return config.concurrency == this.concurrency &&
                config.maxRequestQueueSize == this.maxRequestQueueSize &&
                config.dynamicRequestQueueSize == this.dynamicRequestQueueSize &&
                config.timeout == this.timeout;
    }
}

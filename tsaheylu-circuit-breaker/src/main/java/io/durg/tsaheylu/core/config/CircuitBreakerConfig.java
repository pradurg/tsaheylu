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

package io.durg.tsaheylu.core.config;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author phaneesh
 * @author Manas Mulay
 */
// MARCH-2021 - Manas - removed CircuitBreakerConfigBuilder
@Data
@NoArgsConstructor
public class CircuitBreakerConfig {

    private int acceptableFailuresInWindow = 20;

    private int waitTimeBeforeRetry = 5000;

    private int errorThreshold = 50;

    @Builder
    public CircuitBreakerConfig(int acceptableFailuresInWindow, int waitTimeBeforeRetry, int errorThreshold) {
        this.acceptableFailuresInWindow = acceptableFailuresInWindow;
        this.waitTimeBeforeRetry = waitTimeBeforeRetry;
        this.errorThreshold = errorThreshold;
    }

}

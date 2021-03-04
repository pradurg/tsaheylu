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
 *
 */

package io.durg.tsaheylu;

import io.durg.tsaheylu.circuitbreaker.BaseCommand;
import io.durg.tsaheylu.circuitbreaker.TsaheyluHystrixCircuitBreaker;
import io.durg.tsaheylu.circuitbreaker.config.hystrix.CircuitBreakerConfig;
import io.durg.tsaheylu.circuitbreaker.config.hystrix.CommandThreadPoolConfig;
import io.durg.tsaheylu.circuitbreaker.config.hystrix.HystrixCommandConfig;
import io.durg.tsaheylu.circuitbreaker.config.hystrix.HystrixConfiguratorConfig;
import io.durg.tsaheylu.circuitbreaker.config.hystrix.HystrixDefaultConfig;
import io.durg.tsaheylu.circuitbreaker.config.hystrix.MetricsConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * @author phaneesh
 * @author Manas Mulay
 */
// MARCH 2021 - Manas - Modified Class to reflect naming changes
public class HystrixConfigurationFactorySingleCommandWithCachingSetterTest {

    @BeforeEach
    public void setup() {
        TsaheyluHystrixCircuitBreaker circuitBreaker = new TsaheyluHystrixCircuitBreaker();
        circuitBreaker.init(HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .commands(Collections.singletonList(HystrixCommandConfig.builder().name("test").build()))
                .build());
    }

    @Test
    public void testCommand() throws ExecutionException, InterruptedException {
        SimpleTestCommand command = new SimpleTestCommand();
        String result = command.queue().get();
        Assertions.assertTrue(result.equals("Simple Test"));
    }

    public static class SimpleTestCommand extends BaseCommand<String> {

        public SimpleTestCommand() {
            super("test", HystrixCommandConfig.builder()
                    .name("test")
                    .circuitBreaker(CircuitBreakerConfig.builder().build())
                    .metrics(MetricsConfig.builder().build())
                    .threadPool(CommandThreadPoolConfig.builder().build())
                    .semaphoreIsolation(false)
                    .timeout(150)
                    .build());
        }

        @Override
        protected String run() throws Exception {
            return "Simple Test";
        }
    }
}

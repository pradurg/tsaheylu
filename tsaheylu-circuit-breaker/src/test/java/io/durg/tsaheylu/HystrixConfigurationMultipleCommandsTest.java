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

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import io.durg.tsaheylu.core.TsaheyluHystrixCircuitBreaker;
import io.durg.tsaheylu.core.config.CommandThreadPoolConfig;
import io.durg.tsaheylu.core.config.HystrixCommandConfig;
import io.durg.tsaheylu.core.config.HystrixConfiguratorConfig;
import io.durg.tsaheylu.core.config.HystrixDefaultConfig;
import io.durg.tsaheylu.core.config.ThreadPoolConfig;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author phaneesh
 * @author Manas Mulay
 */
// MARCH 2021 - Manas - Modified Class to reflect naming changes
public class HystrixConfigurationMultipleCommandsTest {

    private TsaheyluHystrixCircuitBreaker circuitBreaker;

    @Before
    public void beforeMethod() {
        this.circuitBreaker = new TsaheyluHystrixCircuitBreaker();
        this.circuitBreaker.init(HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .commands(Collections.singletonList(HystrixCommandConfig.builder().name("test").build()))
                .build());
    }

    @Test
    public void testCommand() throws ExecutionException, InterruptedException {
        SimpleTestCommand1 command1 = new SimpleTestCommand1();
        String result1 = command1.queue().get();
        assertEquals("Simple Test 1", result1);

        SimpleTestCommand2 command2 = new SimpleTestCommand2();
        String result2 = command2.queue().get();
        assertEquals("Simple Test 2", result2);
    }


    @Test
    public void testCommandWithDedicatedPool() throws ExecutionException, InterruptedException {
        val hystrixConfig = HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .pools(createPoolConfigs("testPool1", "testPool2"))
                .commands(Collections.singletonList(HystrixCommandConfig.builder()
                        .name("test1")
                        .threadPool(CommandThreadPoolConfig.builder()
                                .pool("testPool1")
                                .build())
                        .build()))
                .build();

        this.circuitBreaker.init(hystrixConfig);

        SimpleTestCommand1 command1 = new SimpleTestCommand1();
        String result1 = command1.queue().get();
        assertEquals("Simple Test 1", result1);

        SimpleTestCommand2 command2 = new SimpleTestCommand2();
        String result2 = command2.queue().get();
        assertEquals("Simple Test 2", result2);
    }

    @Test
    public void testCommandWithDedicatedPoolCountPools() {
        HystrixConfiguratorConfig hystrixConfig = HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .pools(createPoolConfigs("testPool1", "testPool2"))
                .commands(Collections.singletonList(HystrixCommandConfig.builder()
                        .name("test1")
                        .build()))
                .build();

        this.circuitBreaker.init(hystrixConfig);
        assertEquals(1, this.circuitBreaker.getCommandCache().size());
        assertEquals(3, this.circuitBreaker.getPoolCache().size());

        hystrixConfig = HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .pools(createPoolConfigs("testPool1", "testPool2"))
                .commands(Collections.singletonList(HystrixCommandConfig.builder()
                        .name("test1")
                        .threadPool(CommandThreadPoolConfig.builder()
                                .pool("testPool1")
                                .build())
                        .build()))
                .build();
        this.circuitBreaker.init(hystrixConfig);
        assertEquals(1, this.circuitBreaker.getCommandCache().size());
        assertEquals(2, this.circuitBreaker.getPoolCache().size());
    }

    @Test
    public void testCommandWithSemaphoreIsolation() throws ExecutionException, InterruptedException {
        HystrixConfiguratorConfig hystrixConfig = HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .pools(createPoolConfigs("testPool1", "testPool2"))
                .commands(Collections.singletonList(HystrixCommandConfig.builder()
                        .name("test1")
                        .threadPool(CommandThreadPoolConfig.builder()
                                .semaphoreIsolation(true)
                                .build())
                        .build()))
                .build();
        this.circuitBreaker.init(hystrixConfig);
        assertEquals(1, this.circuitBreaker.getCommandCache().size());
        assertEquals(2, this.circuitBreaker.getPoolCache().size());
        SimpleTestCommand1 command = new SimpleTestCommand1();
        String result1 = command.queue().get();
        assertEquals("Simple Test 1", result1);

    }

    @Test(expected = RuntimeException.class)
    public void testCommandWithNonExistingDedicatedPool() {
        val hystrixConfig = HystrixConfiguratorConfig.builder()
                .defaultConfig(new HystrixDefaultConfig())
                .pools(createPoolConfigs("testPool1", "testPool2"))
                .commands(Collections.singletonList(HystrixCommandConfig.builder()
                        .name("test1")
                        .threadPool(CommandThreadPoolConfig.builder()
                                .pool("testPool3")
                                .build())
                        .build()))
                .build();
        this.circuitBreaker.init(hystrixConfig);
    }

    public static class SimpleTestCommand1 extends HystrixCommand<String> {

        public SimpleTestCommand1() {
            super(HystrixCommandGroupKey.Factory.asKey("test"));
        }

        @Override
        protected String run() {
            return "Simple Test 1";
        }
    }

    public static class SimpleTestCommand2 extends HystrixCommand<String> {

        public SimpleTestCommand2() {
            super(HystrixCommandGroupKey.Factory.asKey("test"));
        }

        @Override
        protected String run() {
            return "Simple Test 2";
        }
    }

    private Map<String, ThreadPoolConfig> createPoolConfigs(String... poolNames) {
        return Stream.of(poolNames).collect(Collectors.toMap(x -> x, x -> ThreadPoolConfig.builder().build()));
    }

}

package io.durg.tsaheylu.core;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.durg.tsaheylu.core.config.CommandThreadPoolConfig;
import io.durg.tsaheylu.core.config.HystrixCommandConfig;
import io.durg.tsaheylu.core.config.HystrixConfiguratorConfig;
import io.durg.tsaheylu.core.config.HystrixDefaultConfig;
import io.durg.tsaheylu.core.config.ThreadPoolConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author phaneesh
 * @author Manas Mulay
 */
// MARCH-2021 - Manas - added threadPoolCache, added cache updation in getCommandConfiguration and renamed class
//                      to TsaheyluHystrixCircuitBreaker
@Slf4j
public class TsaheyluHystrixCircuitBreaker {

    private HystrixConfiguratorConfig config;

    @Getter
    private static HystrixDefaultConfig defaultConfig;

    private static ConcurrentHashMap<String, HystrixCommand.Setter> commandCache;

    private static ConcurrentHashMap<String, HystrixCommandConfig> threadPoolCache;

    private static ConcurrentHashMap<String, HystrixThreadPoolProperties.Setter> poolCache;

    private TsaheyluHystrixCircuitBreaker(final HystrixConfiguratorConfig config) {
        this.config = config;
    }

    public TsaheyluHystrixCircuitBreaker() {
    }

    public void init(final HystrixConfiguratorConfig config) {
        this.config = config;
        setup();
    }

    private void setup() {
        validateUniqueCommands(this.config);
        this.defaultConfig = this.config.getDefaultConfig();
        this.commandCache = new ConcurrentHashMap<>();
        this.poolCache = new ConcurrentHashMap<>();
        this.threadPoolCache = new ConcurrentHashMap<>();

        registerDefaultProperties(this.defaultConfig);

        if (this.config.getPools() != null) {
            this.config.getPools().forEach((pool, poolConfig) -> configureThreadPoolIfMissing(globalPoolId(pool), poolConfig));
        }

        this.config.getCommands()
                .forEach(commandConfig -> {
                    if (commandConfig.getCircuitBreaker() == null) {
                        commandConfig.setCircuitBreaker(this.defaultConfig.getCircuitBreaker());
                    }

                    if (commandConfig.getMetrics() == null) {
                        commandConfig.setMetrics(this.defaultConfig.getMetrics());
                    }

                    if (commandConfig.getThreadPool() == null) {
                        commandConfig.setThreadPool(this.defaultConfig.getThreadPool());
                    }

                    registerCommandProperties(this.defaultConfig, commandConfig);
                    log.info("registered command: {}", commandConfig.getName());
                });
    }

    private static String globalPoolId(String pool) {
        return String.format("global_%s", pool);
    }

    private static String commandPoolId(String commandName) {
        return String.format("command_%s", commandName);
    }

    private void registerDefaultProperties(HystrixDefaultConfig defaultConfig) {

        configureProperty("hystrix.timer.threadpool.default.coreSize", defaultConfig.getTimer().getConcurrency());
        configureProperty("hystrix.threadpool.default.coreSize", defaultConfig.getThreadPool().getConcurrency());
        configureProperty("hystrix.threadpool.default.maxQueueSize", defaultConfig.getThreadPool().getMaxRequestQueueSize());
        configureProperty("hystrix.threadpool.default.queueSizeRejectionThreshold", defaultConfig.getThreadPool().getDynamicRequestQueueSize());

        configureProperty("hystrix.command.default.coreSize", defaultConfig.getThreadPool().getConcurrency());
        configureProperty("hystrix.command.default.maxQueueSize", defaultConfig.getThreadPool().getMaxRequestQueueSize());
        configureProperty("hystrix.command.default.queueSizeRejectionThreshold", defaultConfig.getThreadPool().getDynamicRequestQueueSize());

        configureProperty("hystrix.command.default.execution.isolation.strategy", "THREAD");
        configureProperty("hystrix.command.default.execution.thread.timeoutInMilliseconds", defaultConfig.getThreadPool().getTimeout());
        configureProperty("hystrix.command.default.execution.timeout.enabled", true);
        configureProperty("hystrix.command.default.execution.isolation.thread.interruptOnTimeout", true);
        configureProperty("hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests", defaultConfig.getThreadPool().getConcurrency());

        configureProperty("hystrix.command.default.circuitBreaker.enabled", true);
        configureProperty("hystrix.command.default.circuitBreaker.requestVolumeThreshold", defaultConfig.getCircuitBreaker().getAcceptableFailuresInWindow());
        configureProperty("hystrix.command.default.circuitBreaker.errorThresholdPercentage", defaultConfig.getCircuitBreaker().getErrorThreshold());
        configureProperty("hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds", defaultConfig.getCircuitBreaker().getWaitTimeBeforeRetry());

        configureProperty("hystrix.command.default.metrics.rollingStats.timeInMilliseconds", defaultConfig.getMetrics().getStatsTimeInMillis());
        configureProperty("hystrix.command.default.metrics.rollingStats.numBuckets", defaultConfig.getMetrics().getNumBucketSize());
        configureProperty("hystrix.command.default.metrics.rollingPercentile.enabled", true);
        configureProperty("hystrix.command.default.metrics.rollingPercentile.timeInMilliseconds", defaultConfig.getMetrics().getPercentileTimeInMillis());
        configureProperty("hystrix.command.default.metrics.rollingPercentile.numBuckets", defaultConfig.getMetrics().getNumBucketSize());
        configureProperty("hystrix.command.default.metrics.rollingPercentile.bucketSize", defaultConfig.getMetrics().getPercentileBucketSize());
        configureProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", defaultConfig.getMetrics().getHealthCheckInterval());
    }

    private void registerCommandProperties(HystrixCommandConfig config) {
        registerCommandProperties(this.defaultConfig, config);
    }

    private static void registerCommandProperties(HystrixDefaultConfig defaultConfig, HystrixCommandConfig commandConfig) {
        val command = commandConfig.getName();

        val semaphoreIsolation = commandConfig.getThreadPool().isSemaphoreIsolation();

        val isolationStrategy = semaphoreIsolation ? HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE
                : HystrixCommandProperties.ExecutionIsolationStrategy.THREAD;


        configureProperty(String.format("hystrix.command.%s.execution.isolation.strategy", command), isolationStrategy.name());
        configureProperty(String.format("hystrix.command.%s.execution.timeout.enabled", command), true);
        configureProperty(String.format("hystrix.command.%s.execution.thread.timeoutInMilliseconds", command), commandConfig.getThreadPool().getTimeout());
        configureProperty(String.format("hystrix.command.%s.execution.isolation.thread.interruptOnTimeout", command), true);

        if (semaphoreIsolation) {
            configureProperty(String.format("hystrix.command.%s.execution.isolation.semaphore.maxConcurrentRequests", command), commandConfig.getThreadPool().getConcurrency());
        }

        configureProperty(String.format("hystrix.command.%s.circuitBreaker.enabled", commandConfig.getName()), true);
        configureProperty(String.format("hystrix.command.%s.circuitBreaker.requestVolumeThreshold", commandConfig.getName()), commandConfig.getCircuitBreaker().getAcceptableFailuresInWindow());
        configureProperty(String.format("hystrix.command.%s.circuitBreaker.errorThresholdPercentage", commandConfig.getName()), commandConfig.getCircuitBreaker().getErrorThreshold());
        configureProperty(String.format("hystrix.command.%s.circuitBreaker.sleepWindowInMilliseconds", commandConfig.getName()), commandConfig.getCircuitBreaker().getWaitTimeBeforeRetry());

        configureProperty(String.format("hystrix.command.%s.metrics.rollingStats.timeInMilliseconds", commandConfig.getName()), commandConfig.getMetrics().getStatsTimeInMillis());
        configureProperty(String.format("hystrix.command.%s.metrics.rollingStats.numBuckets", commandConfig.getName()), commandConfig.getMetrics().getNumBucketSize());
        configureProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.enabled", commandConfig.getName()), true);
        configureProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.timeInMilliseconds", commandConfig.getName()), commandConfig.getMetrics().getPercentileTimeInMillis());
        configureProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.numBuckets", commandConfig.getName()), commandConfig.getMetrics().getNumBucketSize());
        configureProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.bucketSize", commandConfig.getName()), commandConfig.getMetrics().getPercentileBucketSize());
        configureProperty(String.format("hystrix.command.%s.metrics.healthSnapshot.intervalInMilliseconds", commandConfig.getName()), commandConfig.getMetrics().getHealthCheckInterval());

        val commandProperties = HystrixCommandProperties.Setter()
                .withExecutionIsolationStrategy(isolationStrategy)
                .withFallbackEnabled(commandConfig.isFallbackEnabled())
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerErrorThresholdPercentage(commandConfig.getCircuitBreaker().getErrorThreshold())
                .withCircuitBreakerRequestVolumeThreshold(commandConfig.getCircuitBreaker().getAcceptableFailuresInWindow())
                .withCircuitBreakerSleepWindowInMilliseconds(commandConfig.getCircuitBreaker().getWaitTimeBeforeRetry())
                .withExecutionTimeoutInMilliseconds(commandConfig.getThreadPool().getTimeout())
                .withMetricsHealthSnapshotIntervalInMilliseconds(commandConfig.getMetrics().getHealthCheckInterval())
                .withMetricsRollingPercentileBucketSize(commandConfig.getMetrics().getPercentileBucketSize())
                .withMetricsRollingPercentileWindowInMilliseconds(commandConfig.getMetrics().getPercentileTimeInMillis());

        if (semaphoreIsolation) {
            commandProperties.withExecutionIsolationSemaphoreMaxConcurrentRequests(commandConfig.getThreadPool().getConcurrency())
                    .withFallbackIsolationSemaphoreMaxConcurrentRequests(commandConfig.getThreadPool().getConcurrency());
        }

        HystrixCommand.Setter commandConfiguration = HystrixCommand.Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandConfig.getName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandConfig.getName()))
                .andCommandPropertiesDefaults(commandProperties);

        if (!semaphoreIsolation) {
            val globalPool = commandConfig.getThreadPool() != null ? commandConfig.getThreadPool().getPool() : null;
            /*
                If an explicit pool is defined, it should be present in pool list otherwise
                a pool with name same as command would be defined
            */
            HystrixThreadPoolProperties.Setter poolConfiguration;
            val poolName = globalPool != null ? globalPoolId(globalPool) : commandPoolId(command);
            if (globalPool != null) {
                poolConfiguration = poolCache.get(poolName);
                if (poolConfiguration == null) {
                    val message = String.format("Undefined global pool [%s] used in command [%s]", globalPool, command);
                    log.error(message);
                    throw new RuntimeException(message);
                }
            } else {
                configureThreadPoolIfMissing(poolName, toPoolConfig(commandConfig.getThreadPool()));
            }

            commandConfiguration.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(poolName));
        }
        commandCache.put(commandConfig.getName(), commandConfiguration);
        threadPoolCache.put(commandConfig.getName(), commandConfig);
    }

    private static ThreadPoolConfig toPoolConfig(CommandThreadPoolConfig threadPool) {
        return ThreadPoolConfig.builder()
                .concurrency(threadPool.getConcurrency())
                .dynamicRequestQueueSize(threadPool.getDynamicRequestQueueSize())
                .maxRequestQueueSize(threadPool.getMaxRequestQueueSize())
                .build();
    }

    private static void configureThreadPoolIfMissing(String poolName, ThreadPoolConfig poolConfig) {
        if (poolCache.containsKey(poolName)) {
            return;
        }

        configureProperty(String.format("hystrix.threadpool.%s.coreSize", poolName), poolConfig.getConcurrency());
        configureProperty(String.format("hystrix.threadpool.%s.maximumSize", poolName), poolConfig.getConcurrency());
        configureProperty(String.format("hystrix.threadpool.%s.maxQueueSize", poolName), poolConfig.getMaxRequestQueueSize());
        configureProperty(String.format("hystrix.threadpool.%s.queueSizeRejectionThreshold", poolName), poolConfig.getDynamicRequestQueueSize());

        poolCache.put(poolName, HystrixThreadPoolProperties.Setter()
                .withCoreSize(poolConfig.getConcurrency())
                .withMaximumSize(poolConfig.getConcurrency())
                .withMaxQueueSize(poolConfig.getMaxRequestQueueSize())
                .withQueueSizeRejectionThreshold(poolConfig.getDynamicRequestQueueSize()));
    }

    private void validateUniqueCommands(HystrixConfiguratorConfig config) {
        if (config.getCommands() == null) {
            return;
        }

        AtomicBoolean duplicatePresent = new AtomicBoolean(false);
        config.getCommands().stream()
                .collect(Collectors.groupingBy(HystrixCommandConfig::getName, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(x -> x.getValue() > 1)
                .forEach(x -> {
                    log.warn("Duplicate command configuration for command [{}]", x.getKey());
                    duplicatePresent.set(true);
                });
        if (duplicatePresent.get()) {
            throw new RuntimeException("Duplicate Hystrix Command Configurations");
        }
    }

    private static void configureProperty(String property, int value) {
        ConfigurationManager.getConfigInstance().setProperty(property, value);
    }

    private static void configureProperty(String property, String value) {
        ConfigurationManager.getConfigInstance().setProperty(property, value);
    }

    private static void configureProperty(String property, boolean value) {
        ConfigurationManager.getConfigInstance().setProperty(property, value);
    }

    public static HystrixCommand.Setter getCommandConfiguration(final String commandName,
                                                                final HystrixCommandConfig commandConfig) {
        if (commandCache == null) {
            throw new IllegalStateException("Factory not initialized");
        }
        if (!commandCache.containsKey(commandName) ||
                !threadPoolCache.get(commandName).getThreadPool().isEqual(commandConfig.getThreadPool())) {
            registerCommandProperties(defaultConfig, commandConfig);
        }
        return commandCache.get(commandName);
    }

    public static ConcurrentHashMap<String, HystrixCommand.Setter> getCommandCache() {
        return commandCache;
    }

    public static ConcurrentHashMap<String, HystrixThreadPoolProperties.Setter> getPoolCache() {
        return poolCache;
    }
}

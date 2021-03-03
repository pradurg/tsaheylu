package io.durg.tsaheylu.core;


import com.netflix.hystrix.HystrixCommand;
import io.durg.tsaheylu.core.config.HystrixCommandConfig;

/**
 * @author phaneesh
 * @author Manas Mulay
 */
// MARCH-2021 - Manas - modified parameters to getCommandConfiguration
public abstract class BaseCommand<T> extends HystrixCommand<T> {

    public BaseCommand(final String name,
                       final HystrixCommandConfig commandConfig) {
        super(TsaheyluHystrixCircuitBreaker.getCommandConfiguration(name, commandConfig));
    }

}

package io.durg.tsaheylu.core;


import com.netflix.hystrix.HystrixCommand;

/**
 * Created by manas.mulay on 28/02/21
 */
public abstract class BaseCommand<T> extends HystrixCommand<T> {

    public BaseCommand(final String name) {
        super(HystrixCommandBuilder.getCommandConfiguration(name));
    }

}

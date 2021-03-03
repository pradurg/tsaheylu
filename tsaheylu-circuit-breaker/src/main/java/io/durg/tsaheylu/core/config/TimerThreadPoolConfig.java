package io.durg.tsaheylu.core.config;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author phaneesh
 * @author Manas Mulay - removed TimerThreadPoolConfigBuilder
 */
// MARCH-2021 - Manas - no changes
@Data
@NoArgsConstructor
public class TimerThreadPoolConfig {

    private int concurrency = Runtime.getRuntime().availableProcessors();

    @Builder
    public TimerThreadPoolConfig(int concurrency) {
        this.concurrency = concurrency;
    }


    public static class TimerThreadPoolConfigBuilder {

        private int concurrency = Runtime.getRuntime().availableProcessors();

    }
}

package io.durg.tsaheylu.circuitbreaker.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Created by manas.mulay on 04/03/21
 */
@NoArgsConstructor
public class ResilienceConfigurationWrapper {

    CircuitBreakerConfig circuitBreakerConfig;

    public ResilienceConfigurationWrapper(final io.durg.tsaheylu.circuitbreaker.config.hystrix.CircuitBreakerConfig
                                                hystrixCircuitBreakerConfig) {
        circuitBreakerConfig  = CircuitBreakerConfig.custom()
                .failureRateThreshold(hystrixCircuitBreakerConfig.getErrorThreshold())
                .waitDurationInOpenState(Duration.of(
                        hystrixCircuitBreakerConfig.getWaitTimeBeforeRetry(),
                        ChronoUnit.MILLIS))
                .build();
    }
}

package io.durg.tsaheylu;

import io.durg.tsaheylu.metrics.MetricMonitor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
public class HealthMetricManager {

    @Singular
    private final List<MetricMonitor> monitors;

    private final Function<List<MetricMonitor>, Double> healthMetricFunction = new SigmoidHealthMetricFunction(); //default

    public double getMetricValue() {
        return healthMetricFunction.apply(monitors);
    }

}

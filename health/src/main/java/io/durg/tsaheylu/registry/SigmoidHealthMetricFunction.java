package io.durg.tsaheylu.registry;

import io.durg.tsaheylu.registry.metrics.MetricMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class SigmoidHealthMetricFunction implements Function<List<MetricMonitor>, Double> {

    @Override
    public Double apply(List<MetricMonitor> metricMonitors) {
        double healthMetric = metricMonitors.stream()
                .filter(Objects::nonNull)
                .filter(metricMonitor -> metricMonitor.getMetric() != null && metricMonitor.getMetric() != Double.NaN)
                .map(monitor -> monitor.getWeight() * monitor.getMetric())
                .map(x -> (1 / (1 + Math.pow(Math.E, (-1 * x)))))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
        log.info("HealthMetric value : {}", healthMetric);
        return healthMetric;
    }
}

package io.durg.tsaheylu.registry;

import io.durg.tsaheylu.registry.metrics.MetricMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class SigmoidHealthMetricFunction implements Function<List<MetricMonitor>, Double> {

    @Override
    public Double apply(List<MetricMonitor> metricMonitors) {
        List<MetricMonitor> monitors = metricMonitors.stream()
                .filter(Objects::nonNull)
                .filter(metricMonitor -> metricMonitor.getMetric() != null && metricMonitor.getMetric() != Double.NaN)
                .collect(Collectors.toList());

        double sum = monitors.stream()
                .map(monitor -> monitor.getWeight() * monitor.getMetric())
                .map(x -> (1 / (1 + Math.pow(Math.E, (-1 * x)))))
                .mapToDouble(Double::doubleValue)
                .sum();
        double den = monitors.stream()
                .map(MetricMonitor::getWeight)
                .mapToDouble(Double::doubleValue)
                .map(x -> x < 0 ? -x : x)
                .sum();

        double healthMetric = sum / den;

        log.info("HealthMetric value : {}", healthMetric);
        return healthMetric;
    }
}

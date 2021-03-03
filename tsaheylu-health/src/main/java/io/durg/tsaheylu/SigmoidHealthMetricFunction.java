package io.durg.tsaheylu;

import io.durg.tsaheylu.metrics.MetricMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SigmoidHealthMetricFunction implements Function<List<MetricMonitor>, Double> {

    @Override
    public Double apply(List<MetricMonitor> metricMonitors) {
        Stream<MetricMonitor> availableMonitors = metricMonitors.stream()
                .filter(Objects::nonNull)
                .filter(metricMonitor -> metricMonitor.getMetric() != null && metricMonitor.getMetric() != Double.NaN);
        List<MetricMonitor> monitorList = availableMonitors.collect(Collectors.toList());
        if (monitorList.size() == 0) {
            log.warn("No metrics available");
            return Double.NaN;
        }
        double healthMetric = monitorList.stream()
                .map(monitor -> monitor.getWeight() * monitor.getMetric())
                .map(x -> (1 / (1 + Math.pow(Math.E, (-1 * x)))))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
        log.info("HealthMetric value : {}", healthMetric);
        return healthMetric;
    }
}

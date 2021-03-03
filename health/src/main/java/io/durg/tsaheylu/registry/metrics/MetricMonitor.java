package io.durg.tsaheylu.registry.metrics;

public interface MetricMonitor {

    default double getWeight() {
        return 1;
    }

    Double getMetric();
}

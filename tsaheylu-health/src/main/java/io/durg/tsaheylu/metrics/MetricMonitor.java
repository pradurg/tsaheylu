package io.durg.tsaheylu.metrics;

public interface MetricMonitor {

    default double getWeight() {
        return 1;
    }

    Double getMetric();
}

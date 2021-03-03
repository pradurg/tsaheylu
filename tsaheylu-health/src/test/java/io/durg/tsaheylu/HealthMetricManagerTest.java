package io.durg.tsaheylu;

import io.durg.tsaheylu.metrics.JVMHeapSizeMetricMonitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HealthMetricManagerTest {

    @Test
    void test() {
        HealthMetricManager manager = HealthMetricManager.builder()
                .monitor(() -> null)
                .build();
        double metricValue = manager.getMetricValue();
        Assertions.assertEquals(Double.NaN, metricValue);
        Assertions.assertTrue(manager.getHealthMetricFunction() instanceof SigmoidHealthMetricFunction);
    }

    @Test
    void testWithJVMMetric() {
        HealthMetricManager manager = HealthMetricManager.builder()
                .monitor(new JVMHeapSizeMetricMonitor())
                .build();
        double metricValue = manager.getMetricValue();
        Assertions.assertNotEquals(Double.NaN, metricValue);
        Assertions.assertTrue(manager.getHealthMetricFunction() instanceof SigmoidHealthMetricFunction);
    }
}
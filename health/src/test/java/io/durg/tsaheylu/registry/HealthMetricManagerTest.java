package io.durg.tsaheylu.registry;

class HealthMetricManagerTest {

    /*@Test
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
                .monitor(() -> null)
                .build();
        double metricValue = manager.getMetricValue();
        Assertions.assertNotEquals(Double.NaN, metricValue);
        Assertions.assertTrue(manager.getHealthMetricFunction() instanceof SigmoidHealthMetricFunction);
    }*/
}
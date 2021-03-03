package io.durg.tsaheylu.registry.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

@Builder
@AllArgsConstructor
public class JVMHeapSizeMetricMonitor implements MetricMonitor {

    private final MemoryMXBean mxBean;
    private final List<MemoryPoolMXBean> memoryPools;

    public JVMHeapSizeMetricMonitor() {
        this.mxBean = ManagementFactory.getMemoryMXBean();
        this.memoryPools = ManagementFactory.getMemoryPoolMXBeans();
    }

    @Override
    public double getWeight() {
        return -1;
    }

    @Override
    public Double getMetric() {
        MemoryUsage heapMemoryUsage = this.mxBean.getHeapMemoryUsage();
        return ((double) heapMemoryUsage.getUsed() / heapMemoryUsage.getMax());
    }
}

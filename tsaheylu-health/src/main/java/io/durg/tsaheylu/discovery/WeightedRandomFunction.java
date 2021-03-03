package io.durg.tsaheylu.discovery;

import io.durg.tsaheylu.model.NodeData;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WeightedRandomFunction implements Function<List<NodeData>, Integer> {
    @Override
    public Integer apply(List<NodeData> nodeData) {
        return new EnumeratedIntegerDistribution(IntStream.range(0, nodeData.size()).toArray(), nodeData.stream()
                .mapToDouble(NodeData::getHealthMetric).toArray()).sample();
    }
}

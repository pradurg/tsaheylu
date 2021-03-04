package io.durg.tsaheylu.discovery;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class WeightedRandomFunction implements Function<List<Double>, Integer> {
    @Override
    public Integer apply(List<Double> weights) {
        if(weights.stream().allMatch(weight -> weight == 0.0)){
            return -1;
        }
        return new EnumeratedIntegerDistribution(IntStream.range(0, weights.size()).toArray(), weights.stream()
                .mapToDouble(Double::new).toArray()).sample();
    }
}

package io.durg.tsaheylu.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import java.util.Iterator;
import java.util.NoSuchElementException;

@Builder
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class NodeData implements Iterable<NodeData> {
    private static final String SEPARATOR = ".";

    @Builder
    public NodeData(String environment, double healthMetric, String version) {
        this.environment = environment;
        this.healthMetric = healthMetric;
        this.version = version;
    }

    private String environment;
    private double healthMetric;
    private String version;

    public NodeData(String environment) {
        this.environment = environment;
    }

    @Override
    public Iterator<NodeData> iterator() {
        return new NodeDataIterator(environment);
    }

    public static final class NodeDataIterator implements Iterator<NodeData> {

        private String remainingEnvironment;

        public NodeDataIterator(String remainingEnvironment) {
            this.remainingEnvironment = remainingEnvironment;
        }

        @Override
        public boolean hasNext() {
            return remainingEnvironment !=null && !remainingEnvironment.isEmpty();
        }

        @Override
        public NodeData next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            val shardInfo = new NodeData(remainingEnvironment);
            val sepIndex = remainingEnvironment.indexOf(SEPARATOR);
            remainingEnvironment = sepIndex < 0 ? "" : remainingEnvironment.substring(0, sepIndex);
            return shardInfo;
        }
    }
}

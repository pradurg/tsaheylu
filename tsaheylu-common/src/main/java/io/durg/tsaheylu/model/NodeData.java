package io.durg.tsaheylu.model;

import lombok.Builder;

@Builder
public class NodeData {
    private String environment;
    private double healthMetric;
    private String version;
}

package io.durg.tsaheylu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class NodeData {
    private String environment;
    private double healthMetric;
    private String version;
}

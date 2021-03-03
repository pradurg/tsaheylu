package io.durg.tsaheylu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
public class NodeData {
    private String environment;
    private double healthMetric;
    private String version;
}

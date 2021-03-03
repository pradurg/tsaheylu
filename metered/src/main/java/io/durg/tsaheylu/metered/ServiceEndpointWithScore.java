package io.durg.tsaheylu.metered;

public class ServiceEndpointWithScore {

    ServiceEndpoint serviceEndpoint;
    double score;

    public ServiceEndpoint getServiceEndpoint() {
        return serviceEndpoint;
    }

    public double getScore() {
        return score;
    }

    public ServiceEndpointWithScore(ServiceEndpoint serviceEndpoint, double score) {
        this.serviceEndpoint = serviceEndpoint;
        this.score = score;
    }
}

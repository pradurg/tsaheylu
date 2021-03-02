package io.durg.tsaheylu.httpclient;

import okhttp3.HttpUrl;

public class EndpointErrorKey implements ErrorKey {
    private int port;
    private String host;
    private String scheme;

    public EndpointErrorKey(HttpUrl url) {
        this.host = url.host();
        this.port = url.port();
        this.scheme = url.scheme();
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getScheme() {
        return scheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointErrorKey that = (EndpointErrorKey) o;

        if (port != that.port) return false;
        if (!host.equals(that.host)) return false;
        return scheme.equals(that.scheme);
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + host.hashCode();
        result = 31 * result + scheme.hashCode();
        return result;
    }
}

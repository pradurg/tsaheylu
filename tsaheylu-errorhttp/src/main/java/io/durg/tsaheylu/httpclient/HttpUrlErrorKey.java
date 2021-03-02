package io.durg.tsaheylu.httpclient;

import com.google.common.base.Preconditions;
import okhttp3.HttpUrl;

public class HttpUrlErrorKey implements ErrorKey{
    private HttpUrl url;

    public HttpUrlErrorKey(HttpUrl url) {
        Preconditions.checkNotNull(url);
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpUrlErrorKey that = (HttpUrlErrorKey) o;

        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}

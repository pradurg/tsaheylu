package io.durg.tsaheylu.httpclient;

import okhttp3.OkHttpClient;

public class ErrorAwareHttpClientFactory {

    public static OkHttpClient build(OkHttpClient client) {
        return client.newBuilder()
                .eventListener(new ErrorEventListener())
                .build();
    }

    public static OkHttpClient build() {
        return new OkHttpClient.Builder()
                .eventListener(new ErrorEventListener())
                .build();
    }
}

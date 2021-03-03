package io.durg.tsaheylu.metered;

import okhttp3.OkHttpClient;

public class ClientFactory {

    private ClientFactory() {
    }

    public static OkHttpClient buildHttpClient(OkHttpClient client, Configuration configuration, ErrorRegistry errorRegistry) {
        return client.newBuilder()
                .eventListener(new ErrorEventListener(errorRegistry, configuration.getSuccessPredicate()))
                .build();
    }


}

package io.durg.tsaheylu.httpclient.localtesting;

import okhttp3.Connection;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

class LoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        final Connection connection = chain.connection();
        final HttpUrl url1 = request.url();
        System.out.println(String.format("Sending request %s on %s%n%s",
                url1, connection, request.headers()));

//        final HttpUrl url = connection.route().address().url();
//        System.out.println(url1.equals(url));

        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            System.out.println("erorro hua aaa");
            e.printStackTrace();
            throw e;
        }

        long t2 = System.nanoTime();
        System.out.println(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}
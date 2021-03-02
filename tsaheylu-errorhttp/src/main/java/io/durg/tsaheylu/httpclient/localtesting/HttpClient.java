package io.durg.tsaheylu.httpclient.localtesting;

import io.durg.tsaheylu.httpclient.Configuration;
import io.durg.tsaheylu.httpclient.ErrorRegistry;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    final static Configuration configuration = Configuration.ConfigurationBuilder.newConfiguration()
            .withErrorPercentageThreshold(0.5)
            .withTimeWindowInSeconds(5)
            .withRecordErrorRateAtURL(false)
            .build();

    public static final ErrorRegistry errorRegistry = new ErrorRegistry(configuration);
    final static OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.SECONDS)
//            .addNetworkInterceptor(new LoggingInterceptor())
            .eventListener(new LoggingEventListener(errorRegistry))
            .build();

    public static void main(String[] args) throws InterruptedException {
//        System.out.println(new AtomicLong(0) == new AtomicLong(0));
        makeParallelCalls();
        errorRegistry.inspectCache();
//        getCall(client);

//        delayCall(client);

//        cancelCall();
    }

    private static void makeParallelCalls() throws InterruptedException {

        final ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 150; i++) {
            Request request;
            if (i % 5 == 0 ) {
                request = new Request.Builder()
                        .url("https://httpbin.org/delay/3")
                        .build();
            } else if (i % 4 == 0) {

                request = new Request.Builder()
                        .url("https://httpbin.org/status/400")
                        .build();
            } else {

                request = new Request.Builder()
                        .url("https://httpbin.org/status/200")
                        .build();
            }
            executorService.execute(() -> {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
//                        System.out.println("exception occur");
//                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        System.out.println("response successful");
                    }
                });
            });
            if (i % 3 == 0)
                Thread.sleep(500);
            else if (i % 5 == 0)
                Thread.sleep(200);
        }

        executorService.awaitTermination(10, TimeUnit.SECONDS);
        executorService.shutdown();
//        executorService.awaitTermination(10, TimeUnit.SECONDS);

//        getCall(client);
//        getCall(client);
//        getCall(client);
//        getCall(client);
    }

    private static void delayCall(OkHttpClient client) {
        final OkHttpClient httpClient = client.newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();


        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2") // This URL is served with a 2 second delay.
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            System.out.println("Response completed: " + response);
        } catch (IOException e) {
            System.out.println("response failed to complete");
            e.printStackTrace();
        }
    }

    private static void getCall(OkHttpClient client) {
        Request request = new Request.Builder()
//                .url("https://nexus.traefik.stg.phonepe.nb6/swagger#")
//                .url("https://httpbin.org/status/500")
//                .url("https://httpbin.org/links/5/3")
//                .url("http://www.publicobject.com/helloworld.txt")
                .url("https://publicobject.com/helloworld.txt")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cancelCall() throws InterruptedException {
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2") // This URL is served with a 2 second delay.
                .build();

        final long startNanos = System.nanoTime();
        final Call call = httpClient.newCall(request);

        // Schedule a job to cancel the call in 1 second.
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.printf("%.2f Canceling call.%n", (System.nanoTime() - startNanos) / 1e9f);
                call.cancel();
                System.out.printf("%.2f Canceled call.%n", (System.nanoTime() - startNanos) / 1e9f);
            }
        }, 1, TimeUnit.SECONDS);

        System.out.printf("%.2f Executing call.%n", (System.nanoTime() - startNanos) / 1e9f);
        try (Response response = call.execute()) {
            System.out.printf("%.2f Call was expected to fail, but completed: %s%n",
                    (System.nanoTime() - startNanos) / 1e9f, response);
        } catch (IOException e) {
            System.out.printf("%.2f Call failed as expected: %s%n",
                    (System.nanoTime() - startNanos) / 1e9f, e);
        } finally {
            executor.awaitTermination(4, TimeUnit.SECONDS);
            executor.shutdown();
        }

    }
}

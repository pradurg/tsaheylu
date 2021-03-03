package io.durg.tsaheylu.httpclient;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import okhttp3.HttpUrl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ErrorRegistry {
    //    Map<HttpUrl, AtomicLong> errorMapCount =  new ConcurrentHashMap<>();
    final Cache<ServiceEndpoint, ConcurrentMap<Long, CountPair>> requestsCount; // map of host:port to map of timestamp in seconds and errorcount at that second.
    private final int window;
    private final TimeUnit timeUnit;
    private final double threshold;

    public ErrorRegistry(Configuration configuration) {
        timeUnit = configuration.getTimeUnit();
        window = configuration.getWindow();
        threshold = configuration.getErrorPercentageThreshold();
        requestsCount = CacheBuilder.newBuilder()
                .build();
    }

    public void recordError(HttpUrl url) {
//        System.out.println("recording error event");
        long timeKey = getTimeKey();
        ServiceEndpoint key = getErrorKey(url);
        requestsCount.asMap()
                .computeIfAbsent(key, errKey -> new ConcurrentHashMap<>())
                .computeIfAbsent(timeKey, timestampInSeconds -> new CountPair(0, 0))
                .incrementErrorCount();
    }

    public void recordHttpCall(HttpUrl url) {
//        System.out.println("recording error event");
        ServiceEndpoint key = getErrorKey(url);
        long timeKey = getTimeKey();
        requestsCount.asMap()
                .computeIfAbsent(key, errKey -> new ConcurrentHashMap<>())
                .computeIfAbsent(timeKey, timestampInSeconds -> new CountPair(0, 0))
                .incrementTotalCount();
    }

    private long getTimeKey() {
        long currentTime = Instant.now().getEpochSecond();
        return timeUnit.equals(TimeUnit.MINUTES) ? currentTime / 60 : currentTime; // only seconds and minute is supported
    }

    private ServiceEndpoint getErrorKey(HttpUrl url) {
        return new ServiceEndpoint(url);
    }


    public List<ServiceEndpointWithScore> filter(List<ServiceEndpoint> endpoints) {
        return endpoints.stream()
                .map(endpoint -> {
                    double percentage = getScore(endpoint);
                    return new ServiceEndpointWithScore(endpoint, percentage);
                })
                .sorted(Comparator.comparingDouble(o -> o.score))
                .filter(endpointWithScore -> endpointWithScore.score < threshold)
                .collect(Collectors.toList());
    }


    public double getScore(ServiceEndpoint endpoint) {
        long currentTime = getTimeKey();
        if (!requestsCount.asMap().containsKey(endpoint)) {
            System.out.println("key not found");
            return 0;
        }
        final CountPair sum = requestsCount.asMap().get(endpoint).entrySet().stream()
                .filter(entry -> currentTime - entry.getKey() <= window)
                .reduce(new CountPair(0, 0),
                        (accumulator, entry) -> accumulator.add(entry.getValue()),
                        CountPair::add
                );
        if (sum.getTotalCount() == 0) return 0;
        double errorPercentageInLastTimeWindow = sum.getErrorCount() * 1.0 / sum.getTotalCount();

        cleanupOldEntries(endpoint, currentTime - 2 * window);

        return errorPercentageInLastTimeWindow;
    }

    private void cleanupOldEntries(ServiceEndpoint errorKey, long lessThanTimestamp) {
        requestsCount.asMap().get(errorKey).entrySet().removeIf(entry -> entry.getKey() <= lessThanTimestamp);
    }

    public void inspectCache() {
        requestsCount.asMap().forEach((k, v) -> {
            final ServiceEndpoint k1 = k;
            System.out.println(k1.getHost() + k1.getPort() + k1.getScheme());
            ArrayList<Map.Entry<Long, CountPair>> list = new ArrayList<>(v.entrySet());
            list.sort(Map.Entry.comparingByKey());
            list.forEach(entry -> {
                System.out.println(entry.getKey() + " " + entry.getValue());
            });
        });
    }

    private static class CountPair {
        private final AtomicInteger errorCount;
        private final AtomicInteger totalCount;

        public CountPair(int errorCount, int totalCount) {
            this.errorCount = new AtomicInteger(errorCount);
            this.totalCount = new AtomicInteger(totalCount);
        }

        public int getErrorCount() {
            return errorCount.get();
        }

        public int getTotalCount() {
            return totalCount.get();
        }

        long incrementErrorCount() {
            return this.errorCount.addAndGet(1);
        }

        long incrementTotalCount() {
            return this.totalCount.addAndGet(1);
        }

        CountPair add(CountPair other) {
            return new CountPair(
                    this.errorCount.addAndGet(other.getErrorCount()),
                    this.totalCount.addAndGet(other.getTotalCount())
            );
        }

        @Override
        public String toString() {
            return "errorCount=" + errorCount.get() +
                    ", totalCount=" + totalCount.get() +
                    '}';
        }
    }

}


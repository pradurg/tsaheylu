package io.durg.tsaheylu.httpclient;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import okhttp3.HttpUrl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ErrorRegistry {
    //    Map<HttpUrl, AtomicLong> errorMapCount =  new ConcurrentHashMap<>();
    final Cache<ErrorKey, ConcurrentMap<Long, CountPair>> requestsCount; // map of host:port to map of timestamp in seconds and errorcount at that second.
    final boolean recordErrorRateAtURL;
    private final int timeWindowInSeconds;

    public ErrorRegistry(Configuration configuration) {
        recordErrorRateAtURL = configuration.isRecordErrorRateAtURL();
        timeWindowInSeconds = configuration.getTimeWindowInSeconds();
        requestsCount = CacheBuilder.newBuilder()
                .expireAfterAccess(configuration.getTimeWindowInSeconds() * 2, TimeUnit.SECONDS)
                .build();
    }

    public void recordError(HttpUrl url) {
//        System.out.println("recording error event");
        long currentTime = Instant.now().getEpochSecond();
        ErrorKey key = getErrorKey(url);
        requestsCount.asMap()
                .computeIfAbsent(key, errKey -> new ConcurrentHashMap<>())
                .computeIfAbsent(currentTime, timestampInSeconds -> new CountPair(0, 0))
                .incrementErrorCount();
    }

    public void recordHttpCall(HttpUrl url) {
//        System.out.println("recording error event");
        long currentTime = Instant.now().getEpochSecond();
        ErrorKey key = getErrorKey(url);
        requestsCount.asMap()
                .computeIfAbsent(key, errKey -> new ConcurrentHashMap<>())
                .computeIfAbsent(currentTime, timestampInSeconds -> new CountPair(0, 0))
                .incrementTotalCount();
    }

    private ErrorKey getErrorKey(HttpUrl url) {
        return recordErrorRateAtURL ? new HttpUrlErrorKey(url) : new EndpointErrorKey(url);
    }

    public double getErrorPercentage(HttpUrl url) {
        long currentTime = Instant.now().getEpochSecond();
        ErrorKey errorKey = getErrorKey(url);
        if (!requestsCount.asMap().containsKey(errorKey)) {
            return 0;
        }
        final CountPair sum = requestsCount.asMap().get(errorKey).entrySet().stream()
                .filter(entry -> currentTime - entry.getKey() <= timeWindowInSeconds)
                .reduce(new CountPair(0, 0),
                        (accumulator, entry) -> accumulator.add(entry.getValue()),
                        CountPair::add
                );
        if (sum.getTotalCount() == 0) return 0;
        double errorPercentageInLastTimeWindow = sum.getErrorCount() * 1.0 / sum.getTotalCount();

        cleanupOldEntries(errorKey, currentTime - 2 * timeWindowInSeconds);

        return errorPercentageInLastTimeWindow;
    }

    private void cleanupOldEntries(ErrorKey errorKey, long lessThanTimestamp) {
        requestsCount.asMap().get(errorKey).entrySet().removeIf(entry -> entry.getKey() <= lessThanTimestamp);
    }

    public void inspectCache() {
        requestsCount.asMap().forEach((k, v) -> {
            final EndpointErrorKey k1 = (EndpointErrorKey) k;
            System.out.println(k1.getHost());
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

        public int getErrorCount() {
            return errorCount.get();
        }

        public int getTotalCount() {
            return totalCount.get();
        }

        public CountPair(int errorCount, int totalCount) {
            this.errorCount = new AtomicInteger(errorCount);
            this.totalCount = new AtomicInteger(totalCount);
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

package io.durg.tsaheylu.httpclient;

import java.util.concurrent.TimeUnit;

public class Configuration {
    private boolean recordErrorRateAtURL; // default will be recorded at host, port, scheme(http/https) level
    private Double errorPercentageThreshold; // eg 40% if 20 out of 50 requests failed in the time_window
    private Integer window; // sliding time window to consider.
    private TimeUnit timeUnit;

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public boolean isRecordErrorRateAtURL() {
        return recordErrorRateAtURL;
    }

    public double getErrorPercentageThreshold() {
        return errorPercentageThreshold;
    }

    public int getWindow() {
        return window;
    }


    public static final class ConfigurationBuilder {
        private boolean recordErrorRateAtURL; // default will be recorded at host, port, scheme(http/https) level
        private Double errorPercentageThreshold; // eg 40% if 20 out of 50 requests failed in the time_window
        private Integer window; // sliding time window to consider.
        private TimeUnit timeUnit;

        private ConfigurationBuilder() {
        }

        public static ConfigurationBuilder newConfiguration() {
            return new ConfigurationBuilder();
        }

        public ConfigurationBuilder withRecordErrorRateAtURL(boolean recordErrorRateAtURL) {
            this.recordErrorRateAtURL = recordErrorRateAtURL;
            return this;
        }

        public ConfigurationBuilder withErrorPercentageThreshold(Double errorPercentageThreshold) {
            this.errorPercentageThreshold = errorPercentageThreshold;
            return this;
        }

        public ConfigurationBuilder withWindow(Integer window) {
            this.window = window;
            return this;
        }

        public ConfigurationBuilder withTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public Configuration build() {
            Configuration configuration = new Configuration();
            configuration.errorPercentageThreshold = this.errorPercentageThreshold;
            configuration.timeUnit = this.timeUnit;
            configuration.recordErrorRateAtURL = this.recordErrorRateAtURL;
            configuration.window = this.window;
            return configuration;
        }
    }
}

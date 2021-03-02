package io.durg.tsaheylu.httpclient;

public class Configuration {
    private boolean recordErrorRateAtURL; // default will be recorded at host, port, scheme(http/https) level
    private Double errorPercentageThreshold; // eg 40% if 20 out of 50 requests failed in the time_window
    private Integer timeWindowInSeconds; // sliding time window to consider.

    public boolean isRecordErrorRateAtURL() {
        return recordErrorRateAtURL;
    }

    public double getErrorPercentageThreshold() {
        return errorPercentageThreshold;
    }

    public int getTimeWindowInSeconds() {
        return timeWindowInSeconds;
    }

    public static final class ConfigurationBuilder {
        private boolean recordErrorRateAtURL; // default false.
        private double errorPercentageThreshold; // eg 40% if 20 out of 50 requests failed in the time_window
        private int timeWindowInSeconds; // sliding time window to consider.

        private ConfigurationBuilder() {
        }

        public static ConfigurationBuilder newConfiguration() {
            return new ConfigurationBuilder();
        }

        public ConfigurationBuilder withRecordErrorRateAtURL(boolean recordErrorRateAtURL) {
            this.recordErrorRateAtURL = recordErrorRateAtURL;
            return this;
        }

        public ConfigurationBuilder withErrorPercentageThreshold(double errorPercentageThreshold) {
            this.errorPercentageThreshold = errorPercentageThreshold;
            return this;
        }

        public ConfigurationBuilder withTimeWindowInSeconds(int timeWindowInSeconds) {
            this.timeWindowInSeconds = timeWindowInSeconds;
            return this;
        }

        public Configuration build() {
            Configuration configuration = new Configuration();
            configuration.recordErrorRateAtURL = recordErrorRateAtURL;
            configuration.timeWindowInSeconds = this.timeWindowInSeconds;
            configuration.errorPercentageThreshold = this.errorPercentageThreshold;
            return configuration;
        }
    }
}

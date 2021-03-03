package io.durg.tsaheylu.httpclient;

import java.util.concurrent.TimeUnit;

public class Configuration {
    private Double errorPercentageThreshold; // eg 40% if 20 out of 50 requests failed in the time_window
    private Integer window; // sliding time window to consider.
    private TimeUnit timeUnit;
    private SuccessPredicate successPredicate;

    public SuccessPredicate getSuccessPredicate() {
        return successPredicate;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public double getErrorPercentageThreshold() {
        return errorPercentageThreshold;
    }

    public int getWindow() {
        return window;
    }


    public static final class ConfigurationBuilder {
        private Double errorPercentageThreshold; // eg 40% if 20 out of 50 requests failed in the time_window
        private Integer window; // sliding time window to consider.
        private TimeUnit timeUnit;
        private SuccessPredicate successPredicate;

        private ConfigurationBuilder() {
        }

        public static ConfigurationBuilder newConfiguration() {
            return new ConfigurationBuilder();
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

        public ConfigurationBuilder withSuccessPredicate(SuccessPredicate successPredicate) {
            this.successPredicate = successPredicate;
            return this;
        }

        public Configuration build() {
            Configuration configuration = new Configuration();
            configuration.errorPercentageThreshold = this.errorPercentageThreshold;
            configuration.timeUnit = this.timeUnit;
            configuration.window = this.window;
            configuration.successPredicate = this.successPredicate == null ? new DefaultSuccessPredicate() : this.successPredicate;
            return configuration;
        }
    }
}

package com.tosan.client.redis.configuration;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class TedissonRedisAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {
    private static final String REDIS_AUTO_CONFIGURATION = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration";
    private static final String CLIENT_TYPE_PROPERTY = "tedisson.redis-client-type";
    private static final String ENABLED_PROPERTY = "tedisson.redis.enabled";
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean excludeRedisAutoConfiguration = shouldExcludeRedisAutoConfiguration();
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            matches[i] = !excludeRedisAutoConfiguration || !REDIS_AUTO_CONFIGURATION.equals(autoConfigurationClasses[i]);
        }
        return matches;
    }

    private boolean shouldExcludeRedisAutoConfiguration() {
        boolean enabled = environment.getProperty(ENABLED_PROPERTY, Boolean.class, false);
        String clientType = environment.getProperty(CLIENT_TYPE_PROPERTY);
        return !enabled || !"LETTUCE".equalsIgnoreCase(clientType);
    }
}

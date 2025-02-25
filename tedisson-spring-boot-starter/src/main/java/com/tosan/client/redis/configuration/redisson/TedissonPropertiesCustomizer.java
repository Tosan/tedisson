package com.tosan.client.redis.configuration.redisson;

import org.redisson.config.Config;

/**
 * @author R.Mehri
 * @since 12/5/2023
 */
@FunctionalInterface
public interface TedissonPropertiesCustomizer {

    void customize(final Config config);
}
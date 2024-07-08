package com.tosan.client.redis.configuration;

import com.tosan.client.redis.enumuration.LocalCacheProvider;
import lombok.Data;

/**
 * @author R.Mehri
 * @since 5/30/2023
 */
@Data
public class LocalProperties {

    /**
     * Local cache provider
     */
    private LocalCacheProvider cacheProvider = LocalCacheProvider.CAFFEINE;
}

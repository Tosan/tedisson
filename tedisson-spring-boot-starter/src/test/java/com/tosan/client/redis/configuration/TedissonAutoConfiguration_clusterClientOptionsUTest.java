package com.tosan.client.redis.configuration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TedissonAutoConfiguration_clusterClientOptionsUTest {

    @Test
    void clusterClientOptions_refreshTopologyWhenANodeDrops() {
        DataRedisProperties properties = clusterProperties();

        ClientOptions options = TedissonAutoConfiguration.clusterClientOptions(properties);

        ClusterClientOptions clusterOptions = assertInstanceOf(ClusterClientOptions.class, options);
        assertTrue(clusterOptions.isAutoReconnect());
        assertTrue(clusterOptions.isPingBeforeActivateConnection());
        ClusterTopologyRefreshOptions refresh = clusterOptions.getTopologyRefreshOptions();
        assertTrue(refresh.isPeriodicRefreshEnabled());
        assertEquals(Duration.ofSeconds(30), refresh.getRefreshPeriod());
        assertEquals(ClusterTopologyRefreshOptions.DEFAULT_ADAPTIVE_REFRESH_TIMEOUT_DURATION, refresh.getAdaptiveRefreshTimeout());
        assertEquals(ClusterTopologyRefreshOptions.DEFAULT_REFRESH_TRIGGERS_RECONNECT_ATTEMPTS, refresh.getRefreshTriggersReconnectAttempts());
        assertTrue(refresh.isCloseStaleConnections());
        assertTrue(refresh.useDynamicRefreshSources());
        assertTrue(refresh.getAdaptiveRefreshTriggers().contains(RefreshTrigger.PERSISTENT_RECONNECTS));
        assertTrue(refresh.getAdaptiveRefreshTriggers().contains(RefreshTrigger.MOVED_REDIRECT));
        assertTrue(refresh.getAdaptiveRefreshTriggers().contains(RefreshTrigger.UNCOVERED_SLOT));
    }

    @Test
    void clusterClientOptions_usesConfiguredRefreshPeriod() {
        DataRedisProperties properties = clusterProperties();
        properties.getLettuce().getCluster().getRefresh().setPeriod(Duration.ofSeconds(15));
        properties.getLettuce().getCluster().getRefresh().setDynamicRefreshSources(false);

        ClusterTopologyRefreshOptions refresh = assertInstanceOf(ClusterClientOptions.class,
                TedissonAutoConfiguration.clusterClientOptions(properties)).getTopologyRefreshOptions();

        assertEquals(Duration.ofSeconds(15), refresh.getRefreshPeriod());
        assertFalse(refresh.useDynamicRefreshSources());
    }

    private static DataRedisProperties clusterProperties() {
        DataRedisProperties properties = new DataRedisProperties();
        DataRedisProperties.Cluster cluster = new DataRedisProperties.Cluster();
        cluster.setNodes(List.of("127.0.0.1:7000", "127.0.0.1:7001"));
        properties.setCluster(cluster);
        return properties;
    }
}

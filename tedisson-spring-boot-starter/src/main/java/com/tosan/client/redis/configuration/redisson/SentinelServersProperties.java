package com.tosan.client.redis.configuration.redisson;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.redisson.api.NatMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author R.Mehri
 * @since 1/7/2023
 */
//org.redisson.config.SentinelServersConfig
@Data
@EqualsAndHashCode(callSuper = true)
public class SentinelServersProperties extends BaseMasterSlaveServersProperties {

    /**
     * Redis Sentinel node address in host:port format.
     */
    private List<String> sentinelAddresses = new ArrayList<>();

    /**
     * Defines NAT mapper which maps Redis URI object.
     * Applied to all Redis connections.
     */
    private NatMapper natMapper = NatMapper.direct();

    /**
     * Master server name used by Redis Sentinel servers and master change monitoring task.
     */
    private String masterName;

    /**
     * Username required by the Redis Sentinel servers for authentication.
     */
    private String sentinelUsername;

    /**
     * Password required by the Redis Sentinel servers for authentication.
     * Used only if sentinel password differs from master and slave.
     */
    private String sentinelPassword;

    /**
     * Database index used for Redis connection
     */
    private int database = 0;

    /**
     * Sentinel scan interval in milliseconds
     */
    private int scanInterval = 1000;

    /**
     * Enables sentinels list check during Redisson startup.
     * <p>
     * Default is <code>true</code>
     */
    private boolean checkSentinelsList = true;

    /**
     * check node status from sentinel with 'master-link-status' flag
     * <p>
     * Default is <code>true</code>
     */
    private boolean checkSlaveStatusWithSyncing = true;

    /**
     * Enables sentinels discovery.
     * <p>
     * Default is <code>true</code>
     */
    private boolean sentinelsDiscovery = true;
}

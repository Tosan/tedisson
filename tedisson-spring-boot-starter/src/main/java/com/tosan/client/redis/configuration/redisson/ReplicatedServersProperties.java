package com.tosan.client.redis.configuration.redisson;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author R.Mehri
 * @since 1/7/2023
 */
//org.redisson.config.ReplicatedServersConfig
@Data
@EqualsAndHashCode(callSuper = true)
public class ReplicatedServersProperties extends BaseMasterSlaveServersProperties {

    /**
     * Replication group node urls list
     */
    private List<String> nodeAddresses = new ArrayList<>();

    /**
     * Replication group scan interval in milliseconds
     */
    private int scanInterval = 1000;

    /**
     * Database index used for Redis connection
     */
    private int database = 0;
}

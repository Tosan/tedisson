package com.tosan.client.redis.configuration.redisson;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author R.Mehri
 * @since 1/7/2023
 */
//org.redisson.config.MasterSlaveServersConfig
@Data
@EqualsAndHashCode(callSuper = true)
public class MasterSlaveServersProperties extends BaseMasterSlaveServersProperties {

    /**
     * Redis slave servers addresses
     */
    private Set<String> slaveAddresses = new HashSet<>();

    /**
     * Redis master server address
     */
    private String masterAddress;

    /**
     * Database index used for Redis connection
     */
    private int database = 0;
}

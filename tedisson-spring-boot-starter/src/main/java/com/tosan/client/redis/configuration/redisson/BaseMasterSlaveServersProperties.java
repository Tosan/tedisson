package com.tosan.client.redis.configuration.redisson;

import lombok.Data;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.redisson.connection.balancer.LoadBalancer;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;

/**
 * @author R.Mehri
 * @since 1/7/2023
 */
//org.redisson.config.BaseMasterSlaveServersConfig
@Data
public class BaseMasterSlaveServersProperties {

    /**
     * Connection load balancer for multiple Redis slave servers
     */
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    /**
     * Redis 'slave' node minimum idle connection amount for <b>each</b> slave node
     */
    private int slaveConnectionMinimumIdleSize = 24;

    /**
     * Redis 'slave' node maximum connection pool size for <b>each</b> slave node
     */
    private int slaveConnectionPoolSize = 64;

    /**
     * When the retry interval <code>failedSlavesReconnectionTimeout<code/>
     * reached Redisson tries to connect to failed Redis node reported by <code>failedSlaveCheckInterval</code>.
     * <p>
     * On every such timeout event Redisson tries
     * to connect to failed Redis server.
     * <p>
     * Default is 3000
     */
    private int failedSlaveReconnectionInterval = 3000;

    /**
     * Check failed slaves
     * time interval in milliseconds
     */
    private int failedSlaveCheckInterval = 180000;

    /**
     * Redis 'master' node minimum idle connection amount for <b>each</b> slave node
     */
    private int masterConnectionMinimumIdleSize = 24;

    /**
     * Redis 'master' node maximum connection pool size
     */
    private int masterConnectionPoolSize = 64;

    /**
     * Set node type used for read operation.
     * <p>
     * Default is <code>SLAVE</code>
     */
    private ReadMode readMode = ReadMode.SLAVE;

    /**
     * Set node type used for subscription operation.
     * <p>
     * Default is <code>MASTER</code>
     */
    private SubscriptionMode subscriptionMode = SubscriptionMode.MASTER;

    /**
     * Redis 'slave' node minimum idle subscription (pub/sub) connection amount for <b>each</b> slave node
     */
    private int subscriptionConnectionMinimumIdleSize = 1;

    /**
     * Redis 'slave' node maximum subscription (pub/sub) connection pool size for <b>each</b> slave node
     */
    private int subscriptionConnectionPoolSize = 50;

    /**
     * Interval in milliseconds to check the endpoint's DNS<p>
     * Applications must ensure the JVM DNS cache TTL is low enough to support this.<p>
     * Set <code>-1</code> to disable.
     * <p>
     * Default is <code>5000</code>.
     */
    private long dnsMonitoringInterval = 5000;
}

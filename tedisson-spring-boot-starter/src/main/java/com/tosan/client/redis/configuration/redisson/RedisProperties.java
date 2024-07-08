package com.tosan.client.redis.configuration.redisson;

import com.tosan.client.redis.configuration.stream.StreamProperties;
import com.tosan.client.redis.enumuration.RedisConnectionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.redisson.api.NameMapper;
import org.redisson.config.Config;
import org.redisson.config.SslProvider;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.net.URL;

/**
 * @author R.Mehri
 * @since 5/30/2023
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RedisProperties extends Config {

    /**
     * TedissonCacheManager implementation is specified from this config
     * true: redis
     * false: local
     */
    private boolean enabled = false;

    /**
     * Redis connection type
     * Each connection type have different configuration
     * Default is <code>SINGLE_NODE</code>
     */
    private RedisConnectionType connectionType = RedisConnectionType.SINGLE_NODE;

    /**
     * Username for Redis authentication. Should be null if not needed
     * <p>
     * Default is <code>null</code>
     * <p>
     * Requires Redis 6.0+
     */
    private String username;

    /**
     * Password for Redis authentication. Should be null if not needed
     */
    private String password;

    /**
     * If pooled connection not used for a <code>timeout</code> time
     * and current connections amount bigger than minimum idle connections pool size,
     * then it will closed and removed from pool.
     * Value in milliseconds.
     */
    private int idleConnectionTimeout = 10000;

    /**
     * Timeout during connecting to any Redis server.
     * Value in milliseconds.
     */
    private int connectTimeout = 10000;

    /**
     * Redis server response timeout. Starts to countdown when Redis command was succesfully sent.
     * Value in milliseconds.
     */
    private int timeout = 3000;

    /**
     * Error will be thrown if Redis command can't be sent to Redis server after <code>retryAttempts</code>.
     * But if it sent successfully then <code>timeout</code> will be started.
     * <p>
     * Default is <code>3</code> attempts
     */
    private int retryAttempts = 3;

    /**
     * Defines time interval for another one attempt send Redis command
     * if it hasn't been sent already.
     * Default is <code>1500</code> milliseconds
     * Time in milliseconds
     */
    private int retryInterval = 1500;

    /**
     * Subscriptions per Redis connection limit
     */
    private int subscriptionsPerConnection = 5;

    /**
     * Name of client connection
     */
    private String clientName;

    /**
     * Enables SSL.
     * <p>
     * Default is <code>false</code>
     */
    private boolean sslEnable = false;

    /**
     * Enables SSL endpoint identification.
     * <p>
     * Default is <code>true</code>
     */
    private boolean sslEnableEndpointIdentification = true;

    /**
     * Defines SSL provider used to handle SSL connections.
     * <p>
     * Default is <code>JDK</code>
     */
    private SslProvider sslProvider = SslProvider.JDK;

    /**
     * Defines path to SSL truststore
     * <p>
     * Default is <code>null</code>
     */
    private URL sslTruststore;

    /**
     * Defines password for SSL truststore.
     * SSL truststore is read on each new connection creation and can be dynamically reloaded.
     * <p>
     * Default is <code>null</code>
     */
    private String sslTruststorePassword;

    /**
     * Defines path to SSL keystore.
     * SSL keystore is read on each new connection creation and can be dynamically reloaded.
     * <p>
     * Default is <code>null</code>
     */
    private URL sslKeystore;

    /**
     * Defines password for SSL keystore
     * <p>
     * Default is <code>null</code>
     */
    private String sslKeystorePassword;

    /**
     * Defines SSL protocols.
     * Example values: TLSv1.3, TLSv1.2, TLSv1.1, TLSv1
     * <p>
     * Default is <code>null</code>
     */
    private String[] sslProtocols;

    /**
     * Defines PING command sending interval per connection to Redis.
     * <code>0</code> means disable.
     * <p>
     * Default is <code>30000</code>
     * Time in milliseconds
     */
    private int pingConnectionInterval = 10000;

    /**
     * Enables TCP keepAlive for connection
     * <p>
     * Default is <code>false</code>
     */
    private boolean keepAlive = false;

    /**
     * Enables TCP noDelay for connection
     * <p>
     * Default is <code>true</code>
     */
    private boolean tcpNoDelay = true;

    /**
     * Defines Name mapper which maps Redisson object name.
     * Applied to all Redisson objects.
     */
    private NameMapper nameMapper;

    /**
     * Defines SSL ciphers.
     * <p>
     * Default is <code>null</code>
     */
    private String[] sslCiphers = null;

    /**
     * Stream propertie
     */
    @NestedConfigurationProperty
    private StreamProperties stream;

    /**
     * Sentinel servers properties
     */
    @NestedConfigurationProperty
    private SentinelServersProperties sentinelServers;

    /**
     * Master slave servers properties
     */
    @NestedConfigurationProperty
    private MasterSlaveServersProperties masterSlaveServers;

    /**
     * Single server properties
     */
    @NestedConfigurationProperty
    private SingleServerProperties singleServer;

    /**
     * cluster servers properties
     */
    @NestedConfigurationProperty
    private ClusterServersProperties clusterServers;

    /**
     * Replicated servers properties
     */
    @NestedConfigurationProperty
    private ReplicatedServersProperties replicatedServers;
}

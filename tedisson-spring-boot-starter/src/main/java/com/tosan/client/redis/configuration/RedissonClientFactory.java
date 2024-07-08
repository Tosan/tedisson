package com.tosan.client.redis.configuration;

import com.tosan.client.redis.configuration.redisson.TedissonProperties;
import com.tosan.client.redis.exception.TedissonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author R.Mehri
 * @since 06/01/2021
 */
@Slf4j
public class RedissonClientFactory {
    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";
    private final TedissonProperties tedissonProperties;
    private RedissonClient redisClient;

    public RedissonClientFactory(TedissonProperties tedissonProperties) throws TedissonException {
        this.tedissonProperties = tedissonProperties;
        createInstance();
    }

    private void createInstance() throws TedissonException {
        Config config = null;
        if (tedissonProperties.getRedis().getConnectionType() == null) {
            config = getSingleNodeRedissonConfig();
        }
        switch (tedissonProperties.getRedis().getConnectionType()) {
            case SINGLE_NODE:
                config = getSingleNodeRedissonConfig();
                break;
            case CLUSTER:
                config = getClusterRedissonConfig();
                break;
            case SENTINEL:
                config = getSentinelRedissonConfig();
                break;
            case MASTER_SLAVE:
                config = getMasterSlaveRedissonConfig();
                break;
            case REPLICATED:
                config = getReplicatedRedissonConfig();
                break;
        }
        try {
            config.setThreads(tedissonProperties.getRedis().getThreads());
            config.setNettyThreads(tedissonProperties.getRedis().getNettyThreads());
            redisClient = Redisson.create(config);
        } catch (Exception e) {
            throw new TedissonException("Could not connect to redis server!");
        }
        log.info("Successfully connected to redis server!");
    }

    public RedissonClient getInstance() {
        return redisClient;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSSLConfig(BaseConfig baseConfig) {
        return (T) baseConfig.setSslEnableEndpointIdentification(tedissonProperties.getRedis().isSslEnableEndpointIdentification())
                .setSslKeystore(tedissonProperties.getRedis().getSslKeystore())
                .setSslKeystorePassword(tedissonProperties.getRedis().getSslKeystorePassword())
                .setSslTruststorePassword(tedissonProperties.getRedis().getSslTruststorePassword())
                .setSslTruststore(tedissonProperties.getRedis().getSslTruststore())
                .setSslProtocols(tedissonProperties.getRedis().getSslProtocols())
                .setSslProvider(tedissonProperties.getRedis().getSslProvider());
    }

    private Config getSingleNodeRedissonConfig() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        if (tedissonProperties.getRedis().isSslEnable()) {
            singleServerConfig = getSSLConfig(singleServerConfig);
        }
        singleServerConfig.setUsername(tedissonProperties.getRedis().getUsername())
                .setPassword(tedissonProperties.getRedis().getPassword())
                .setConnectTimeout(tedissonProperties.getRedis().getConnectTimeout())
                .setPingConnectionInterval(tedissonProperties.getRedis().getPingConnectionInterval())
                .setSubscriptionsPerConnection(tedissonProperties.getRedis().getSubscriptionsPerConnection())
                .setTimeout(tedissonProperties.getRedis().getTimeout())
                .setRetryAttempts(tedissonProperties.getRedis().getRetryAttempts())
                .setRetryInterval(tedissonProperties.getRedis().getRetryInterval())
                .setKeepAlive(tedissonProperties.getRedis().isKeepAlive())
                .setIdleConnectionTimeout(tedissonProperties.getRedis().getIdleConnectionTimeout())
                .setTcpNoDelay(tedissonProperties.getRedis().isTcpNoDelay())


                .setAddress(convertAddress(tedissonProperties.getRedis().getSingleServer().getAddress()))
                .setDatabase(tedissonProperties.getRedis().getSingleServer().getDatabase())
                .setConnectionMinimumIdleSize(tedissonProperties.getRedis().getSingleServer().getConnectionMinimumIdleSize())
                .setConnectionPoolSize(tedissonProperties.getRedis().getSingleServer().getConnectionPoolSize())
                .setSubscriptionConnectionMinimumIdleSize(tedissonProperties.getRedis().getSingleServer().getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(tedissonProperties.getRedis().getSingleServer().getSubscriptionConnectionPoolSize())
                .setDnsMonitoringInterval(tedissonProperties.getRedis().getSingleServer().getDnsMonitoringInterval());
        return config;
    }

    private Config getMasterSlaveRedissonConfig() {
        Config config = new Config();
        MasterSlaveServersConfig masterSlaveServersConfig = config.useMasterSlaveServers();
        if (tedissonProperties.getRedis().isSslEnable()) {
            masterSlaveServersConfig = getSSLConfig(masterSlaveServersConfig);
        }
        masterSlaveServersConfig.setUsername(tedissonProperties.getRedis().getUsername())
                .setPassword(tedissonProperties.getRedis().getPassword())
                .setConnectTimeout(tedissonProperties.getRedis().getConnectTimeout())
                .setPingConnectionInterval(tedissonProperties.getRedis().getPingConnectionInterval())
                .setSubscriptionsPerConnection(tedissonProperties.getRedis().getSubscriptionsPerConnection())
                .setTimeout(tedissonProperties.getRedis().getTimeout())
                .setRetryAttempts(tedissonProperties.getRedis().getRetryAttempts())
                .setRetryInterval(tedissonProperties.getRedis().getRetryInterval())
                .setKeepAlive(tedissonProperties.getRedis().isKeepAlive())
                .setIdleConnectionTimeout(tedissonProperties.getRedis().getIdleConnectionTimeout())
                .setTcpNoDelay(tedissonProperties.getRedis().isTcpNoDelay())
                .setSslEnableEndpointIdentification(tedissonProperties.getRedis().isSslEnableEndpointIdentification())
                .setSslKeystore(tedissonProperties.getRedis().getSslKeystore())
                .setSslKeystorePassword(tedissonProperties.getRedis().getSslKeystorePassword())
                .setSslTruststorePassword(tedissonProperties.getRedis().getSslTruststorePassword())
                .setSslTruststore(tedissonProperties.getRedis().getSslTruststore())
                .setSslProtocols(tedissonProperties.getRedis().getSslProtocols())
                .setSslProvider(tedissonProperties.getRedis().getSslProvider())

                .setMasterAddress(convertAddress(tedissonProperties.getRedis().getMasterSlaveServers().getMasterAddress()))
                .setDatabase(tedissonProperties.getRedis().getMasterSlaveServers().getDatabase())
                .setSubscriptionConnectionMinimumIdleSize(tedissonProperties.getRedis().getMasterSlaveServers().getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(tedissonProperties.getRedis().getMasterSlaveServers().getSubscriptionConnectionPoolSize())

                .setSlaveConnectionMinimumIdleSize(tedissonProperties.getRedis().getMasterSlaveServers().getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(tedissonProperties.getRedis().getMasterSlaveServers().getSlaveConnectionPoolSize())
                .setFailedSlaveReconnectionInterval(tedissonProperties.getRedis().getMasterSlaveServers().getFailedSlaveReconnectionInterval())
                .setFailedSlaveCheckInterval(tedissonProperties.getRedis().getMasterSlaveServers().getFailedSlaveCheckInterval())
                .setMasterConnectionMinimumIdleSize(tedissonProperties.getRedis().getMasterSlaveServers().getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(tedissonProperties.getRedis().getMasterSlaveServers().getMasterConnectionPoolSize())
                .setReadMode(tedissonProperties.getRedis().getMasterSlaveServers().getReadMode())
                .setSubscriptionMode(tedissonProperties.getRedis().getMasterSlaveServers().getSubscriptionMode())
                .setSubscriptionConnectionMinimumIdleSize(tedissonProperties.getRedis().getMasterSlaveServers().getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(tedissonProperties.getRedis().getMasterSlaveServers().getSubscriptionConnectionPoolSize())
                .setDnsMonitoringInterval(tedissonProperties.getRedis().getMasterSlaveServers().getDnsMonitoringInterval())

                .setSlaveAddresses(getConvertedAddresses(tedissonProperties.getRedis().getMasterSlaveServers().getSlaveAddresses()));
        return config;
    }

    private Config getSentinelRedissonConfig() {
        Config config = new Config();
        SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
        if (tedissonProperties.getRedis().isSslEnable()) {
            sentinelServersConfig = getSSLConfig(sentinelServersConfig);
        }
        sentinelServersConfig.setUsername(tedissonProperties.getRedis().getUsername())
                .setPassword(tedissonProperties.getRedis().getPassword())
                .setConnectTimeout(tedissonProperties.getRedis().getConnectTimeout())
                .setPingConnectionInterval(tedissonProperties.getRedis().getPingConnectionInterval())
                .setSubscriptionsPerConnection(tedissonProperties.getRedis().getSubscriptionsPerConnection())
                .setTimeout(tedissonProperties.getRedis().getTimeout())
                .setRetryAttempts(tedissonProperties.getRedis().getRetryAttempts())
                .setRetryInterval(tedissonProperties.getRedis().getRetryInterval())
                .setKeepAlive(tedissonProperties.getRedis().isKeepAlive())
                .setIdleConnectionTimeout(tedissonProperties.getRedis().getIdleConnectionTimeout())
                .setTcpNoDelay(tedissonProperties.getRedis().isTcpNoDelay())
                .setSslEnableEndpointIdentification(tedissonProperties.getRedis().isSslEnableEndpointIdentification())
                .setSslKeystore(tedissonProperties.getRedis().getSslKeystore())
                .setSslKeystorePassword(tedissonProperties.getRedis().getSslKeystorePassword())
                .setSslTruststorePassword(tedissonProperties.getRedis().getSslTruststorePassword())
                .setSslTruststore(tedissonProperties.getRedis().getSslTruststore())
                .setSslProtocols(tedissonProperties.getRedis().getSslProtocols())
                .setSslProvider(tedissonProperties.getRedis().getSslProvider())

                .setNatMapper(tedissonProperties.getRedis().getSentinelServers().getNatMapper())
                .setMasterName(tedissonProperties.getRedis().getSentinelServers().getMasterName())
                .setSentinelUsername(tedissonProperties.getRedis().getSentinelServers().getSentinelUsername())
                .setSentinelPassword(tedissonProperties.getRedis().getSentinelServers().getSentinelPassword())
                .setDatabase(tedissonProperties.getRedis().getSentinelServers().getDatabase())
                .setScanInterval(tedissonProperties.getRedis().getSentinelServers().getScanInterval())
                .setCheckSentinelsList(tedissonProperties.getRedis().getSentinelServers().isCheckSentinelsList())
                .setCheckSlaveStatusWithSyncing(tedissonProperties.getRedis().getSentinelServers().isCheckSlaveStatusWithSyncing())
                .setSentinelsDiscovery(tedissonProperties.getRedis().getSentinelServers().isSentinelsDiscovery())

                .setSlaveConnectionMinimumIdleSize(tedissonProperties.getRedis().getSentinelServers().getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(tedissonProperties.getRedis().getSentinelServers().getSlaveConnectionPoolSize())
                .setFailedSlaveReconnectionInterval(tedissonProperties.getRedis().getSentinelServers().getFailedSlaveReconnectionInterval())
                .setFailedSlaveCheckInterval(tedissonProperties.getRedis().getSentinelServers().getFailedSlaveCheckInterval())
                .setMasterConnectionMinimumIdleSize(tedissonProperties.getRedis().getSentinelServers().getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(tedissonProperties.getRedis().getSentinelServers().getMasterConnectionPoolSize())
                .setReadMode(tedissonProperties.getRedis().getSentinelServers().getReadMode())
                .setSubscriptionMode(tedissonProperties.getRedis().getSentinelServers().getSubscriptionMode())
                .setSubscriptionConnectionMinimumIdleSize(tedissonProperties.getRedis().getSentinelServers().getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(tedissonProperties.getRedis().getSentinelServers().getSubscriptionConnectionPoolSize())
                .setDnsMonitoringInterval(tedissonProperties.getRedis().getSentinelServers().getDnsMonitoringInterval())

                .setSentinelAddresses(getConvertedAddresses(tedissonProperties.getRedis().getSentinelServers().getSentinelAddresses()));
        return config;
    }

    private Config getClusterRedissonConfig() {
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        if (tedissonProperties.getRedis().isSslEnable()) {
            clusterServersConfig = getSSLConfig(clusterServersConfig);
        }
        clusterServersConfig.setUsername(tedissonProperties.getRedis().getUsername())
                .setPassword(tedissonProperties.getRedis().getPassword())
                .setConnectTimeout(tedissonProperties.getRedis().getConnectTimeout())
                .setPingConnectionInterval(tedissonProperties.getRedis().getPingConnectionInterval())
                .setSubscriptionsPerConnection(tedissonProperties.getRedis().getSubscriptionsPerConnection())
                .setTimeout(tedissonProperties.getRedis().getTimeout())
                .setRetryAttempts(tedissonProperties.getRedis().getRetryAttempts())
                .setRetryInterval(tedissonProperties.getRedis().getRetryInterval())
                .setKeepAlive(tedissonProperties.getRedis().isKeepAlive())
                .setIdleConnectionTimeout(tedissonProperties.getRedis().getIdleConnectionTimeout())
                .setTcpNoDelay(tedissonProperties.getRedis().isTcpNoDelay())
                .setSslEnableEndpointIdentification(tedissonProperties.getRedis().isSslEnableEndpointIdentification())
                .setSslKeystore(tedissonProperties.getRedis().getSslKeystore())
                .setSslKeystorePassword(tedissonProperties.getRedis().getSslKeystorePassword())
                .setSslTruststorePassword(tedissonProperties.getRedis().getSslTruststorePassword())
                .setSslTruststore(tedissonProperties.getRedis().getSslTruststore())
                .setSslProtocols(tedissonProperties.getRedis().getSslProtocols())
                .setSslProvider(tedissonProperties.getRedis().getSslProvider())

                .setNatMapper(tedissonProperties.getRedis().getClusterServers().getNatMapper())
                .setScanInterval(tedissonProperties.getRedis().getClusterServers().getScanInterval())
                .setCheckSlotsCoverage(tedissonProperties.getRedis().getClusterServers().isCheckSlotsCoverage())

                .setSlaveConnectionMinimumIdleSize(tedissonProperties.getRedis().getClusterServers().getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(tedissonProperties.getRedis().getClusterServers().getSlaveConnectionPoolSize())
                .setFailedSlaveReconnectionInterval(tedissonProperties.getRedis().getClusterServers().getFailedSlaveReconnectionInterval())
                .setFailedSlaveCheckInterval(tedissonProperties.getRedis().getClusterServers().getFailedSlaveCheckInterval())
                .setMasterConnectionMinimumIdleSize(tedissonProperties.getRedis().getClusterServers().getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(tedissonProperties.getRedis().getClusterServers().getMasterConnectionPoolSize())
                .setReadMode(tedissonProperties.getRedis().getClusterServers().getReadMode())
                .setSubscriptionMode(tedissonProperties.getRedis().getClusterServers().getSubscriptionMode())
                .setSubscriptionConnectionMinimumIdleSize(tedissonProperties.getRedis().getClusterServers().getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(tedissonProperties.getRedis().getClusterServers().getSubscriptionConnectionPoolSize())
                .setDnsMonitoringInterval(tedissonProperties.getRedis().getClusterServers().getDnsMonitoringInterval())

                .setNodeAddresses(getConvertedAddresses(tedissonProperties.getRedis().getClusterServers().getNodeAddresses()));

        return config;
    }

    private Config getReplicatedRedissonConfig() {
        Config config = new Config();
        ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers();
        if (tedissonProperties.getRedis().isSslEnable()) {
            replicatedServersConfig = getSSLConfig(replicatedServersConfig);
        }
        replicatedServersConfig.setUsername(tedissonProperties.getRedis().getUsername())
                .setPassword(tedissonProperties.getRedis().getPassword())
                .setConnectTimeout(tedissonProperties.getRedis().getConnectTimeout())
                .setPingConnectionInterval(tedissonProperties.getRedis().getPingConnectionInterval())
                .setSubscriptionsPerConnection(tedissonProperties.getRedis().getSubscriptionsPerConnection())
                .setTimeout(tedissonProperties.getRedis().getTimeout())
                .setRetryAttempts(tedissonProperties.getRedis().getRetryAttempts())
                .setRetryInterval(tedissonProperties.getRedis().getRetryInterval())
                .setKeepAlive(tedissonProperties.getRedis().isKeepAlive())
                .setIdleConnectionTimeout(tedissonProperties.getRedis().getIdleConnectionTimeout())
                .setTcpNoDelay(tedissonProperties.getRedis().isTcpNoDelay())
                .setSslEnableEndpointIdentification(tedissonProperties.getRedis().isSslEnableEndpointIdentification())
                .setSslKeystore(tedissonProperties.getRedis().getSslKeystore())
                .setSslKeystorePassword(tedissonProperties.getRedis().getSslKeystorePassword())
                .setSslTruststorePassword(tedissonProperties.getRedis().getSslTruststorePassword())
                .setSslTruststore(tedissonProperties.getRedis().getSslTruststore())
                .setSslProtocols(tedissonProperties.getRedis().getSslProtocols())
                .setSslProvider(tedissonProperties.getRedis().getSslProvider())

                .setScanInterval(tedissonProperties.getRedis().getClusterServers().getScanInterval())

                .setSlaveConnectionMinimumIdleSize(tedissonProperties.getRedis().getReplicatedServers().getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(tedissonProperties.getRedis().getReplicatedServers().getSlaveConnectionPoolSize())
                .setFailedSlaveReconnectionInterval(tedissonProperties.getRedis().getReplicatedServers().getFailedSlaveReconnectionInterval())
                .setFailedSlaveCheckInterval(tedissonProperties.getRedis().getReplicatedServers().getFailedSlaveCheckInterval())
                .setMasterConnectionMinimumIdleSize(tedissonProperties.getRedis().getReplicatedServers().getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(tedissonProperties.getRedis().getReplicatedServers().getMasterConnectionPoolSize())
                .setReadMode(tedissonProperties.getRedis().getReplicatedServers().getReadMode())
                .setSubscriptionMode(tedissonProperties.getRedis().getReplicatedServers().getSubscriptionMode())
                .setSubscriptionConnectionMinimumIdleSize(tedissonProperties.getRedis().getReplicatedServers().getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(tedissonProperties.getRedis().getReplicatedServers().getSubscriptionConnectionPoolSize())
                .setDnsMonitoringInterval(tedissonProperties.getRedis().getReplicatedServers().getDnsMonitoringInterval())

                .setNodeAddresses(getConvertedAddresses(tedissonProperties.getRedis().getReplicatedServers().getNodeAddresses()));

        return config;
    }

    private String convertAddress(String address) {
        if (!address.startsWith(REDIS_PROTOCOL_PREFIX) && !address.startsWith(REDISS_PROTOCOL_PREFIX)) {
            if (tedissonProperties.getRedis().isSslEnable()) {
                return REDISS_PROTOCOL_PREFIX + address;
            } else {
                return REDIS_PROTOCOL_PREFIX + address;
            }
        }
        return address;
    }

    private List<String> getConvertedAddresses(List<String> addresses) {
        List<String> convertedAddresses;
        if (CollectionUtils.isEmpty(addresses)) {
            return null;
        }
        convertedAddresses = new ArrayList<>();
        for (String address : addresses) {
            convertedAddresses.add(convertAddress(address));
        }
        return convertedAddresses;
    }

    private Set<String> getConvertedAddresses(Set<String> addresses) {
        Set<String> convertedAddresses;
        if (CollectionUtils.isEmpty(addresses)) {
            return null;
        }
        convertedAddresses = new HashSet<>();
        for (String address : addresses) {
            convertedAddresses.add(convertAddress(address));
        }
        return convertedAddresses;
    }
}

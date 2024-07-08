package com.tosan.client.redis;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.cacheconfig.*;
import com.tosan.client.redis.listener.*;
import com.tosan.client.redis.spring.Customer;
import com.tosan.client.redis.spring.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 1/1/2023
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.tosan.client.redis"})
public class TedissonApplication implements CommandLineRunner {

    @Autowired
    private TedissonCacheManager cacheManager;
    @Autowired
    private CustomerService customerService;

    @Autowired
    private LocalCacheManager localCacheManager;

    public static void main(String[] args) {
        new SpringApplicationBuilder(TedissonApplication.class)
                .web(WebApplicationType.NONE)
                .build()
                .run();
    }

    @Override
    public void run(String... args) throws InterruptedException {
        //sampleLocalCacheManager();
        //sampleCacheManagerWithLocalConfig();
        //sampleCacheManagerWithRedisConfig();
        //sampleCacheManagerWithRedisConfigAndCacheConfig();
        //sampleCacheManagerWithRedisConfigAndMessageQueue();
        //sampleLocalCacheManagerWithMessageQueueAndCacheClearing();
        //sampleUsingSpringCacheManager();
    }

    private void sampleLocalCacheManager() throws InterruptedException {
        LocalCacheConfig cacheConfig = new LocalCacheConfig();
        cacheConfig.setMaxSize(100);
        localCacheManager.createCache("cache", cacheConfig);
        LocalCacheConfig cacheConfig1 = new LocalCacheConfig();
        cacheConfig.setMaxSize(100);
        cacheConfig1.setCacheListener(new SampleCaffeineListener());
        localCacheManager.createCache("cache1", cacheConfig1);
        LocalCacheConfig cacheConfig2 = new LocalCacheConfig();
        cacheConfig.setMaxSize(100);
        cacheConfig2.setCacheListener(new SampleCaffeineListener());
        cacheConfig2.setExpiryPolicy(new CacheExpiryPolicy(10L, 7L));
        localCacheManager.createCache("cache2", cacheConfig2);
        localCacheManager.addItemToCache("cache", "key", "value");
        localCacheManager.addItemToCache("cache1", "key1", "value1");
        localCacheManager.replaceCacheItem("cache1", "key1", "newValue1");
        localCacheManager.removeItemFromCache("cache1", "key1");
        localCacheManager.addItemToCache("cache2", "key2", "value2");
        Thread.sleep(8000);
        //print null because item is not accessed for time greater than time to idle
        log.info((String) localCacheManager.getItemFromCache("cache2", "key2"));
        localCacheManager.addItemToCache("cache2", "key3", "value3");
        Thread.sleep(6000);
        //print value3
        log.info((String) localCacheManager.getItemFromCache("cache2", "key3"));
        Thread.sleep(5000);
        //print null because item expired(time to live)
        log.info((String) localCacheManager.getItemFromCache("cache2", "key3"));
    }

    //For running sample this config should be set
    //tedisson.redis.enabled=false
    private void sampleCacheManagerWithLocalConfig() throws InterruptedException {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(100);
        cacheManager.createCache("cache", cacheConfig);
        CacheConfig cacheConfig1 = new CacheConfig();
        cacheConfig.setListeners(Collections.singletonList(new SampleCaffeineListener()));
        cacheConfig1.setMaxSize(100);
        cacheManager.createCache("cache1", cacheConfig1);
        CacheConfig cacheConfig2 = new CacheConfig();
        cacheConfig2.setListeners(Collections.singletonList(new SampleCaffeineListener()));
        cacheConfig2.setExpiryPolicy(new CacheExpiryPolicy(10L,
                7L));
        cacheConfig2.setMaxSize(100);
        cacheManager.createCache("cache2", cacheConfig2);
        cacheManager.addItemToCache("cache", "key", "value");
        cacheManager.addItemToCache("cache1", "key1", "value1");
        cacheManager.replaceCacheItem("cache1", "key1", "newValue1");
        cacheManager.removeItemFromCache("cache1", "key1");
        cacheManager.addItemToCache("cache2", "key2", "value2");
        Thread.sleep(9000);
        //print null because item is not accessed for time greater than time to idle
        log.info((String) cacheManager.getItemFromCache("cache2", "key2"));
        cacheManager.addItemToCache("cache2", "key3", "value3");
        Thread.sleep(6000);
        //print value3
        log.info((String) cacheManager.getItemFromCache("cache2", "key3"));
        Thread.sleep(5000);
        //print null because item expired(time to live)
        log.info((String) cacheManager.getItemFromCache("cache2", "key3"));
    }

    //For running sample this config should be set
    //tedisson.redis.enabled=true
    private void sampleCacheManagerWithRedisConfig() throws InterruptedException {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(100);
        cacheManager.createCache("cache", cacheConfig);
        List<CacheListener> listeners = new ArrayList<>();
        listeners.add(new SampleRedisCreatedListener());
        listeners.add(new SampleRedisRemovedListener());
        listeners.add(new SampleRedisUpdatedListener());
        listeners.add(new SampleRedisExpiredListener());
        CacheConfig cacheConfig1 = new CacheConfig();
        cacheConfig.setListeners(listeners);
        cacheConfig1.setMaxSize(100);
        cacheManager.createCache("cache1", cacheConfig1);
        CacheConfig cacheConfig2 = new CacheConfig();
        cacheConfig2.setListeners(listeners);
        cacheConfig2.setExpiryPolicy(new CacheExpiryPolicy(10L,
                7L));
        cacheManager.createCache("cache2", cacheConfig2);
        cacheManager.addItemToCache("cache", "key", "value");
        cacheManager.addItemToCache("cache1", "key1", "value1");
        cacheManager.replaceCacheItem("cache1", "key1", "newValue1");
        cacheManager.removeItemFromCache("cache1", "key1");
        cacheManager.addItemToCache("cache2", "key2", "value2");
        Thread.sleep(8000);
        //print null because item is not accessed for time greater than time to idle
        log.info((String) cacheManager.getItemFromCache("cache2", "key2"));
        cacheManager.addItemToCache("cache2", "key3", "value3");
        Thread.sleep(6000);
        //print value3
        log.info((String) cacheManager.getItemFromCache("cache2", "key3"));
        Thread.sleep(5000);
        //print null because item expired(time to live)
        log.info((String) cacheManager.getItemFromCache("cache2", "key3"));
    }

    //For running sample this config should be set
    //tedisson.redis.enabled=true
    private void sampleCacheManagerWithRedisConfigAndCacheConfig() {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setCentralCacheType(new SharedCacheConfig());
        cacheConfig.setMaxSize(100);
        cacheManager.createCache("cache", cacheConfig);
        CacheConfig cacheConfig1 = new CacheConfig();
        cacheConfig1.setCentralCacheType(new ListenerSyncedLocalCacheConfig(false,
                true, true));
        cacheConfig1.setMaxSize(100);
        cacheManager.createCache("cache1", cacheConfig1);
        cacheManager.addItemToCache("cache1", "key1", "value1", 60L, 50L, TimeUnit.SECONDS);
        //print value1 because created listener is true and item added to local cache
        //also in a node that call addItemToCache, item add to local cache without listener
        log.info((String) localCacheManager.getItemFromCache("cache1", "key1"));
        //value of item with key 'key1' update in local caches
        cacheManager.replaceCacheItem("cache1", "key1", "value2");
    }

    //For running sample this config should be set
    //tedisson.redis.stream.enabled=true
    private void sampleCacheManagerWithRedisConfigAndMessageQueue() {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setCentralCacheType(new StreamSyncedLocalCacheConfig());
        cacheConfig.setMaxSize(100);
        cacheManager.createCache("cache", cacheConfig);
        cacheManager.addItemToCache("cache", "key", "value", 60L, 50L, TimeUnit.SECONDS);
        cacheManager.replaceCacheItem("cache", "key", "newValue");
    }

    //For running sample this config should be set
    //tedisson.redis.stream.enabled=true
    private void sampleLocalCacheManagerWithMessageQueueAndCacheClearing() {
        LocalCacheConfig cacheConfig = new LocalCacheConfig();
        cacheConfig.setNeedsClearCachePropagation(true);
        localCacheManager.createCache("test", cacheConfig);
        localCacheManager.addItemToCache("test", "key", "value");
        localCacheManager.clearCache("test");
    }

    private void sampleUsingSpringCacheManager() throws InterruptedException {
        Customer customer = new Customer();
        customer.setId("1");
        customer.setAddress("address1");
        Customer customer1 = new Customer();
        customer1.setId("2");
        customer1.setAddress("address2");
        log.info("address:{}", customerService.getAddress(customer));
        Thread.sleep(6000);
        log.info("address:{}", customerService.getAddress(customer));
    }
}

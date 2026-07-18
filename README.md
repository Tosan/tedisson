# tedisson-spring-boot-starter(using redisson)

This project provides a redisson Spring-Boot Starter that facilitate common requirements of a redis client using redisson and providing
an interface for using common cache methods such as add to cache, remove from cache, etc. This interface has two implementations: redis and local.
By setting redis enabled config to true redis implementation used. In addition to
using these implementations you can use local cache manager which always is created.


## ⭐ Usage
The tedisson-spring-boot-starter brings most of the required configuration with it,
therefore you only need to add it as a maven dependency and enable the desired functionality.

```
<dependency>
  <groupId>com.tosan.client.redis</groupId>
  <artifactId>tedisson-spring-boot-starter</artifactId>
  <version>${version}</version>
</dependency>
```

## ⚙️ Configuration Reference

The starter is configured via `tedisson.*` properties in your `application.yml` (or `application.properties`).

| Property | Type | Default | Description |
|---|---|---|---|
| `tedisson.redis.enabled` | `boolean` | `false` | Enable Redis-backed caching. When `false`, only local cache is used. |
| `tedisson.redis-client-type` | `REDISSON` / `LETTUCE` | `REDISSON` | Selects the Redis client implementation. |
| `tedisson.local.cache-provider` | `ehcache` / `caffeine` | `ehcache` | Local cache provider when a local tier is used. |
| `tedisson.redis.stream.enabled` | `boolean` | `false` | Enable Redis Streams for cross-node cache invalidation. Required for `StreamSyncedLocalCacheConfig`. |
| `tedisson.redis.stream.max-message-size` | `long` | `20` | Max stream messages retained before trimming. |
| `tedisson.redis.stream.trim-rate-second` | `int` | `3600` | Interval in seconds between stream trim operations. |
| `tedisson.redis.stream.thread-pool.core-pool-size` | `int` | — | Core pool size for stream consumer threads. |
| `tedisson.redis.stream.thread-pool.max-pool-size` | `int` | — | Max pool size for stream consumer threads. |
| `tedisson.redis.stream.thread-pool.queue-capacity` | `int` | — | Queue capacity for stream consumer thread pool. |

### Redis Client Type: Redisson vs Lettuce

The starter supports two Redis client implementations:

- **REDISSON** (default) — Uses the [Redisson](https://redisson.org) client. The starter creates and manages its own `RedissonClient` from `tedisson.redis.*` properties. Supports all Redis connection types: single node, cluster, sentinel, master/slave, and replicated.

- **LETTUCE** — Uses Spring Data Redis with the [Lettuce](https://lettuce.io) driver. The starter does **not** create a `RedisConnectionFactory` — your application must provide one via standard `spring.data.redis.*` properties (Spring Boot auto-configuration handles this). Use this when your project already has a `RedisConnectionFactory` bean.

To switch to Lettuce:
```yaml
tedisson:
  redis:
    enabled: true
  redis-client-type: LETTUCE
```

When using Lettuce, configure the connection as usual:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Redisson Connection Types

When using `redis-client-type: REDISSON`, the connection type is set via `tedisson.redis.connection-type` (`SINGLE_NODE`, `SENTINEL`, `CLUSTER`, `MASTER_SLAVE`, `REPLICATED`). Each type has its own nested properties under `tedisson.redis.*`:

```yaml
tedisson:
  redis:
    enabled: true
    connection-type: SINGLE_NODE   # default
    single-server:
      address: "redis://localhost:6379"
    # For cluster:
    # connection-type: CLUSTER
    # cluster-servers:
    #   node-addresses:
    #     - "redis://host1:6379"
    #     - "redis://host2:6379"
```

## 🚀 TedissonCacheManager Interface
This Interface provides common functionalities of a cache.

##### List of provided methods:
>- boolean isKeyInCache(String cacheName, String key)
>
>- void createCache(String cacheName, CacheConfig cacheConfig);
>
>- void createCache(String cacheName);
>
>- <T> T getItemFromCache(String cacheName, String key);
>
>- void addItemToCache(String cacheName, String key, Object value);
>
>- void addItemToCache(String cacheName, String key, Object value, Long timeToLive, TimeUnit timeUnit);
>
>- void addItemToCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);
>
>- void addAllToCache(String cacheName, Map<String, Object> items);
>
>- void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit);
>
>- void replaceCacheItem(String cacheName, String key, Object value);
>
>- void removeItemFromCache(String cacheName, String key);
>
>- void clearCache(String cacheName);
>
>- boolean isCacheEmpty(String cacheName);
>
>- long getCacheSize(String cacheName);
>
>- Set<String> getCacheKeySet(String cacheName);
>
>- CacheExpiryPolicy getCacheExpirationConfig(String cacheName);
>
>- void initializeAtomicLongCache(String cacheName, String key, CacheExpiryPolicy cacheExpiryPolicy);
>
>- long incrementAndGetAtomicItem(String cacheName, String key);
>
>- void resetAtomicItem(String cacheName, String key);
>
>- long getAtomicValue(String cacheName, String key);
>
>- void setAtomicItem(String cacheName, String key, long value);
>
>- void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit);
>
>- void addCacheExpirationConfig(String cacheName, CacheExpiryPolicy cacheExpiryPolicy);
>
>- void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy);
>
>- void updateItemExpiration(String cacheName, String key, Long timeToLive,
   TimeUnit timeUnit);
>
>- void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);
>
>- String getInstanceID();
>
>- Boolean isRedisEnabled();
>
>- LocalCacheProvider getLocalCacheProvider();
>
>- CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs);


By using this config one of implementation could be used.
> tedisson.redis.enabled= true | false

### Redis implementation
When `tedisson.redis.enabled` is `true`, the `TedissonCacheManager` bean communicates with a Redis server.
The client implementation is determined by `tedisson.redis-client-type` (`REDISSON` or `LETTUCE`).

There are 3 types of caches in this implementation:
- SharedCacheConfig (default)
- ListenerSyncedLocalCacheConfig
- StreamSyncedLocalCacheConfig

The cache type can be specified at the time of cache creation.
##### SharedCacheConfig
Default type is SharedCacheConfig. in this type there is no extra functionality on cache methods.</br>

Example:
```
CacheConfig cacheConfig = new CacheConfig();
cacheConfig.setCentralCacheType(new SharedCacheConfig());
cacheManager.createCache("cache", cacheConfig);
```
##### ListenerSyncedLocalCacheConfig
In ListenerSyncedLocalCacheConfig type data is stored on redis server and also local cache in order to achieve lower access time on read.
We suggest using this type for cache items which have big size with low change rate.</br>
When an item is fetched from the cache, it is first read from the local cache. If item does not exist in local cache item will be read from redis server
and later will be saved in local cache. local item expiration time set to remaining expiration time of redis server item.
This type has 3 listeners for creation, removal and update items.
These listeners could be activated by a flag.

Example:
```
CacheConfig cacheConfig = new CacheConfig();
cacheConfig1.setCentralCacheType(new ListenerSyncedLocalCacheConfig(false, true, true));
cacheManager.createCache("cache", cacheConfig);
```

- If needRemovedListener is true when an item remove from cache on redis server then item remove on local cahce.
- If needCreatedListener is true when an item insert into cache on redis server then item insert to local cahce.
- If needUpdatedListener is true when an item update in a cache on redis server then item update on local cahce.

##### StreamSyncedLocalCacheConfig
In this type when an item is created, updated or deleted in redis cache an event is sent to all local caches in every node and the local cache id cleared.
This feature uses Redis Streams, so `tedisson.redis.stream.enabled` must be `true`.

Example:
```
CacheConfig cacheConfig = new CacheConfig();
cacheConfig.setCentralCacheType(new StreamSyncedLocalCacheConfig());
cacheManager.createCache("cache", cacheConfig);
```
#### Create cache
There are two methods for creating a cache.
```
1- void createCache(String cacheName, CacheConfig cacheConfig);
2- void createCache(String cacheName);
```
Method 1 create cache with CacheConfig which contains cache configuration.</br>
Method 2 create cache with max size: Integer.MAX_VALUE

CacheConfig:
- private CacheExpiryPolicy expiryPolicy: specify time to live and time to idle for all items in cache.
- private List<CacheListener> listeners: cache items event listener(RedisCreatedListener, RedisExpiredListener, RedisRemovedListener and RedisUpdatedListener)
- private int maxSize: specify cache max size
- private CentralCacheTypeConfig centralCacheTypeConfig; specify redis cache type(SharedCacheConfig, ListenerSyncedLocalCacheConfig and StreamSyncedLocalCacheConfig)

Example:
```
@Autowired
private TedissonCacheManager cacheManager;

CacheConfig cacheConfig = new CacheConfig();
cacheConfig.setCentralCacheType(new SharedCacheConfig());
cacheConfig.setMaxSize(100);
cacheConfig.setExpiryPolicy(new CacheExpiryPolicy(100L, 50L));
cacheConfig.setListeners(Collections.singletonList((RedisCreatedListener) event -> >   {
   log.info("key created:" + event.getKey());
}));
cacheManager.createCache("cache", cacheConfig);
```
## 🚀 LocalCacheManager interface
This Interface provides common functionalities of a cache with two implementations:ehcache and caffeine.
The implementation can be selected using the following setting:
> tedisson.local.cache-provider=caffeine (default)
##### List of provided methods:
>- void createCache(String cacheName);
>
>- void createCache(String cacheName, LocalCacheConfig cacheConfig);
>
>- void clearCache(String cacheName);
>
>- void updateItemExpiration(String cacheName, String key, Long timeToLive, Long   timeToIdle, TimeUnit timeUnit);
>
>- removeCache(String cacheName);
>- boolean isKeyInCache(String cacheName, String key);
>
>- long incrementAndGetAtomicItem(String cacheName, String key);
>
>- void resetAtomicItem(String cacheName, String key);
>
>- long getAtomicValue(String cacheName, String key);
>
>- void setAtomicItem(String cacheName, String key, long value);
>
>- void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit);
>
>- Set<String> getCacheKeySet(String cacheName);
>
>- void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, TimeUnit timeUnit);
>
>- void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);
>
>- void addAllToCache(String cacheName, Map<String, Object> items);
>
>- <T> List<T> getAllFromCache(String cacheName);
>
>- void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit);
>
>- void addItemToCache(String cacheName, Object key, Object value);
>
>- boolean isCacheExist(String cacheName);
>
>- <T> T getItemFromCache(String cacheName, String key);
>
>- void removeItemFromCache(String cacheName, String key);
>
>- void replaceCacheItem(String cacheName, String key, Object value);
>
>- void evictExpiredCaches();
>
>- long getCacheSize(String cacheName);
>
>- CacheExpiryPolicy getCacheExpirationConfig(String cacheName);
>
>- void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy);
>
>- boolean isCacheEmpty(String cacheName);
>
>- LocalCacheProvider getCacheProvider();
>
>- CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs);

#### Create cache
There are two methods for creating a local cache.
```
1- void createCache(String cacheName, LocalCacheConfig cacheConfig);
2- void createCache(String cacheName);
```
Method 1 create cache with LocalCacheConfig which contains cache configuration.</br>
Method 2 create cache with max size: Integer.MAX_VALUE

LocalCacheConfig:
```
private CacheExpiryPolicy expiryPolicy: specify time to live and time to idle for all items in cache.
private CacheListener cacheListener: cache items event listener
private int maxSize = Integer.MAX_VALUE;
private boolean needsClearCachePropagation = false: if this flag is true, whenever the clearCache function is called, a message will be sent using the redis streams to clear all caches in other nodes with the same cache name. Note that for using this feature `tedisson.redis.stream.enabled` must be `true` and redis connection settings must be initialized.
```
Example:
```
@Autowired
private LocalCacheManager localCacheManager;

LocalCacheConfig cacheConfig = new LocalCacheConfig();
cacheConfig.setMaxSize(100);
cacheConfig.setCacheListener(new CaffeineCacheListener() {
   @Override
   public void onRemoval(@Nullable String key, @Nullable CaffeineElement value, @NonNull RemovalCause removalCause) {
      log.info("event: " + removalCause + " on key:" + key);
   }
});
cacheConfig.setExpiryPolicy(new CacheExpiryPolicy(10L, 7L));
cacheConfig.setNeedsClearCachePropagation(true);
localCacheManager.createCache("cache", cacheConfig)
```
In the case where the ehcache is used, the listener should be used as follows.
```
cacheConfig.setCacheListener(new EhCacheListener() {
   @Override
   public void onEvent(CacheEvent<? extends String, ? extends EhCacheElement> event) {
      log.info("event" + event.getType() + " on key:" + event.getKey());
   }
});
```
### Sample Project

You can find a sample project in tedisson-spring-boot-sample module

### Prerequisites
This Library requires java version 11 or above.

## Contributing
Any contribution is greatly appreciated.
If you have a suggestion that would make this project better, please fork the repo and create a pull request.
You can also simply open an issue with the tag "enhancement".

## License
The source files in this repository are available under the [Apache License Version 2.0](./LICENSE.txt).

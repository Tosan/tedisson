package com.tosan.client.redis.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author R.Mehri
 * @since 8/14/2023
 */
@Service
@Slf4j
public class CustomerService {

    @Cacheable(value = {"addresses"}, key = "#customer.id")
    //Or using specific cache manager
    //@Cacheable(value = {"addresses"}, key = "#customer.id", cacheManager = "tedissonSpringCacheManager")
    public String getAddress(Customer customer) {
        log.info("get from method");
        return customer.getAddress();
    }

}

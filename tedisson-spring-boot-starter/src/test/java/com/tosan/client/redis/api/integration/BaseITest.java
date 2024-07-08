package com.tosan.client.redis.api.integration;

import com.tosan.client.redis.configuration.TedissonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * @author R.Mehri
 * @since 6/3/2023
 */
@SpringBootTest()
@TestPropertySource(
        locations = "classpath:application.yaml")
@ContextConfiguration(classes = TedissonAutoConfiguration.class)
public class BaseITest {
}

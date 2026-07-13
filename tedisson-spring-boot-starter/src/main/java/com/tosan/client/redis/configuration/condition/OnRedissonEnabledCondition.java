package com.tosan.client.redis.configuration.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnRedissonEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String redisEnabled =
                context.getEnvironment().getProperty("tedisson.redis.enabled");
        if (!"true".equalsIgnoreCase(redisEnabled)) {
            return false;
        }
        String clientType =
                context.getEnvironment().getProperty("tedisson.redis-client-type");
        // matchIfMissing = true
        return clientType == null || "REDISSON".equalsIgnoreCase(clientType);
    }
}
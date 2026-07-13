package com.tosan.client.redis.configuration.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnLettuceEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment()
                .getProperty("tedisson.redis.enabled");
        String clientType = context.getEnvironment()
                .getProperty("tedisson.redis-client-type");
        return "true".equalsIgnoreCase(enabled)
                && "LETTUCE".equalsIgnoreCase(clientType);
    }
}
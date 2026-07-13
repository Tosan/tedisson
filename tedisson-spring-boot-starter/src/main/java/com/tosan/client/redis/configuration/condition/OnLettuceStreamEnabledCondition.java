package com.tosan.client.redis.configuration.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnLettuceStreamEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        boolean streamEnabled = env.getProperty(
                "tedisson.redis.stream.enabled",
                Boolean.class,
                false);
        if (!streamEnabled) {
            return false;
        }
        String clientType = env.getProperty("tedisson.redis-client-type");
        return "LETTUCE".equalsIgnoreCase(clientType);
    }
}

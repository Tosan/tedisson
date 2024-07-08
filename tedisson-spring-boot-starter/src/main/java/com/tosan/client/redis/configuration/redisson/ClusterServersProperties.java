package com.tosan.client.redis.configuration.redisson;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.redisson.api.NatMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author R.Mehri
 * @since 1/7/2023
 */
//org.redisson.config.ClusterServersConfig
@Data
@EqualsAndHashCode(callSuper = true)
public class ClusterServersProperties extends BaseMasterSlaveServersProperties {
    private NatMapper natMapper = NatMapper.direct();

    /**
     * Redis cluster node urls list
     */
    private List<String> nodeAddresses = new ArrayList<>();

    /**
     * Redis cluster scan interval in milliseconds
     */
    private int scanInterval = 5000;

    /**
     * Enables cluster slots check during Redisson startup.
     * <p>
     * Default is <code>true</code>
     */
    private boolean checkSlotsCoverage = true;
}

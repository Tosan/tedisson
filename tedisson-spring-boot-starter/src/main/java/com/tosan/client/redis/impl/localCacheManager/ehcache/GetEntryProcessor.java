package com.tosan.client.redis.impl.localCacheManager.ehcache;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.util.Date;

/**
 * @author R.Mehri
 * @since 6/3/2023
 */
public class GetEntryProcessor implements EntryProcessor<String, EhCacheElement, EhCacheElement> {

    @Override
    public EhCacheElement process(MutableEntry<String, EhCacheElement> entry, Object... objects) throws EntryProcessorException {
        if (!entry.exists()) {
            entry.setValue(new EhCacheElement(0L));
        }
        Date now = new Date();
        if (entry.exists() && entry.getValue() != null && entry.getValue().getExpirationTime() != null &&
                entry.getValue().getExpirationTime().after(now)) {
            EhCacheElement element = entry.getValue();
            element.setValue(0L);
            entry.setValue(element);
        }
        return entry.getValue();
    }
}

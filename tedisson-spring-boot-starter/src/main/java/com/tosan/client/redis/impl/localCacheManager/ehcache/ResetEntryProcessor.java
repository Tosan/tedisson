package com.tosan.client.redis.impl.localCacheManager.ehcache;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

/**
 * @author R.Mehri
 * @since 6/7/2023
 */
public class ResetEntryProcessor implements EntryProcessor<String, EhCacheElement, EhCacheElement> {
    @Override
    public EhCacheElement process(MutableEntry<String, EhCacheElement> entry, Object... arguments) throws EntryProcessorException {
        if (!entry.exists()) {
            entry.setValue(new EhCacheElement(0L));
            return entry.getValue();
        }
        EhCacheElement current = entry.getValue();
        current.setValue(0L);
        return current;
    }
}

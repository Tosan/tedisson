package com.tosan.client.redis.impl.localCacheManager.ehcache;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.util.Date;

/**
 * @author R.Mehri
 * @since 12/6/2022
 */
public class AddEntryProcessor implements EntryProcessor<String, EhCacheElement, EhCacheElement> {

    @Override
    public EhCacheElement process(MutableEntry<String, EhCacheElement> entry, Object... objects) throws EntryProcessorException {
        if (entry.exists()) {
            EhCacheElement current = entry.getValue();
            Date now = new Date();
            if (entry.getValue() != null && entry.getValue().getExpirationTime() != null && entry.getValue().getExpirationTime().before(now)) {
                EhCacheElement element = entry.getValue();
                element.setValue(1L);
                element.setExpirationTime(null);
                entry.setValue(element);
            } else {
                current.setValue((long) current.getValue() + 1);
            }
            return current;
        } else {
            entry.setValue(new EhCacheElement(1L));
            return entry.getValue();
        }
    }
}
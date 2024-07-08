package com.tosan.client.redis.impl.localCacheManager.ehcache;

import com.tosan.client.redis.api.CacheExpiryPolicy;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.util.Calendar;
import java.util.Date;

/**
 * @author R.Mehri
 * @since 6/21/2023
 */
public class UpdateEntryExpirationProcessor implements EntryProcessor<String, EhCacheElement, EhCacheElement> {

    private final CacheExpiryPolicy cacheExpiryPolicy;

    public UpdateEntryExpirationProcessor(CacheExpiryPolicy cacheExpiryPolicy) {
        this.cacheExpiryPolicy = cacheExpiryPolicy;
    }

    @Override
    public EhCacheElement process(MutableEntry<String, EhCacheElement> entry, Object... arguments) throws EntryProcessorException {
        if (entry.exists()) {
            EhCacheElement element = entry.getValue();
            if (cacheExpiryPolicy != null) {
                Date now = new Date();
                if (cacheExpiryPolicy.getTimeToLiveSecond() != null) {
                    Date expirationTime = getItemExpirationTime(now, cacheExpiryPolicy.getTimeToLiveSecond());
                    element.setExpirationTime(expirationTime);
                }
                element.setTimeToLiveSecond(cacheExpiryPolicy.getTimeToLiveSecond());
            }
            return element;
        }
        return null;
    }

    private Date getItemExpirationTime(Date now, Long duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, Math.toIntExact(duration));
        return calendar.getTime();
    }
}


package com.underdog.jersey.cache.example.dry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * An HTTP cache that behaves somewhat the same way as a proxy (like Squid)
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SimpleServerCache implements ServerCache {

    public static class CacheEntry implements Entry {

        private final byte[] cached;
        private final int expires;
        private final long timestamp = System.currentTimeMillis();
        private final MultivaluedMap<String, Object> headers;
        private String etag;

        public CacheEntry(MultivaluedMap<String, Object> headers, byte[] cached, int expires, String etag) {
            this.headers = headers;
            this.cached = cached;
            this.expires = expires;
            this.etag = etag;
        }

        @Override
        public int getExpirationInSeconds() {
            int expirationInSeconds = expires - (int) ((System.currentTimeMillis() - timestamp) / 1000);
            return expirationInSeconds;
        }

        @Override
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp >= expires * 1000;
        }

        @Override
        public String getEtag() {
            return etag;
        }

        @Override
        public byte[] getCached() {
            return cached;
        }

        @Override
        public MultivaluedMap<String, Object> getHeaders() {
            return headers;
        }
    }

    private final Map<String, Map<MediaType, CacheEntry>> cache = new ConcurrentHashMap<>();

    @Override
    public Entry add(String uri, MediaType mediaType, CacheControl cc,
            MultivaluedMap<String, Object> headers, byte[] entity, String etag) {
        CacheEntry cacheEntry = new CacheEntry(headers, entity, cc.getMaxAge(), etag);
        Map<MediaType, CacheEntry> entry = cache.get(uri);
        if (entry == null) {
            entry = new ConcurrentHashMap<MediaType, CacheEntry>();
            cache.put(uri, entry);
        }
        entry.put(mediaType, cacheEntry);
        return cacheEntry;
    }

    @Override
    public Entry get(String uri, MediaType accept) {
        Map<MediaType, CacheEntry> entry = cache.get(uri);
        if (entry == null || entry.isEmpty()) {
            return null;
        }

        for (Map.Entry<MediaType, CacheEntry> produce : entry.entrySet()) {
            if (accept.isCompatible(produce.getKey())) {
                return produce.getValue();
            }
        }
        return null;
    }

    @Override
    public void remove(String uri) {
        cache.remove(uri);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}

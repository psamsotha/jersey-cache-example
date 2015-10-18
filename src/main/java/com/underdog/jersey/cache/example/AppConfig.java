
package com.underdog.jersey.cache.example;

import com.underdog.jersey.cache.example.cachecontrol.CacheControlResource;
import com.underdog.jersey.cache.example.dry.DryResource;
import com.underdog.jersey.cache.example.dry.ServerCacheFeature;
import com.underdog.jersey.cache.example.dry.SimpleServerCache;
import com.underdog.jersey.cache.example.expires.ExpiresResource;
import com.underdog.jersey.cache.example.lastmodified.LastModifiedResource;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Paul Samsotha
 */
@ApplicationPath("/api")
public class AppConfig extends ResourceConfig {
    
    public AppConfig() {
        register(ExpiresResource.class);
        register(LastModifiedResource.class);
        register(CacheControlResource.class);
        
        register(DryResource.class);
        register(new ServerCacheFeature(new SimpleServerCache()));
    }
}

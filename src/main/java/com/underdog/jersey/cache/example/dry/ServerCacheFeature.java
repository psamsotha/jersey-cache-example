/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.dry;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 *
 * @author PaulSamsotha
 */
public class ServerCacheFeature implements Feature {
    
    protected ServerCache cache;
    
    //public ServerCacheFeature(){}
    
    public ServerCacheFeature(ServerCache cache) {
        this.cache = cache;
    }

    @Override
    public boolean configure(FeatureContext configurable) {
        configurable.register(new ServerCacheHitFilter(cache));
        configurable.register(new ServerCacheInterceptor(cache));
        configurable.register(CacheControlFeature.class);
        return true;
    }
}

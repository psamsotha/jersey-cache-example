/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.dry;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author PaulSamsotha
 */
public interface ServerCache {
    
    public static interface Entry {
        int getExpirationInSeconds();
        boolean isExpired();
        String getEtag();
        byte[] getCached();
        MultivaluedMap<String, Object> getHeaders();
    }
    
    Entry add(String uri, MediaType mediaType, CacheControl cc, 
              MultivaluedMap<String, Object> headers, byte[] entity, String etag);
    
    Entry get(String uri, MediaType accept);
    
    void remove(String uri);
    
    void clear();
}

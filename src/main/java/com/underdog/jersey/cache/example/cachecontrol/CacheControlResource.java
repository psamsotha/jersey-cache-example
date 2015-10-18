/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.cachecontrol;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

/**
 *
 * @author PaulSamsotha
 */
@Path("cc")
public class CacheControlResource {
    
    @GET
    @Produces("text/plain")
    public Response get() {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(10);
        return Response.ok("Some Data").cacheControl(cc).build();
    }
}

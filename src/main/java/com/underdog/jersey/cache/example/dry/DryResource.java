/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.dry;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul Samsotha
 */
@Singleton
@Path("dry")
public class DryResource {
    
    private String data = "Some Data";
    
    @GET
    @Cache(maxAge=10)
    @Produces("text/plain")
    public Response get() {
        return Response.ok(data).build();
    }
    
    @POST
    @Consumes("text/plain")
    public Response post(String data) {
        this.data = data;
        return Response.noContent().build();
    }
}

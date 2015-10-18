/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.lastmodified;

import java.util.Date;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author Paul Samsotha
 */
@Singleton
@Path("last-modified")
public class LastModifiedResource {
    
    private String data = "Some Data";
    private Date lastModified = new Date();
    
    @GET
    @Produces("text/plain")
    public Response get(@Context Request request) {
        ResponseBuilder builder = request.evaluatePreconditions(lastModified);
        if (builder != null) {
            return builder.build();
        }
        return Response.ok(data).lastModified(lastModified).build();
    }
    
    @POST
    @Consumes("text/plain")
    public Response post(String data) {
        this.data = data;
        lastModified = new Date();
        return Response.noContent().build();
    }
}

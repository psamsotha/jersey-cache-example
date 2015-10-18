
package com.underdog.jersey.cache.example.etag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Paul Samsotha
 */
@Singleton
@Path("etag")
public class EtagResource {
    
    private String data = "Some Data";
    
    @GET
    @Produces("text/plain")
    public Response get(@Context Request request) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        String hex = DatatypeConverter.printHexBinary(hash);
        EntityTag etag = new EntityTag(hex);
        
        ResponseBuilder builder = request.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.build();
        }
        
        return Response.ok(data).tag(etag).build();
    }
    
    @POST
    @Consumes("text/plain")
    public Response post(String data) {
        this.data = data;
        return Response.noContent().build();
    }
}

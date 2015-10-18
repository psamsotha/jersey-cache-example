
package com.underdog.jersey.cache.example.expires;

import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul Samsotha
 */
@Path("expires")
public class ExpiresResource {
    
    @GET
    @Produces("text/plain")
    public Response get() {
        long current = new Date().getTime();
        Date expires = new Date(current + (10 * 1_000));
        return Response.ok("Some Data").expires(expires).build();
    }
}

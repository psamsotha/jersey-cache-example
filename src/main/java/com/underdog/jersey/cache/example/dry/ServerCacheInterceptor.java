/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.dry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 *
 * @author Paul Samsotha
 */
@ConstrainedTo(RuntimeType.SERVER)
public class ServerCacheInterceptor implements WriterInterceptor {

    protected ServerCache cache;

    public ServerCacheInterceptor(ServerCache cache) {
        this.cache = cache;
    }

    @Context
    protected Request validation;

    @Context
    protected javax.inject.Provider<ContainerRequestContext> requestProvider;

    private static final String pseudo[] = {"0", "1", "2",
        "3", "4", "5", "6", "7", "8",
        "9", "A", "B", "C", "D", "E",
        "F"};

    public static String byteArrayToHexString(byte[] bytes) {

        byte ch = 0x00;
        StringBuffer out = new StringBuffer(bytes.length * 2);
        out.append('"');

        for (byte b : bytes) {

            ch = (byte) (b & 0xF0);
            ch = (byte) (ch >>> 4);
            ch = (byte) (ch & 0x0F);
            out.append(pseudo[(int) ch]);
            ch = (byte) (b & 0x0F);
            out.append(pseudo[(int) ch]);

        }

        out.append('"');
        String rslt = new String(out);
        return rslt;
    }

    protected String createHash(byte[] entity) {
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            byte abyte0[] = messagedigest.digest(entity);
            return byteArrayToHexString(abyte0);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        ContainerRequestContext request = requestProvider.get();
        if (!request.getMethod().equalsIgnoreCase("GET")
                || request.getProperty(ServerCacheHitFilter.DO_NOT_CACHE_RESPONSE) != null) {
            context.proceed();
            return;
        }

        Object occ = context.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL);
        if (occ == null) {
            context.proceed();
            return;
        }

        CacheControl cc = null;
        if (occ instanceof CacheControl) {
            cc = (CacheControl) occ;
        } else {
            cc = CacheControl.valueOf(occ.toString());
        }

        if (cc.isNoCache()) {
            context.proceed();
            return;
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream old = context.getOutputStream();
        try {
            context.setOutputStream(buffer);
            context.proceed();
            byte[] entity = buffer.toByteArray();
            Object etagObject = context.getHeaders().getFirst(HttpHeaders.ETAG);
            String etag = null;
            if (etagObject == null) {
                etag = createHash(entity);
                context.getHeaders().putSingle(HttpHeaders.ETAG, etag);
            } else {
                etag = etagObject.toString();
            }

            cache.add(request.getUriInfo().getRequestUri().toString(),
                    context.getMediaType(), cc, context.getHeaders(), entity, etag);
            
            Response.ResponseBuilder validatedResponse = validation.evaluatePreconditions(new EntityTag(etag));
            if (validatedResponse != null) {
                throw new WebApplicationException(validatedResponse.status(Response.Status.NOT_MODIFIED)
                        .cacheControl(cc).header(HttpHeaders.ETAG, etag).build());
            }
            
            old.write(entity);
            
        } finally {
            context.setOutputStream(old);
        }
    }
}

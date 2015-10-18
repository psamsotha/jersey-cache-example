/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.underdog.jersey.cache.example.dry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set response Cache-Control header automatically.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache
{
   int maxAge() default -1;

   int sMaxAge() default -1;

   boolean noStore() default false;

   boolean noTransform() default false;

   boolean mustRevalidate() default false;

   boolean proxyRevalidate() default false;

   boolean isPrivate() default false;

}

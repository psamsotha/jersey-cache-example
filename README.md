Jerey HTTP Cache Example
==============

###Blog example from [HTTP Caching With Jersey and JAX-RS ][1]


[1]: http://paulsamsotha.blogspot.com/2015/10/http-caching-with-jersey-and-jax-rs.html

## UPDATE

If you've read the blog, then you will have notice that the implementation in this example is
taken from the RESTEasy Cache module. 

What I have actually been doing is just using the module. It takes a little bit of tweeking though.
We need to add the RESTEasy cache dependency, but also exclude a bunch of stuff

```xml
<!-- RESTEasy dependencies used only for its cache feature -->
<dependency>
	<groupId>org.jboss.resteasy</groupId>
	<artifactId>resteasy-jaxrs</artifactId>
	<version>${resteasy.version}</version>
	<exclusions>
		<exclusion>
			<groupId>org.jboss.resteasy</groupId>
			<artifactId>jaxrs-api</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.jboss.spec.javax.servlet</groupId>
			<artifactId>jboss-servlet-api_3.1_spec</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.jboss.spec.javax.annotation</groupId>
			<artifactId>jboss-annotations-api_1.1_spec</artifactId>
		</exclusion>
		<exclusion>
			<groupId>javax.activation</groupId>
			<artifactId>activation</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpclient</artifactId>
		</exclusion>
		<exclusion>
			<groupId>commons-io</groupId>
			<artifactId>commons-io</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>org.jboss.resteasy</groupId>
	<artifactId>resteasy-cache-core</artifactId>
	<version>${resteasy.version}</version>
	<exclusions>
		<exclusion>
			<groupId>org.jboss.resteasy</groupId>
			<artifactId>jaxrs-api</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.jboss.resteasy</groupId>
			<artifactId>resteasy-jaxrs</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.jboss.spec.javax.servlet</groupId>
			<artifactId>jboss-servlet-api_3.1_spec</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<!-- End RESTEasy -->
```

Then I created a Jersey (JAX-RS) `Feature` which includes creting a half implementation of a 
RESTEasy specfic `HttpRequest` class. This is innjected into one of the `RESTEasy` components.
So we make our own implementation an use Jersey's DI system to inject it.

```java
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.jboss.resteasy.plugins.cache.server.ServerCacheFeature;
import org.jboss.resteasy.plugins.cache.server.SimpleServerCache;
import org.jboss.resteasy.plugins.interceptors.CacheControlFeature;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyUriInfo;

/**
 * Feature that uses RESTEasy's caching. Though the components were meant to used with RESTEasy, we
 * are are creating our own implementation of RESTEasy's {@code HttpRequest}, which is specific to
 * RESTEasy. It is injected into one of the JAX-RS interceptors, so we need to implement this, since
 * it is only available in a RESTEasy application.
 * 
 * RESTEasy's caching mechanism sets the Etag for us, and also handles conditional GET requests.
 * The RESTEasy {@code ServerCacheFeature} allows us to configure a {@code ServerCache}
 * implementation. The default implementation is the {@code InfinispanServerCache}. Since
 * we don't care too much to set up Inifinispan for this example, we will just be using the
 * <em>deprecated</em> {@code SimpleServerCache}, which uses an in-memory cache.
 *
 * @author Paul Samsotha
 */
public class ResteasyCacheFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {

        final ServerCacheFeature cacheFeature = new ServerCacheFeature(new SimpleServerCache());
        if (cacheFeature.configure(context)) {
            context.register(CacheControlFeature.class);
            context.register(new Binder());
            return true;
        }
        return false;
    }

    private static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bindFactory(ResteasyHttpRequestFactory.class)
                    .to(HttpRequest.class)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .in(RequestScoped.class);
        }
    }

    /**
     * Factory class that create's a RESTEasy {@code HttpRequest}. The only methods implemented are
     * those that are used in RESTEasy's cache implementation. Most of the method implementations
     * will simply delegate to respective methods in Jersey's {@code ContainerRequest}.
     */
    static class ResteasyHttpRequestFactory
            extends AbstractContainerRequestValueFactory<HttpRequest> {

        @Override
        public HttpRequest provide() {
            return new ConvertedHttpRequest(getContainerRequest());
        }

        static class ConvertedHttpRequest implements HttpRequest {

            private final ContainerRequest containerRequest;
            private final HttpHeaders httpHeaders;

            ConvertedHttpRequest(ContainerRequest containerRequest) {
                this.containerRequest = containerRequest;

                this.httpHeaders = new ResteasyHttpHeaders(containerRequest.getHeaders(),
                        containerRequest.getCookies());
            }

            @Override
            public HttpHeaders getHttpHeaders() {
                return this.httpHeaders;
            }

            @Override
            public MultivaluedMap<String, String> getMutableHeaders() {
                return this.containerRequest.getHeaders();
            }

            @Override
            public InputStream getInputStream() {
                return containerRequest.getEntityStream();
            }

            @Override
            public void setInputStream(InputStream in) {
                containerRequest.setEntityStream(in);
            }

            @Override
            public ResteasyUriInfo getUri() {
                return new ResteasyUriInfo(this.containerRequest.getBaseUri(),
                        URI.create(this.containerRequest.getPath(false)));
            }

            @Override
            public String getHttpMethod() {
                return this.containerRequest.getMethod();
            }

            @Override
            public void setHttpMethod(String string) {
                this.containerRequest.setMethod(string);
            }

            @Override
            public void setRequestUri(URI uri) throws IllegalStateException {
                this.containerRequest.setRequestUri(uri);
            }

            @Override
            public void setRequestUri(URI uri, URI uri1) throws IllegalStateException {
                this.containerRequest.setRequestUri(uri, uri);
            }

            @Override
            public MultivaluedMap<String, String> getFormParameters() {
                this.containerRequest.bufferEntity();
                return this.containerRequest.readEntity(Form.class).asMap();
            }

            @Override
            public MultivaluedMap<String, String> getDecodedFormParameters() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(String string) {
                return this.containerRequest.getProperty(string);
            }

            @Override
            public void setAttribute(String string, Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void removeAttribute(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ResteasyAsynchronousContext getAsyncContext() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isInitial() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void forward(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean wasForwarded() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }
}
```

Now you can juse register the feature with Jersey

```java
resourceConfig.resgister(new ResteasyCacheFilter());
```

The rest of it will work as expected, just like in the example from this project. You will probably want to
modify the feature a bit so that you can use a different server cache, instead of the deprecated `SimpleServerCache`.

package org.objectlabs.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;


public enum HttpMethod {

    GET(HttpGet.class),
    POST(HttpPost.class),
    PUT(HttpPut.class),
    DELETE(HttpDelete.class),
    CREATE(null), // TODO: Create our own implementation
    PATCH(HttpPatch.class),
    HEAD(HttpHead.class),
    OPTIONS(HttpOptions.class);

    HttpMethod(Class<? extends HttpUriRequest> requestClass ) {
        setRequestClass(requestClass);
    }

    private Class<? extends HttpUriRequest> mRequestClass;
    public Class<? extends HttpUriRequest> getRequestClass ( )
    { return mRequestClass; }
    public void setRequestClass ( Class<? extends HttpUriRequest> requestClass )
    { mRequestClass = requestClass; }

    public HttpUriRequest newRequest ( String uri ) {
        Class<? extends HttpUriRequest> clazz = getRequestClass();
        if ( clazz == null ) {
            throw new UnsupportedOperationException("No request class associated with HTTP method " + toString());
        }
        try {
            Constructor<? extends HttpUriRequest> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(uri);
        } catch ( NoSuchMethodException e ) {
            throw new IllegalStateException("Could not find String constructor for class " + clazz.getName(), e);
        } catch ( InstantiationException e ) {
            throw new IllegalStateException(e);
        } catch ( IllegalAccessException e ) {
            throw new IllegalStateException(e);
        } catch ( InvocationTargetException e ) {
            throw new RuntimeException(e);
        }
    }

}

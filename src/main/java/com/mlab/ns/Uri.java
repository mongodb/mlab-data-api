package com.mlab.ns;

import java.net.URI;
import java.net.URISyntaxException;

public class Uri {

    public Uri() {
        super();
    }

    public Uri(URI uri) {
        this(uri.getScheme(),
             uri.getAuthority(),
             uri.getSchemeSpecificPart(),
             uri.getPath(),
             uri.getQuery());
    }

    public Uri(String uri) {
        try {
            URI u = new URI(uri);
            configure(u.getScheme(),
                      u.getAuthority(),
                      u.getSchemeSpecificPart(),
                      u.getPath(),
                      u.getQuery());
        } catch (Exception e) {
            throw(new IllegalArgumentException(e));
        }
    }

    public Uri(String scheme, String path) {
        this(scheme, null, path);
    }

    public Uri(String scheme, String authority, String path) {
        this(scheme, authority, null, path, null);
    }

    public Uri(String scheme,
               String authority,
               String ssp,
               String path,
               String query)
    {
        configure(scheme, authority, ssp, path, query);
    }

    private void configure(String scheme,
                           String authority,
                           String ssp,
                           String path,
                           String query)
    {
        if (path == null) {
            path = ssp;
        }
        configure(scheme,
                  (authority == null) ? null : new Authority(authority),
                  new Path(path),
                  query);
    }

    public Uri(String scheme, Authority authority, Path path, String query) {
        configure(scheme, authority, path, query);
    }

    private void configure(String scheme,
                           Authority authority,
                           Path path,
                           String query)
    {
        setScheme(scheme);
        setAuthority(authority);
        setPath(path);
        setQuery(query);
    }

    private String scheme;

    public String getScheme() {
        return(scheme);
    }

    public void setScheme(String value) {
        scheme = value;
    }

    private Authority authority;

    public Authority getAuthority() {
        return(authority);
    }

    public void setAuthority(Authority value) {
        authority = value;
    }

    private Path path;

    public Path getPath() {
        if (path == null) path = new Path();
        return(path);
    }

    public void setPath(Path value) {
        path = value;
    }

    private String query;

    public String getQuery() {
        return(query);
    }

    public void setQuery(String value) {
        query = value;
    }

    public String getName() {
        Path path = getPath();
        if (path.isEmpty()) {
            return(null);
        }
        return(path.getHead());
    }

    public String getAbsoluteName() {
        StringBuffer buff = new StringBuffer();

        if (getScheme() != null) buff.append(getScheme()).append(":");
        if (getAuthority() != null) buff.append(getAuthority());
        buff.append(getPath());
        if (getQuery() != null) buff.append("?").append(getQuery());

        return(buff.toString());
    }

    public boolean isAbsolute() {
        return(getPath().isAbsolute());
    }

    public String getHead() {
        return(getPath().getHead());
    }

    public Uri getTail() {
        Uri result = clone();
        result.setPath(getPath().getTail());
        return(result);
    }

    public Uri getParent() {
        Uri result = clone();
        result.setPath(getPath().getParent());
        return(result);
    }

    public Uri resolve(String name) {
        throw(new IllegalArgumentException("not implemented"));
    }

    public boolean hasEmptyPath() {
        return(getPath().isEmpty());
    }

    public Uri clone() {
        Uri result = new Uri();
        result.setScheme(getScheme());
        result.setAuthority(getAuthority());
        result.setPath(getPath());
        result.setQuery(getQuery());
        return(result);
    }

    public URI toURI() {
        try {
            return(new URI(toString()));
        } catch (URISyntaxException e) {
            throw(new IllegalStateException(e));
        }
    }

    public String toString() {
        return(getAbsoluteName());
    }

}

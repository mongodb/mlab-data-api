package org.objectlabs.ns;

import java.net.URI;
import java.net.URISyntaxException;

/******************************************************************************
 * Uri
 *
 * @author William Shulman
 *
 * 02.07.2010
 */
public class Uri {

    /*******************************************************************
     * Constructor
     */
    public Uri() {
        super();
    }

    /*******************************************************************
     * Constructor
     */
    public Uri(URI uri) {
        this(uri.getScheme(),
             uri.getAuthority(),
             uri.getSchemeSpecificPart(),
             uri.getPath(),
             uri.getQuery());
    }

    /*******************************************************************
     * Constructor
     */
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

    /*******************************************************************
     * Constructor
     */
    public Uri(String scheme, String path) {
        this(scheme, null, path);
    }

    /*******************************************************************
     * Constructor
     */
    public Uri(String scheme, String authority, String path) {
        this(scheme, authority, null, path, null);
    }

    /*******************************************************************
     * Constructor
     */
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

    /*******************************************************************
     * Constructor
     */
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

    /*******************************************************************
     * scheme
     */
    private String scheme;

    public String getScheme() {
        return(scheme);
    }

    public void setScheme(String value) {
        scheme = value;
    }

    /*******************************************************************
     * authority
     */
    private Authority authority;

    public Authority getAuthority() {
        return(authority);
    }

    public void setAuthority(Authority value) {
        authority = value;
    }

    /*******************************************************************
     * path
     */
    private Path path;

    public Path getPath() {
        if (path == null) path = new Path();
        return(path);
    }

    public void setPath(Path value) {
        path = value;
    }

    /*******************************************************************
     * query
     */
    private String query;

    public String getQuery() {
        return(query);
    }

    public void setQuery(String value) {
        query = value;
    }

    /*******************************************************************
     * name
     */
    public String getName() {
        Path path = getPath();
        if (path.isEmpty()) {
            return(null);
        }
        return(path.getHead());
    }

    /*******************************************************************
     * absoluteName
     */
    public String getAbsoluteName() { // XXX should have query?
        StringBuffer buff = new StringBuffer();

        if (getScheme() != null) buff.append(getScheme()).append(":");
        if (getAuthority() != null) buff.append(getAuthority());
        buff.append(getPath());
        if (getQuery() != null) buff.append("?").append(getQuery());

        return(buff.toString());
    }

    /*******************************************************************
     * absolute
     */
    public boolean isAbsolute() {
        return(getPath().isAbsolute());
    }

    /*******************************************************************
     * head
     */
    public String getHead() {
        return(getPath().getHead());
    }

    /*******************************************************************
     * tail
     */
    public Uri getTail() {
        Uri result = clone();
        result.setPath(getPath().getTail());
        return(result);
    }

    /*******************************************************************
     * parent
     */
    public Uri getParent() {
        Uri result = clone();
        result.setPath(getPath().getParent());
        return(result);
    }

    /*******************************************************************
     * resolve
     */
    public Uri resolve(String name) {
        throw(new IllegalArgumentException("not implemented"));
        // XXX do we want? might be useful
    }

    /*******************************************************************
     * hasEmptyPath
     */
    public boolean hasEmptyPath() {
        return(getPath().isEmpty());
    }

    /*******************************************************************
     * clone
     */
    public Uri clone() {
        Uri result = new Uri();
        result.setScheme(getScheme());
        result.setAuthority(getAuthority());
        result.setPath(getPath());
        result.setQuery(getQuery());
        return(result);
    }

    /*******************************************************************
     * toURI
     */
    public URI toURI() {
        try {
            return(new URI(toString()));
        } catch (URISyntaxException e) {
            throw(new IllegalStateException(e));
        }
    }

    /*******************************************************************
     * toString
     */
    public String toString() {
        return(getAbsoluteName());
    }

}

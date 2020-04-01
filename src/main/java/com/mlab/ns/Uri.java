package com.mlab.ns;

import java.net.URI;

public class Uri {

  private String scheme;
  private Authority authority;
  private Path path;
  private String query;

  public Uri() {
    super();
  }

  public Uri(final URI uri) {
    this(
        uri.getScheme(),
        uri.getAuthority(),
        uri.getSchemeSpecificPart(),
        uri.getPath(),
        uri.getQuery());
  }

  public Uri(final String uri) {
    try {
      final URI u = new URI(uri);
      configure(
          u.getScheme(), u.getAuthority(), u.getSchemeSpecificPart(), u.getPath(), u.getQuery());
    } catch (final Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  public Uri(final String scheme, final String path) {
    this(scheme, null, path);
  }

  public Uri(final String scheme, final String authority, final String path) {
    this(scheme, authority, null, path, null);
  }

  public Uri(
      final String scheme,
      final String authority,
      final String ssp,
      final String path,
      final String query) {
    configure(scheme, authority, ssp, path, query);
  }

  public Uri(final String scheme, final Authority authority, final Path path, final String query) {
    configure(scheme, authority, path, query);
  }

  private void configure(
      final String scheme,
      final String authority,
      final String ssp,
      String path,
      final String query) {
    if (path == null) {
      path = ssp;
    }
    configure(scheme, authority == null ? null : new Authority(authority), new Path(path), query);
  }

  private void configure(
      final String scheme, final Authority authority, final Path path, final String query) {
    setScheme(scheme);
    setAuthority(authority);
    setPath(path);
    setQuery(query);
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(final String value) {
    scheme = value;
  }

  public Authority getAuthority() {
    return authority;
  }

  public void setAuthority(final Authority value) {
    authority = value;
  }

  public Path getPath() {
    if (path == null) path = new Path();
    return path;
  }

  public void setPath(final Path value) {
    path = value;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(final String value) {
    query = value;
  }

  public String getName() {
    final Path path = getPath();
    if (path.isEmpty()) {
      return null;
    }
    return path.getHead();
  }

  public String getAbsoluteName() {
    final StringBuilder s = new StringBuilder();

    if (getScheme() != null) s.append(getScheme()).append(":");
    if (getAuthority() != null) s.append(getAuthority());
    s.append(getPath());
    if (getQuery() != null) s.append("?").append(getQuery());

    return s.toString();
  }

  public boolean isAbsolute() {
    return getPath().isAbsolute();
  }

  public String getHead() {
    return getPath().getHead();
  }

  public Uri getTail() {
    final Uri result = clone();
    result.setPath(getPath().getTail());
    return result;
  }

  public boolean hasEmptyPath() {
    return getPath().isEmpty();
  }

  public Uri clone() {
    final Uri result = new Uri();
    result.setScheme(getScheme());
    result.setAuthority(getAuthority());
    result.setPath(getPath());
    result.setQuery(getQuery());
    return result;
  }

  public String toString() {
    return getAbsoluteName();
  }
}

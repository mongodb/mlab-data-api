package com.mlab.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mlab.ns.Namespace;
import com.mlab.ns.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource implements Namespace {

  private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);
  private String name;
  private Namespace parent;

  private static Logger getLogger() {
    return LOGGER;
  }

  public String getName() {
    if (name == null) {
      name = "";
    }
    return name;
  }

  public void setName(final String value) {
    name = value;
  }

  public String getAbsoluteName() {
    final String result;

    final Namespace parent = getParent();
    if (parent == null) {
      result = "/" + getName();
    } else {
      result = parent.getAbsoluteName() + "/" + getName();
    }

    return result;
  }

  public Namespace getParent() {
    return parent;
  }

  public void setParent(final Namespace value) {
    parent = value;
  }

  public Namespace getRoot() {
    final Namespace parent = getParent();
    if (parent == null) {
      return this;
    }

    return parent.getRoot();
  }

  public Resource resolve(final String name) {
    return resolve(name == null ? null : new Uri(null, name));
  }

  public Resource resolve(final Uri uri) {
    if (uri == null || uri.hasEmptyPath()) {
      return this;
    }

    if (uri.isAbsolute()) {
      final Namespace parent = getParent();
      if (parent != null) {
        return (Resource) parent.resolve(uri);
      }
    }

    return resolveRelative(uri);
  }

  public Resource resolveRelative(final Uri uri) {
    return null;
  }

  public void service(final HttpServletRequest request, final HttpServletResponse response) {
    getLogger().info("service(): " + getName());
  }
}

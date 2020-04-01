package com.mlab.ns;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Path {

  private static final String PATH_SEPARATOR = "/";
  private List<String> path;
  private boolean absolute = false;

  public Path() {
    super();
  }

  public Path(final String path) {
    if (path != null) {
      if (path.startsWith(PATH_SEPARATOR)) {
        setAbsolute(true);
      }
      setPath(parsePath(path));
    }
  }

  public Path(final List<String> path) {
    this(path, false);
  }

  public Path(final List<String> path, final boolean isAbsolute) {
    setPath(path);
    setAbsolute(isAbsolute);
  }

  public List<String> getPath() {
    if (path == null) path = new ArrayList<>();
    return path;
  }

  public void setPath(final List<String> value) {
    path = value;
  }

  public boolean isAbsolute() {
    return absolute;
  }

  public void setAbsolute(final boolean value) {
    absolute = value;
  }

  public boolean isEmpty() {
    return getPath().isEmpty();
  }

  public String getHead() {
    final List<String> path = getPath();
    if (path.isEmpty()) {
      return null;
    }
    return path.get(0);
  }

  public Path getTail() {
    final List<String> path = getPath();
    if (path.isEmpty()) {
      return new Path();
    }
    return new Path(path.subList(1, path.size()));
  }

  public Path getParent() {
    final List<String> path = getPath();
    if (path.size() < 2) {
      return null;
    }

    return new Path(path.subList(0, path.size() - 1), isAbsolute());
  }

  public String toString() {
    final StringBuilder s = new StringBuilder();
    if (isAbsolute()) {
      s.append(PATH_SEPARATOR);
    }

    boolean first = true;
    for (final String elem : getPath()) {
      if (!first) {
        s.append(PATH_SEPARATOR);
      }
      s.append(elem);
      first = false;
    }

    return s.toString();
  }

  protected List<String> parsePath(String path) {
    final List<String> result = new ArrayList<>();

    if (path != null && !path.equals("")) {
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      final String[] pathList = path.split("/");
      for (final String elem : pathList) {
        if (elem != null) {
          result.add(URLDecoder.decode(elem, StandardCharsets.UTF_8));
        }
      }
    }

    return result;
  }
}

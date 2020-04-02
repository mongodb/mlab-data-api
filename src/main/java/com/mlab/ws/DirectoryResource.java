package com.mlab.ws;

import java.util.ArrayList;
import java.util.List;
import com.mlab.http.HttpMethod;
import com.mlab.ns.Uri;
import com.mlab.api.PortalRESTResource;

public class DirectoryResource extends PortalRESTResource {

  private List<Resource> children;

  public String[] getMethods() {
    return new String[] {HttpMethod.GET.name()};
  }

  public List<Resource> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  public void setChildren(final List<Resource> value) {
    children = value;
  }

  public Resource resolveRelative(final Uri uri) {
    final String head = uri.getHead();
    for (final Resource r : getChildren()) {
      if (r.getName().equals(head)) {
        return r.resolve(uri.getTail());
      }
    }
    return null;
  }
}

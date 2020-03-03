package org.objectlabs.ws;

import java.util.ArrayList;
import java.util.List;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.ns.Uri;
import org.olabs.portal.api.PortalRESTResource;

public class DirectoryResource extends PortalRESTResource {

  private List<Resource> children;

  public String[] getMethods() {
    String[] result = {HttpMethod.GET.name()};
    return (result);
  }

  public List<Resource> getChildren() {
    if (children == null) {
      children = new ArrayList<Resource>();
    }
    return (children);
  }

  public void setChildren(List<Resource> value) {
    children = value;
  }

  public Resource resolveRelative(Uri uri) {
    String head = uri.getHead();
    for (Resource r : getChildren()) {
      if (r.getName().equals(head)) {
        return (r.resolve(uri.getTail()));
      }
    }
    return (null);
  }
}

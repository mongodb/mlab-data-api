package org.olabs.portal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.ns.Uri;
import org.objectlabs.ws.RequestContext;
import org.objectlabs.ws.Resource;
import org.objectlabs.ws.ResourceException;

public class AccountClustersResource extends PortalRESTResource {

  private String[] methods = {HttpMethod.GET.name()};

  public String[] getMethods() {
    return (methods);
  }

  @Override
  public Object handleGet(Map parameters, RequestContext context) throws ResourceException {
    final List<String> result = new ArrayList<>();
    result.add("test");
    return (result);
  }

  public Resource resolveRelative(Uri uri) {
    if (uri.hasEmptyPath()) {
      return (this);
    }

    String head = uri.getHead();
    if (head == null || head.equals("")) {
      return (this);
    }

    return null;
  }
}

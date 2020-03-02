package org.olabs.portal.api;

import java.util.Map;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.ns.Uri;
import org.objectlabs.ws.RequestContext;
import org.objectlabs.ws.Resource;
import org.objectlabs.ws.ResourceException;

public class ClustersResource extends PortalRESTResource {

  private String[] methods = {HttpMethod.GET.name()};

  public String[] getMethods() {
    return (methods);
  }

  @Override
  public Object handleGet(Map parameters, RequestContext context) throws ResourceException {
    return ApiConfig.getInstance().getClusters().keySet();
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

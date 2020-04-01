package com.mlab.api;

import java.util.Collections;
import java.util.Map;
import com.mlab.http.HttpMethod;
import com.mlab.ns.Uri;
import com.mlab.ws.RequestContext;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;

public class ClustersResource extends PortalRESTResource {

  private String[] methods = {HttpMethod.GET.name()};

  public String[] getMethods() {
    return (methods);
  }

  @Override
  public Object handleGet(Map parameters, RequestContext context) throws ResourceException {
    final Map<String, String> clusters = getApiConfig().getClusters();
    return clusters == null ? Collections.emptyList() : clusters.keySet();
  }

  public Resource resolveRelative(Uri uri) {
    if (uri.hasEmptyPath()) {
      return (this);
    }

    String head = uri.getHead();
    if (head == null || head.equals("")) {
      return (this);
    }

    Resource r = new ClusterResource(head);
    r.setParent(this);
    return (r.resolve(uri.getTail()));
  }
}

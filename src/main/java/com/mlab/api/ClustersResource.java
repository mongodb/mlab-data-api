package com.mlab.api;

import java.net.http.HttpClient;
import java.util.Collections;
import java.util.Map;
import com.mlab.http.HttpMethod;
import com.mlab.ns.Uri;
import com.mlab.ws.RequestContext;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;
import org.apache.http.protocol.HTTP;

public class ClustersResource extends PortalRESTResource {

  private final String[] METHODS = {HttpMethod.GET.name()};

  public String[] getMethods() {
    return METHODS;
  }

  @Override
  public Object handleGet(final Map parameters, final RequestContext context)
      throws ResourceException {
    final Map<String, String> clusters = getApiConfig().getClusters();
    return clusters == null ? Collections.emptyList() : clusters.keySet();
  }

  public Resource resolveRelative(final Uri uri) {
    if (uri.hasEmptyPath()) {
      return this;
    }

    final String head = uri.getHead();
    if (head == null || head.equals("")) {
      return this;
    }

    final Resource r = new ClusterResource(head);
    r.setParent(this);
    return r.resolve(uri.getTail());
  }
}

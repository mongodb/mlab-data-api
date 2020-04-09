package com.mlab.api;

import com.mongodb.MongoClient;
import javax.servlet.http.HttpServletResponse;
import com.mlab.ns.Uri;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;

public class ClusterResource extends PortalRESTResource {

  private String name;

  public ClusterResource(final String pName) {
    super();
    setName(pName);
  }

  public String getName() {
    return name;
  }

  public void setName(final String pName) {
    name = pName;
  }

  public MongoClient getClusterConnection() {
    return ApiConfig.getInstance().getClusterConnection(getName());
  }

  public Resource resolveRelative(final Uri uri) {
    Resource result = null;

    final String head = uri.getHead();
    Resource r = null;
    if (head.equals("databases")) {
      r = new MongoDBConnectionResource(getClusterConnection());
    } else if (head.equals("runCommand")) {
      r = new RunCommandResource(getClusterConnection().getDatabase("admin"));
    }

    if (r != null) {
      r.setParent(this);
      result = r.resolve(uri.getTail());
    }

    return result;
  }
}

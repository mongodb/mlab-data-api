package org.olabs.portal.api;

import com.mongodb.MongoClient;
import javax.servlet.http.HttpServletResponse;
import org.objectlabs.ns.Uri;
import org.objectlabs.ws.Resource;
import org.objectlabs.ws.ResourceException;

public class ClusterResource extends PortalRESTResource {

  public ClusterResource(final String pName) {
    super();
    setName(pName);
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String pName) {
    name = pName;
  }

  public MongoClient getClusterConnection() {
    return ApiConfig.getInstance().getClusterConnection(getName());
  }

  public Resource resolveRelative(Uri uri) {
    Resource result = null;

    String head = uri.getHead();
    Resource r = null;
    if (head.equals("databases")) {
      r = new MongoDBConnectionResource(getClusterConnection());
    } else if (head.equals("runCommand")) {
      if(getAuthDb().equals("admin")) {
        r = new RunCommandResource(getClusterConnection().getDatabase("admin"));
      } else {
        throw new ResourceException(HttpServletResponse.SC_NOT_FOUND);
      }
    } else if (head.equals("listDatabases")) {
      r = new ListDatabasesResource();
    }

    if (r != null) {
      r.setParent(this);
      result = r.resolve(uri.getTail());
    }

    return (result);
  }

  private String getAuthDb() {
    return getApiConfig().getClusterUri(getName()).getDatabase();
  }
}

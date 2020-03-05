package org.olabs.portal.api;

import org.objectlabs.ns.Uri;
import org.objectlabs.ws.Resource;

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

  public Resource resolveRelative(Uri uri) {
    Resource result = null;

    String head = uri.getHead();
    Resource r = null;
    if (head.equals("databases")) {
      r = new MongoDBConnectionResource(ApiConfig.getInstance().getClusterConnection(getName()));
      /*
    } else if (head.equals("runCommand")) {
      r = new RunCommandResource(getCluster().getDb("admin"));
    } else if (head.equals("listDatabases")) {
      Plan plan = getCluster().getSubscription().getPlan();
      if (plan != null) {
        r = new ListDatabasesResource(plan.listDatabases(getCluster()));
      }
       */
    }

    if (r != null) {
      r.setParent(this);
      result = r.resolve(uri.getTail());
    }

    return (result);
  }
}

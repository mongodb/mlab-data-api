package org.olabs.portal.api;

import org.objectlabs.ws.HttpRequestResource;
import org.objectlabs.ws.Resource;

import java.util.ArrayList;
import java.util.List;

public class RootResource extends HttpRequestResource {

  public String getName() {
    return("");
  }

  @Override
  public List<Resource> getChildren() {
    List<Resource> children = super.getChildren();
    if (children == null || children.isEmpty()) {
      children = makeChildren(this);
      setChildren(children);
    }
    return children;
  }

  public static List<Resource> makeChildren(Resource r) {
    List<Resource> children = new ArrayList<Resource>(2);
    children.add(makeClustersResource(r));
    return children;
  }

  private static Resource makeClustersResource(Resource parent) {
    Resource result = new AccountClustersResource();
    result.setName("clusters");
    result.setParent(parent);
    return result;
  }

  /*

  private static Resource makeDatabasesResource(Resource parent) {
    Resource result = new AccountDatabasesResource();
    result.setName("databases");
    result.setParent(parent);
    return result;
  }
 */

}

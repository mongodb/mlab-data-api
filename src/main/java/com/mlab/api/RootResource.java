package com.mlab.api;

import com.mlab.ws.DirectoryResource;
import com.mlab.ws.Resource;

import java.util.ArrayList;
import java.util.List;

public class RootResource extends DirectoryResource {

  public static List<Resource> makeChildren(final Resource r) {
    final List<Resource> children = new ArrayList<>(2);
    children.add(makeClustersResource(r));
    children.add(makeDatabasesResource(r));
    return children;
  }

  private static Resource makeClustersResource(final Resource parent) {
    final Resource result = new ClustersResource();
    result.setName("clusters");
    result.setParent(parent);
    return result;
  }

  private static Resource makeDatabasesResource(final Resource parent) {
    final Resource result = new DatabasesResource();
    result.setName("databases");
    result.setParent(parent);
    return result;
  }

  public String getName() {
    return "";
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
}

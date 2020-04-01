package com.mlab.api;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoDatabase;
import java.util.Map;
import com.mlab.http.HttpMethod;
import com.mlab.ns.Uri;
import com.mlab.ws.RequestContext;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;

public class DatabaseResource extends PortalRESTResource {

  private String[] methods = {HttpMethod.GET.name()};
  private MongoDatabase database;

  public DatabaseResource(final MongoDatabase db) {
    super();
    setDatabase(db);
  }

  public String[] getMethods() {
    return (methods);
  }

  public String getName() {
    return (getDatabase().getName());
  }

  public MongoDatabase getDatabase() {
    return (database);
  }

  public void setDatabase(final MongoDatabase value) {
    database = value;
  }

  @Override
  public Object handleGet(final Map parameters, final RequestContext context) throws ResourceException {
    final BasicDBList result = new BasicDBList();
    result.add("collections");
    result.add("commands");
    result.add("runCommand");
    return (result);
  }

  public Resource resolveRelative(final Uri uri) {

    Resource r = null;
    final String head = uri.getHead();
    if (head.equals("collections")) {
      r = new DBCollectionsResource(getDatabase());
    } else if (head.equals("users")) {
      r = new DBUsersResource(getDatabase());
    } else if (head.equals("commands")) {
      r = new CommandsResource(getDatabase());
    } else if (head.equals("runCommand")) {
      r = new RunCommandResource(getDatabase());
    }

    /*
    } else if (head.equals("commands")) {
      r = new CommandsResource(getDatabase());
    } else if (head.equals("status")) {
      r = new DBStatusResource(getDatabase());
   */

    Resource result = null;
    if (r != null) {
      r.setParent(this);
      result = r.resolve(uri.getTail());
    }

    return (result);
  }
}

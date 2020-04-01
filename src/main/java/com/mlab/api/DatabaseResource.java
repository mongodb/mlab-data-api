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

  private final String[] METHODS = {HttpMethod.GET.name()};
  private MongoDatabase database;

  public DatabaseResource(final MongoDatabase db) {
    super();
    setDatabase(db);
  }

  public String[] getMethods() {
    return METHODS;
  }

  public String getName() {
    return getDatabase().getName();
  }

  public MongoDatabase getDatabase() {
    return database;
  }

  public void setDatabase(final MongoDatabase value) {
    database = value;
  }

  @Override
  public Object handleGet(final Map parameters, final RequestContext context)
      throws ResourceException {
    final BasicDBList result = new BasicDBList();
    result.add("collections");
    result.add("commands");
    result.add("runCommand");
    return result;
  }

  public Resource resolveRelative(final Uri uri) {

    Resource r = null;
    final String head = uri.getHead();
    switch (head) {
      case "collections":
        r = new DBCollectionsResource(getDatabase());
        break;
      case "users":
        r = new DBUsersResource(getDatabase());
        break;
      case "commands":
        r = new CommandsResource(getDatabase());
        break;
      case "runCommand":
        r = new RunCommandResource(getDatabase());
        break;
    }

    if (r != null) {
      r.setParent(this);
      return r.resolve(uri.getTail());
    }

    return null;
  }
}

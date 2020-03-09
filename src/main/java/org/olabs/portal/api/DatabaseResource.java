package org.olabs.portal.api;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoDatabase;
import java.util.Map;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.ns.Uri;
import org.objectlabs.ws.RequestContext;
import org.objectlabs.ws.Resource;
import org.objectlabs.ws.ResourceException;

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

  public void setDatabase(MongoDatabase value) {
    database = value;
  }

  @Override
  public Object handleGet(Map parameters, RequestContext context) throws ResourceException {
    BasicDBList result = new BasicDBList();
    result.add("collections");
    result.add("commands");
    result.add("runCommand");
    return (result);
  }

  public Resource resolveRelative(Uri uri) {

    Resource r = null;

    /*
    String head = uri.getHead();
    if (head.equals("collections")) {
      r = new DBCollectionsResource(getDatabase());
    } else if (head.equals("commands")) {
      r = new CommandsResource(getDatabase());
    } else if (head.equals("runCommand")) {
      r = new RunCommandResource(getDatabase());
    } else if (head.equals("status")) {
      r = new DBStatusResource(getDatabase());
    } else if (head.equals("users")) {
      r = new DBUsersResource(getDatabase());
    }
   */

    Resource result = null;
    if (r != null) {
      r.setParent(this);
      result = r.resolve(uri.getTail());
    }

    return (result);
  }
}

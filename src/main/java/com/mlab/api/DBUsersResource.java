package com.mlab.api;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import com.mlab.http.HttpMethod;
import com.mlab.ws.RequestContext;
import com.mlab.ws.ResourceException;

public class DBUsersResource extends PortalRESTResource {

  private static final String[] METHODS = new String[] {HttpMethod.GET.name()};
  private MongoDatabase database;

  public DBUsersResource(final MongoDatabase db) {
    setDatabase(db);
  }

  public String[] getMethods() {
    return METHODS;
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
    final List<Document> users =
        (List<Document>) getDatabase().runCommand(new BasicDBObject("usersInfo", 1)).get("users");
    // remove credentials, just in case
    return users.stream()
        .peek(u -> u.remove("credentials"))
        .map(BasicDBObject::new)
        .collect(Collectors.toList());
  }
}

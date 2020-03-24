package org.olabs.portal.api;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import java.util.Map;
import org.bson.Document;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.ws.RequestContext;
import org.objectlabs.ws.ResourceException;

public class CommandsResource extends PortalRESTResource {

  public CommandsResource(final MongoDatabase db) {
    super();
    setDatabase(db);
  }

  private static final String[] METHODS = {HttpMethod.GET.name()};

  public String[] getMethods() {
    return METHODS;
  }

  public String getName() {
    return "commands";
  }

  private MongoDatabase database;

  public MongoDatabase getDatabase() {
    return database;
  }

  public void setDatabase(final MongoDatabase value) {
    database = value;
  }

  @Override
  public Object handleGet(final Map parameters, final RequestContext context)
      throws ResourceException {
    final MongoDatabase db = getDatabase();
    final Document result = db.runCommand(new BasicDBObject("listCommands", 1));
    if (result.getDouble("ok") <= 0) {
      throw new ResourceException(result.getString("errmsg"));
    }
    return result;
  }
}

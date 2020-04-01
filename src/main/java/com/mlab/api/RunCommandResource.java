package com.mlab.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.bson.BSONObject;
import org.bson.BsonDocument;
import com.mlab.http.HttpMethod;
import com.mlab.mongodb.SecurityUtils;
import com.mlab.ws.RequestContext;
import com.mlab.ws.ResourceException;

public class RunCommandResource extends PortalRESTResource {
  public static final List<String> SUPPORTED_COMMANDS =
      Arrays.asList(
          "getLastError",
          "getPrevError",
          "ping",
          "profile",
          "repairDatabase",
          "whatsmyuri",
          "aggregate",
          "convertToCapped",
          "distinct",
          "findAndModify",
          "geoNear",
          "reIndex",
          "collStats",
          "dbStats");
  public static final List<String> SUPPORTED_ADMIN_COMMANDS =
      Arrays.asList(
          "isMaster",
          "replSetGetStatus",
          "replSetStepDown",
          "replSetFreeze",
          "serverStatus",
          "getCmdLineOpts",
          "top");
  public static final List<String> SUPPORTED_PARAMETERS =
      Collections.singletonList("failIndexKeyTooLong");
  private static final String[] METHODS = {HttpMethod.GET.name(), HttpMethod.POST.name()};
  private MongoDatabase database;

  public RunCommandResource(final MongoDatabase db) {
    super();
    setDatabase(db);
  }

  public String[] getMethods() {
    return METHODS;
  }

  public String getName() {
    return "runCommand";
  }

  public MongoDatabase getDatabase() {
    return database;
  }

  private void setDatabase(final MongoDatabase value) {
    database = value;
  }

  @Override
  protected Object handlePost(final Object o, final RequestContext context)
      throws ResourceException {
    final BasicDBObject object = (BasicDBObject) o;

    final String command = getCommand(object);
    validateCommand(command, object);

    try {
      return getDatabase().runCommand(object);
    } catch (final MongoCommandException e) {
      final BsonDocument response = e.getResponse();
      final BasicDBObject err = new BasicDBObject("ok", response.getDouble("ok").doubleValue());
      if (response.containsKey("errmsg")) {
        err.append("errmsg", response.getString("errmsg").toString());
      }
      if (response.containsKey("ns")) {
        err.append("ns", response.getString("ns").toString());
      }
      return err;
    } catch (final Exception e) {
      // replSetStepDown causes the server to drop all connections,
      // so running it is guaranteed to cause an exception
      if (object.containsField("replSetStepDown")) {
        final DBObject uiResult = new BasicDBObject();
        uiResult.put(
            "message",
            Arrays.asList(
                "replSetStepDown command has been issued.",
                "Your cluster may be unavailable for a bit while a new primary is elected.",
                "Refresh the cluster's page to see if it was successful.",
                "If this server is still the primary, try a longer step down time."));
        return uiResult;
      }
      if (hasCause(e, SocketTimeoutException.class)) {
        final DBObject uiResult = new BasicDBObject();
        uiResult.put(
            "message",
            Arrays.asList(
                "The " + command + " command was issued, but the request timed out.",
                "Usually this means the command was successfully started in the database,",
                "but it will take a while to complete."));
        return uiResult;
      }
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "An internal server error occurred while running the command",
          e);
    }
  }

  private String getCommand(final DBObject object) {
    // The command name is the name of the first field in the document.
    return getCommandField(object, 0);
  }

  private String getCommandField(final DBObject object, int index) {
    // DBObject inherits from LinkedHashMap, whose iterators have a stable order.
    if (object != null) {
      final Iterator<String> fields = object.keySet().iterator();
      while (index > 0 && fields.hasNext()) {
        fields.next();
        index--;
      }
      if (fields.hasNext()) {
        return fields.next();
      }
    }
    return null;
  }

  private List<String> getSupportedCommands() {
    return SUPPORTED_COMMANDS;
  }

  private List<String> getSupportedAdminCommands() {
    return SUPPORTED_ADMIN_COMMANDS;
  }

  private List<String> getSupportedParameters() {
    return SUPPORTED_PARAMETERS;
  }

  private boolean isSupportedCommand(final String command, final DBObject object) {
    if (command.equals("setParameter")) {
      return isSupportedParameter(getCommandField(object, 1));
    } else {
      return getSupportedCommands().contains(command)
          || getDatabase().getName().equals("admin")
              && getSupportedAdminCommands().contains(command);
    }
  }

  private boolean isSupportedParameter(final String parameter) {
    return getSupportedParameters().contains(parameter);
  }

  private void validateCommand(final String command, final DBObject object) {
    if (command == null) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST, "Please specify a command to run.");
    }
    if (!isSupportedCommand(command, object)) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "The command you are trying to run, '" + object + "', is invalid or unsupported.");
    }
    final List<BSONObject> js = SecurityUtils.findJavaScriptInCommand(object);
    if (js != null) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "mLab does not support JavaScript in commands. " + js);
    }
  }

  private boolean hasCause(final Throwable t, final Class cause) {
    return t != null && (cause.isInstance(t) || hasCause(t.getCause(), cause));
  }
}

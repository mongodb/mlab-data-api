package org.olabs.portal.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.mongodb.MongoUtils;
import org.objectlabs.ws.RequestContext;
import org.objectlabs.ws.WebServiceException;

public class ListDatabasesResource extends PortalRESTResource {

  public MongoClient getClient() {
    return ApiConfig.getInstance().getClusterConnection(getParent().getName());
  }

  public String getName() {
    return "listDatabases";
  }

  public String[] getMethods() {
    return new String[] {HttpMethod.GET.name()};
  }

  protected Object handleGet(final Map parameters, final RequestContext context)
      throws WebServiceException {
    List<DBObject> dbs = getDbs();
    if (parameters.containsKey("systemOnly")) {
      dbs = new ArrayList<>(dbs);
      dbs.removeIf(d -> !MongoUtils.SYSTEM_DB_NAMES.contains(d.get("name")));
    } else if (parameters.containsKey("userOnly")) {
      dbs = new ArrayList<>(dbs);
      dbs.removeIf(d -> MongoUtils.SYSTEM_DB_NAMES.contains(d.get("name")));
    }
    return dbs;
  }

  private List<DBObject> getDbs() {
    final String authDb = getApiConfig().getClusterUri(getParent().getName()).getDatabase();
    if (authDb == null) {
      return Collections.emptyList();
    }
    if (MongoUtils.ADMIN_DB_NAME.equals(authDb)) {
      return getClient().listDatabases(BasicDBObject.class).into(new ArrayList<>());
    } else {
      // simulate listDatabases for single db
      final BasicDBObject db = new BasicDBObject("name", authDb);
      final Document stats =
          getClient().getDatabase(authDb).runCommand(new BasicDBObject("dbStats", 1));
      if (stats != null) {
        db.append("sizeOnDisk", stats.getDouble("storageSize") + stats.getDouble("indexSize"));
      } else {
        db.append("sizeOnDisk", 0d);
      }
      db.append("empty", db.getDouble("sizeOnDisk") <= 0);
      return List.of(db);
    }
  }
}

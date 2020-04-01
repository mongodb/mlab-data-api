package com.mlab.api;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.mlab.http.HttpMethod;
import com.mlab.mongodb.MongoUtils;
import com.mlab.ns.Uri;
import com.mlab.ws.RequestContext;
import com.mlab.ws.Resource;
import com.mlab.ws.WebServiceException;

public class MongoDBConnectionResource extends PortalRESTResource implements MongoBegotten {

  public MongoDBConnectionResource(MongoClient mongo) {
    this.mongo = mongo;
  }

  public String getName() {
    return ("databases");
  }

  public String[] getMethods() {
    return (new String[] {HttpMethod.GET.name()});
  }

  private final MongoClient mongo;

  public MongoClient getMongo() {
    return (mongo);
  }

  protected Object handleGet(Map parameters, RequestContext context) throws WebServiceException {
    Set<String> names = new TreeSet<String>();
    if (parameters.containsKey("systemOnly")) {
      names.addAll(getDbNames());
      names.retainAll(MongoUtils.SYSTEM_DB_NAMES);
    } else {
      names.addAll(getDbNames());
      if (parameters.containsKey("userOnly")) {
        names.removeAll(MongoUtils.SYSTEM_DB_NAMES);
      }
    }
    BasicDBList result = new BasicDBList();
    result.addAll(names);
    return result;
  }

  private Collection<String> getDbNames() {
    final String authDb =
        getApiConfig().getClusterUri(getParent().getName()).getDatabase();
    return authDb.equals(MongoUtils.ADMIN_DB_NAME)
        ? getMongo().listDatabaseNames().into(new ArrayList<>())
        : List.of(authDb);
  }

  public Resource resolveRelative(Uri uri) {
    Resource result = null;

    String dbName = uri.getHead();
    if (dbName == null) {
      return null;
    }
    MongoDatabase db = getMongo().getDatabase(dbName);
    if (db != null) {
        result = new DatabaseResource(db);
        result.setParent(this);
        return result.resolve(uri.getTail());
    }

    return (result);
  }
}

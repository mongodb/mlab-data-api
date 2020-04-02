package com.mlab.api;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoDatabase;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import com.mlab.http.HttpMethod;
import com.mlab.mongodb.MongoUtils;
import com.mlab.ns.Uri;
import com.mlab.ws.RequestContext;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;

public class DBCollectionsResource extends PortalRESTResource {

  private final String[] METHODS = {HttpMethod.GET.name()};
  private MongoDatabase database;

  public DBCollectionsResource(final MongoDatabase db) {
    super();
    setDatabase(db);
  }

  public String[] getMethods() {
    return METHODS;
  }

  public String getName() {
    return "collections";
  }

  public MongoDatabase getDatabase() {
    return database;
  }

  public void setDatabase(final MongoDatabase value) {
    database = value;
  }

  public Object handleGet(final Map parameters, final RequestContext context)
      throws ResourceException {
    final Collection<String> sorted =
        new TreeSet<>(
            (s, s2) -> {
              int cmp = s.compareToIgnoreCase(s2);
              if (cmp == 0) {
                cmp = s.compareTo(s2);
              }
              return cmp;
            });
    final String type = (String) parameters.get("type");
    if (type != null && type.equals("view")) {
      sorted.addAll(MongoUtils.getViewNames(getDatabase()));
    } else {
      if (type == null) {
        getDatabase().listCollectionNames().into(sorted);
      } else if (type.equals("collection")) {
        sorted.addAll(MongoUtils.getNonViewCollectionNames(getDatabase()));
      }
      final String dbName = getDatabase().getName();
      if (dbName.equals("local") || dbName.equals("admin")) {
        sorted.remove("system.users");
      }
    }
    sorted.remove("system.views");
    final BasicDBList result = new BasicDBList();
    result.addAll(sorted);
    return result;
  }

  public Resource resolveRelative(final Uri uri) {
    final Resource r = new CollectionResource(getDatabase().getCollection(uri.getHead()));
    r.setParent(this);
    return r.resolve(uri.getTail());
  }
}

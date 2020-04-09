package com.mlab.api;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnectionResource extends PortalRESTResource {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDBConnectionResource.class);

  private final MongoClient mongo;

  public MongoDBConnectionResource(final MongoClient mongo) {
    this.mongo = mongo;
  }

  public String getName() {
    return "databases";
  }

  public String[] getMethods() {
    return new String[] {HttpMethod.GET.name()};
  }

  public MongoClient getMongo() {
    return mongo;
  }

  protected Object handleGet(final Map parameters, final RequestContext context)
      throws WebServiceException {
    final Set<String> names = new TreeSet<>();
    if (parameters.containsKey("systemOnly")) {
      names.addAll(getDbNames());
      names.retainAll(MongoUtils.SYSTEM_DB_NAMES);
    } else {
      names.addAll(getDbNames());
      if (parameters.containsKey("userOnly")) {
        names.removeAll(MongoUtils.SYSTEM_DB_NAMES);
      }
    }
    final BasicDBList result = new BasicDBList();
    result.addAll(names);
    return result;
  }

  private Collection<String> getDbNames() {
    try {
      return getMongo().listDatabaseNames().into(new ArrayList<>());
    } catch (final MongoCommandException e) {
      if (e.getErrorCode() == 13) {
        final String authDb = getApiConfig().getClusterUri(getParent().getName()).getDatabase();
        return authDb == null ? Collections.emptyList() : List.of(authDb);
      }
      throw e;
    }
  }

  public Resource resolveRelative(final Uri uri) {
    final String dbName = uri.getHead();
    if (dbName == null) {
      return null;
    }
    final MongoDatabase db = getMongo().getDatabase(dbName);
    if (db != null) {
      final Resource result = new DatabaseResource(db);
      result.setParent(this);
      return result.resolve(uri.getTail());
    }

    return null;
  }
}

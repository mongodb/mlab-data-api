package org.objectlabs.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MongoUtils {

  public static final String ADMIN_DB_NAME = "admin";
  public static final String LOCAL_DB_NAME = "local";

  public static final Collection<String> SYSTEM_DB_NAMES = new ArrayList<String>();

  static {
    SYSTEM_DB_NAMES.add(LOCAL_DB_NAME);
    SYSTEM_DB_NAMES.add(ADMIN_DB_NAME);
    SYSTEM_DB_NAMES.add("config");
  }

  public static final Collection<String> READ_ONLY_DB_NAMES = new ArrayList<String>();

  static {
    READ_ONLY_DB_NAMES.add("config");
    READ_ONLY_DB_NAMES.add(LOCAL_DB_NAME);
  }

  public static final Collection<String> READ_ONLY_COLLECTION_NAMES = new ArrayList<String>();

  static {
    READ_ONLY_COLLECTION_NAMES.add("system.indexes");
    READ_ONLY_COLLECTION_NAMES.add("system.namespaces");
    READ_ONLY_COLLECTION_NAMES.add("objectlabs-system");
  }

  public static List<String> getViewNames(MongoDatabase db) {
    return db.listCollections()
        .filter(new BasicDBObject("type", "view"))
        .map(result -> (String) result.get("name"))
        .into(new LinkedList<>());
  }

  public static Collection<String> getNonViewCollectionNames(MongoDatabase db) {
    List<String> names =
        db.listCollections()
            .filter(new BasicDBObject("type", "collection"))
            .map(result -> (String) result.get("name"))
            .into(new LinkedList<>());
    if (names.isEmpty()) {
      return db.listCollectionNames().into(new LinkedList<>());
    } else {
      return names;
    }
  }
}

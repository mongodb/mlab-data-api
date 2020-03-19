package org.objectlabs.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.bson.types.ObjectId;

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

  public static DBObject findOne(MongoCollection collection, BasicDBObject query) {
    final MongoCursor cursor = collection.find(query).limit(1).iterator();
    return cursor.hasNext() ? (DBObject) cursor.next() : null;
  }

  public static DBObject findOneById(MongoCollection collection, Object id) {
    return findOne(collection, new BasicDBObject("_id", id));
  }

  public static DBObject findOneByStringId(MongoCollection collection, String docid) {
    Object id;
    try {
      id = new ObjectId(docid);
    } catch (IllegalArgumentException e) {
      id = docid;
    }
    DBObject doc = findOneById(collection, id);
    if (doc == null) {
      // maybe they're using the oid string as a literal id?
      doc = findOneById(collection, docid);
    }
    if (doc == null) {
      // maybe the id is supposed to be a number?
      try {
        doc = findOneById(collection, Long.valueOf(docid));
      } catch (NumberFormatException e) {
        // nope, guess not
        // how bout a double?
        try {
          doc = findOneById(collection, Double.valueOf(docid));
        } catch (NumberFormatException f) {
          // bah, fine, I give up
        }
      }
    }
    if (doc == null) {
      // maybe it's a UUID?
      try {
        doc = findOneById(collection, UUID.fromString(docid));
      } catch (IllegalArgumentException e) {
        // guess not
      }
    }
    return doc;
  }

  public static boolean isDatabaseReadOnly(MongoDatabase db) {
    return isDatabaseReadOnly(db.getName());
  }

  public static boolean isDatabaseReadOnly(String dbName) {
    return READ_ONLY_DB_NAMES.contains(dbName);
  }

  public static boolean isCollectionReadOnly(MongoCollection c) {
    return isCollectionReadOnly(c.getNamespace().getDatabaseName(), c.getNamespace().getCollectionName());
  }

  public static boolean isCollectionReadOnly(MongoDatabase db, String collection) {
    return isCollectionReadOnly(db.getName(), collection);
  }

  public static boolean isCollectionReadOnly(String dbName, String collection) {
    return isDatabaseReadOnly(dbName) || READ_ONLY_COLLECTION_NAMES.contains(collection) || collection.startsWith("objectlabs-system.");
  }

  private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  static {
    ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  public static Date parseISODate(String date) throws ParseException {
    return ISO_DATE_FORMAT.parse(date);
  }
  public static String toISODateString(Date date) {
    return ISO_DATE_FORMAT.format(date);
  }

  public static ObjectId oid(final Integer pId) {
    return new ObjectId(String.format("%024d", pId));
  }
}

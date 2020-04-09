package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import com.mlab.json.JsonParser;
import com.mlab.mongodb.MongoUtils;
import com.mlab.ws.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectResourceIntTests extends ParameterizedClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(CollectionResourceIntTests.class);
  private static final String TEST_DB = "test";
  private static final String TEST_COLLECTION = new ObjectId().toString();
  private static final JsonParser JSON_PARSER = new JsonParser();

  @AfterClass
  public static void afterClass() {
    getTestCollection().drop();
  }

  private static MongoCollection<Document> getTestCollection() {
    return ApiConfig.getInstance()
        .getClusterConnection(DEDICATED_CLUSTER_ID)
        .getDatabase(TEST_DB)
        .getCollection(TEST_COLLECTION);
  }

  @Test
  public void testGet() throws IOException {
    // test fetching documents with different types of IDs
    final List ids =
        List.of(
            new ObjectId(),
            client.getName() + "_string",
            new ObjectId().toHexString(),
            client.getName().hashCode(),
            client.getName().hashCode() + 0.123d,
            UUID.randomUUID());
    for (final Object id : ids) {
      testGet(id);
    }
  }

  @Test
  public void testGet_notFound() throws IOException {
    try {
      client.getJson(getObjectUrl("does_not_exist"));
      fail("Expected request to fail");
    } catch (final ResourceException e) {
      assertEquals(HttpServletResponse.SC_NOT_FOUND, e.getStatusCode());
    }
  }

  @Test
  public void testPut_replace() throws IOException {
    final String variable = "objectReplace";
    final Document doc = makeTestDocument(variable);
    getTestCollection().insertOne(doc);
    doc.put("date", new Date(0));
    doc.put("newField", "new");
    final JSONObject replaced =
        client.putJson(getObjectUrl(doc.get("_id").toString()), JSON_PARSER.serialize(doc));
    assertNotNull(replaced);
    assertEquals(variable, replaced.getString("variable"));
    assertEquals(
        MongoUtils.toISODateString(new Date(0)),
        ((JSONObject) replaced.get("date")).getString("$date"));
  }

  @Test
  public void testPut_update() throws IOException {
    final String variable = "objectReplace";
    final Document doc = makeTestDocument(variable);
    getTestCollection().insertOne(doc);
    final BasicDBObject update =
        new BasicDBObject("$set", new BasicDBObject("date", new Date(0)).append("newField", "new"));
    final JSONObject replaced =
        client.putJson(getObjectUrl(doc.get("_id").toString()), JSON_PARSER.serialize(update));
    assertNotNull(replaced);
    assertEquals(variable, replaced.getString("variable"));
    assertEquals(
        MongoUtils.toISODateString(new Date(0)),
        ((JSONObject) replaced.get("date")).getString("$date"));
  }

  @Test
  public void testDelete() throws IOException {
    // test deleting documents with different types of IDs
    final List ids =
        List.of(
            new ObjectId(),
            client.getName() + "_deleteString",
            new ObjectId().toHexString(),
            client.getName().hashCode() + 3,
            client.getName().hashCode() + 0.456d,
            UUID.randomUUID());
    for (final Object id : ids) {
      testDelete(id);
    }
  }

  private void testGet(final Object id) throws IOException {
    final Document doc = makeTestDocument("objectGet");
    doc.put("_id", id);
    getTestCollection().insertOne(doc);

    final String idClass = doc.get("_id").getClass().getName();
    final JSONObject obj = client.getJson(getObjectUrl(doc.get("_id").toString()));
    assertNotNull(String.format("lookup by %s id failed", idClass), obj);
    assertTestDocumentValid(obj);
    assertEquals("objectGet", obj.getString("variable"));
    assertEquals(
        String.format("IDs of class %s don't match", idClass),
        JSON_PARSER.jsonify(doc.get("_id")),
        JSON_PARSER.jsonify(obj.get("_id")));
  }

  private void testDelete(final Object id) throws IOException {
    final Document doc = makeTestDocument("objectDelete");
    doc.put("_id", id);
    getTestCollection().insertOne(doc);

    final String idClass = doc.get("_id").getClass().getName();
    final JSONObject obj = client.deleteJson(getObjectUrl(doc.get("_id").toString()));
    assertNotNull(String.format("lookup by %s id failed", idClass), obj);
    assertTestDocumentValid(obj);
    assertEquals("objectDelete", obj.getString("variable"));
    assertEquals(
        String.format("IDs of class %s don't match", idClass),
        JSON_PARSER.jsonify(doc.get("_id")),
        JSON_PARSER.jsonify(obj.get("_id")));
    // verify document no longer exists
    assertEquals(0, getTestCollection().countDocuments(new BasicDBObject("_id", id)));
  }

  private String getObjectUrl(final String id) {
    return ApiPathBuilder.start()
        .cluster(DEDICATED_CLUSTER_ID)
        .db(TEST_DB)
        .collection(TEST_COLLECTION)
        .object(id)
        .toString();
  }
}

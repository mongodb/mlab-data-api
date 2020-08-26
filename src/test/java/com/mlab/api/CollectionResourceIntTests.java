package com.mlab.api;

import static com.mlab.mongodb.MongoUtils.toISODateString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.mlab.mongodb.MongoUtils;
import com.mlab.ws.ResourceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionResourceIntTests extends ParameterizedClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(CollectionResourceIntTests.class);
  private static final String TEST_DB = "test";
  private static final String TEST_GET_COLLECTION = new ObjectId().toString();
  private static final String TEST_POST_COLLECTION = new ObjectId().toString();

  @BeforeClass
  public static void setUpClass() {
    // add dummy data for testing GET
    final MongoCollection c =
        ApiConfig.getInstance()
            .getClusterConnection(DEDICATED_CLUSTER_ID)
            .getDatabase(TEST_DB)
            .getCollection(TEST_GET_COLLECTION);
    c.insertMany(List.of(makeTestDocument("1"), makeTestDocument("2"), makeTestDocument("3")));
  }

  @AfterClass
  public static void tearDownClass() {
    try {
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(TEST_GET_COLLECTION)
          .drop();
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(TEST_POST_COLLECTION)
          .drop();
    } catch (final Exception e) {
      LOG.error("Unable to clean up test collections", e);
    }
  }

  @Test
  public void testGet_empty() throws IOException {
    // query non-existent collection
    final String badCollection = "doesnotexist";
    final JSONArray nonExistentResult =
        client.getJsonArray(getCollectionUrl(badCollection).toString());
    assertNotNull(nonExistentResult);
    assertTrue(nonExistentResult.isEmpty());

    // make sure that didn't create a new collection
    assertFalse(
        client
            .getJsonArray(
                ApiPathBuilder.start()
                    .cluster(DEDICATED_CLUSTER_ID)
                    .db(TEST_DB)
                    .collections()
                    .toString())
            .toList()
            .contains(badCollection));

    // query empty collection
    assertTrue(client.getJsonArray(getCollectionUrl("c1").toString()).isEmpty());
  }

  @Test
  public void testGet() throws IOException {
    final JSONArray all = client.getJsonArray(getCollectionUrl(TEST_GET_COLLECTION).toString());
    assertEquals(3, all.length());
    assertTrue(
        all.toList().stream()
            .map(o -> ((Map) o).get("variable"))
            .collect(Collectors.toList())
            .containsAll(List.of("1", "2", "3")));
    all.forEach(o -> assertTestDocumentValid((JSONObject) o));
  }

  @Test
  public void testGet_query() throws IOException {
    // query with filter
    final JSONArray filtered =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("q", new BasicDBObject("variable", "2").toJson())
                .toString());
    assertEquals(1, filtered.length());
    assertEquals("2", ((JSONObject) filtered.get(0)).get("variable"));

    // query with non-matching filter
    final JSONArray unmatched =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("q", new BasicDBObject("variable", "X").toJson())
                .toString());
    assertEquals(0, unmatched.length());
  }

  @Test
  public void testGet_projection() throws IOException {

    // query with projection
    final JSONArray projected =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("f", new BasicDBObject().append("date", 1).append("variable", 1).toJson())
                .toString());
    assertEquals(3, projected.length());
    projected.forEach(
        o -> {
          assertEquals(3, ((JSONObject) o).keySet().size());
          assertNotNull(((JSONObject) o).get("variable"));
          assertNotNull(((JSONObject) o).get("_id"));
          final JSONObject dateVal = (JSONObject) ((JSONObject) o).get("date");
          assertNotNull(dateVal);
          assertEquals(MongoUtils.toISODateString(TEST_DATE), dateVal.get("$date"));
        });
  }

  @Test
  public void testGet_sort() throws IOException {
    // query with sort
    final JSONArray sorted =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("s", new BasicDBObject("variable", 1))
                .toString());
    assertEquals(3, sorted.length());
    assertEquals(
        List.of("1", "2", "3"),
        sorted.toList().stream().map(o -> ((Map) o).get("variable")).collect(Collectors.toList()));
    final JSONArray reversed =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("s", new BasicDBObject("variable", -1))
                .toString());
    assertEquals(3, reversed.length());
    assertEquals(
        List.of("3", "2", "1"),
        reversed.toList().stream()
            .map(o -> ((Map) o).get("variable"))
            .collect(Collectors.toList()));
  }

  @Test
  public void testGet_count() throws IOException {
    // count
    final String count =
        client.get(getCollectionUrl(TEST_GET_COLLECTION).query("c", "true").toString());
    assertEquals("3", count);
    final String filteredCount =
        client.get(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("c", "true")
                .query("q", new BasicDBObject("variable", "1").toJson())
                .toString());
    assertEquals("1", filteredCount);
    final String countIgnoresSkipLimit =
        client.get(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("c", "true")
                .query("l", 2)
                .query("sk", 1)
                .toString());
    assertEquals("3", countIgnoresSkipLimit);
  }

  @Test
  public void testGet_findOne() throws IOException {
    // findOne
    final JSONObject one =
        client.getJson(getCollectionUrl(TEST_GET_COLLECTION).query("fo", "true").toString());
    assertNotNull(one);
    assertEquals(13, one.length());
  }

  @Test
  public void testGet_findOne_sorted() throws IOException {
    // this only works correctly in new API
    assumeTrue(isTestClient());
    final JSONObject oneSorted =
        client.getJson(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("fo", "true")
                .query("s", new BasicDBObject("variable", -1))
                .toString());
    assertNotNull(oneSorted);
    assertEquals("3", oneSorted.get("variable"));
  }

  @Test
  public void testGet_skip() throws IOException {
    final JSONArray results =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("sk", 1)
                .query("s", new BasicDBObject("variable", -1))
                .toString());
    assertEquals(2, results.length());
    assertEquals("2", ((JSONObject) results.get(0)).get("variable"));
    assertEquals("1", ((JSONObject) results.get(1)).get("variable"));
  }

  @Test
  public void testGet_limit() throws IOException {
    final JSONArray results =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("l", 2)
                .query("s", new BasicDBObject("variable", -1))
                .toString());
    assertEquals(2, results.length());
    assertEquals("3", ((JSONObject) results.get(0)).get("variable"));
    assertEquals("2", ((JSONObject) results.get(1)).get("variable"));

    final JSONArray results2 =
        client.getJsonArray(
            getCollectionUrl(TEST_GET_COLLECTION)
                .query("sk", 1)
                .query("l", 1)
                .query("s", new BasicDBObject("variable", 1))
                .toString());
    assertEquals(1, results2.length());
    assertEquals("2", ((JSONObject) results2.get(0)).get("variable"));
  }

  @Test
  public void testPost() throws IOException {
    final JSONObject result =
        client.postJson(
            getCollectionUrl(TEST_POST_COLLECTION).toString(),
            JSON_PARSER.serialize(new BasicDBObject(makeTestMap("POST"))));
    assertNotNull(result);
    assertTestDocumentValid(result);
    // verify posted document really exists
    final JSONObject inserted =
        client.getJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", JSON_PARSER.serialize(new BasicDBObject("_id", result.get("_id"))))
                .query("fo", "true")
                .toString());
    assertNotNull(inserted);
    assertEquals(inserted.toMap(), result.toMap());
  }

  @Test
  public void testPost_many() throws IOException {
    final String variable = client.getName() + "postMany";
    final List<DBObject> docs =
        List.of(
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)));

    final JSONObject result =
        client.postJson(
            getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(docs));
    assertNotNull(result);
    assertEquals(5, result.getInt("n"));
    // verify posted documents exist
    final JSONArray inserted =
        client.getJsonArray(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .toString());
    assertNotNull(inserted);
    assertEquals(5, inserted.length());
    inserted.forEach(o -> assertTestDocumentValid((JSONObject) o));
  }

  @Test
  public void testPut_replaceOne() throws IOException {
    final String variable = client.getName() + "put_replaceOne";
    final DBObject doc = new BasicDBObject(makeTestMap(variable));
    // add document
    final JSONObject put =
        client.postJson(
            getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(doc));
    // modify it
    doc.put("date", new Date(0));
    doc.put("newField", "new");
    // replace it in db
    final JSONObject putResult =
        client.putJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .toString(),
            JSON_PARSER.serialize(doc));
    assertNotNull(putResult);
    assertEquals(1, putResult.getInt("n"));
    // verify that modification really happened
    final JSONObject replaced =
        client.getJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", JSON_PARSER.serialize(new BasicDBObject("_id", put.get("_id"))))
                .query("fo", true)
                .toString());
    assertNotNull(replaced);
    assertNotEquals(put.toMap(), replaced.toMap());
    assertEquals(toISODateString(new Date(0)), ((JSONObject) replaced.get("date")).get("$date"));
    assertEquals("new", replaced.getString("newField"));
  }

  @Test
  public void testPut_replaceManyObjectNotAllowed() throws IOException {
    final String variable = client.getName() + "put_replaceManyObject";
    final List<DBObject> docs =
        List.of(
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)));
    // add documents
    client.postJson(getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(docs));
    // modify one
    final DBObject update = docs.get(0);
    update.put("date", new Date(0));
    update.put("newField", "new");
    // replacing many is not allowed
    try {
      client.putJson(
          getCollectionUrl(TEST_POST_COLLECTION)
              .query("q", new BasicDBObject("variable", variable).toJson())
              .query("m", "true")
              .toString(),
          JSON_PARSER.serialize(update));
      fail("Expected replacing many documents to fail");
    } catch (final ResourceException e) {
      assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatusCode());
    }
  }

  @Test
  public void testPut_updateOne() throws IOException {
    final String variable = client.getName() + "put_updateOne";
    final DBObject doc = new BasicDBObject(makeTestMap(variable));
    // add document
    final JSONObject put =
        client.postJson(
            getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(doc));
    // update it
    final BasicDBObject update =
        new BasicDBObject("$set", new BasicDBObject("date", new Date(0)).append("newField", "new"));
    final JSONObject putResult =
        client.putJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .toString(),
            update.toJson());
    assertNotNull(putResult);
    assertEquals(1, putResult.getInt("n"));
    // verify that modification really happened
    final JSONObject replaced =
        client.getJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", JSON_PARSER.serialize(new BasicDBObject("_id", put.get("_id"))))
                .query("fo", true)
                .toString());
    assertNotNull(replaced);
    assertEquals(toISODateString(new Date(0)), ((JSONObject) replaced.get("date")).get("$date"));
    assertEquals("new", replaced.getString("newField"));
  }

  @Test
  public void testPut_updateMany() throws IOException {
    final String variable = client.getName() + "put_updateMany";
    final List<DBObject> docs =
        List.of(
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)));
    // add documents
    client.postJson(getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(docs));
    // update them all
    final BasicDBObject update =
        new BasicDBObject("$set", new BasicDBObject("date", new Date(0)).append("newField", "new"));
    final JSONObject putResult =
        client.putJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .query("m", "true")
                .toString(),
            JSON_PARSER.serialize(update));
    assertEquals(5, putResult.getInt("n"));
    // verify that all documents were changed
    final JSONArray updated =
        client.getJsonArray(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .toString());
    assertEquals(5, updated.length());
    updated.forEach(
        u -> {
          assertEquals(
              toISODateString(new Date(0)),
              ((JSONObject) ((JSONObject) u).get("date")).get("$date"));
          assertEquals("new", ((JSONObject) u).get("newField"));
        });
  }

  @Test
  public void testPut_upsert() throws IOException {
    final String variable = client.getName() + "put_upsert";
    final BasicDBObject update =
        new BasicDBObject("$set", new BasicDBObject("date", new Date(0)).append("newField", "new"));
    final JSONObject putResult =
        client.putJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .query("u", "true")
                .toString(),
            JSON_PARSER.serialize(update));
    assertEquals(1, putResult.getInt("n"));
    // verify that a new document was created
    final JSONObject upserted =
        client.getJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .query("fo", "true")
                .toString());
    assertNotNull(upserted);
    assertEquals(toISODateString(new Date(0)), ((JSONObject) upserted.get("date")).get("$date"));
    assertEquals("new", upserted.get("newField"));
  }

  @Test
  public void testPut_replaceList() throws IOException {
    final String variable = client.getName() + "put_replaceList";
    final List<DBObject> docs =
        List.of(
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)));
    // add documents
    client.postJson(getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(docs));
    // update them all
    final String replacementVariable = client.getName() + "put_replaceList_replacement";
    final List<DBObject> replacements =
        List.of(
            new BasicDBObject(makeTestMap(replacementVariable)),
            new BasicDBObject(makeTestMap(replacementVariable)),
            new BasicDBObject(makeTestMap(replacementVariable)));
    final JSONObject putResult =
        client.putJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .toString(),
            JSON_PARSER.serialize(replacements));
    assertEquals(3, putResult.getInt("n"));
    assertEquals(5, putResult.getInt("removed"));
    // verify that old documents were removed
    assertNull(
        client.getJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .query("fo", "true")
                .toString()));
    // verify that new documents were added
    final JSONArray replaced =
        client.getJsonArray(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", replacementVariable).toJson())
                .toString());
    assertEquals(3, replaced.length());
  }

  @Test
  public void testPut_remove() throws IOException {
    final String variable = client.getName() + "put_remove";
    final List<DBObject> docs =
        List.of(
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)),
            new BasicDBObject(makeTestMap(variable)));
    // add documents
    client.postJson(getCollectionUrl(TEST_POST_COLLECTION).toString(), JSON_PARSER.serialize(docs));
    // remove them all
    final JSONObject putResult =
        client.putJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .toString(),
            "[]");
    assertEquals(0, putResult.getInt("n"));
    assertEquals(5, putResult.getInt("removed"));
    // verify that old documents were removed
    assertNull(
        client.getJson(
            getCollectionUrl(TEST_POST_COLLECTION)
                .query("q", new BasicDBObject("variable", variable).toJson())
                .query("fo", "true")
                .toString()));
  }

  private ApiPathBuilder getCollectionUrl(final String collection) {
    return ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).db(TEST_DB).collection(collection);
  }
}

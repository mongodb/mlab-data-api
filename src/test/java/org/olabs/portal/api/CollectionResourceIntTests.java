package org.olabs.portal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObjectBuilder;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class CollectionResourceIntTests extends BaseResourceTest {

  private static final String TEST_DB = "test";

  @Test
  public void testGet() throws IOException {
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

    final String testCollection = new ObjectId().toString();
    try {
      // add dummy data
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(testCollection)
          .insertOne(makeTestDocument("1"));
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(testCollection)
          .insertOne(makeTestDocument("2"));
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(testCollection)
          .insertOne(makeTestDocument("3"));

      // query all
      final JSONArray all = client.getJsonArray(getCollectionUrl(testCollection).toString());
      assertEquals(3, all.length());
      assertTrue(
          all.toList().stream()
              .map(o -> ((Map) o).get("variable"))
              .collect(Collectors.toList())
              .containsAll(List.of("1", "2", "3")));

      // query with filter
      final JSONArray filtered =
          client.getJsonArray(
              getCollectionUrl(testCollection).query("q", "{\"variable\": \"2\"}").toString());
      assertEquals(1, filtered.length());
      assertEquals("2", ((JSONObject) filtered.get(0)).get("variable"));

      // query with non-matching filter
      final JSONArray unmatched =
          client.getJsonArray(
              getCollectionUrl(testCollection).query("q", "{\"variable\": \"X\"}").toString());
      assertEquals(0, unmatched.length());

      // query with projection
      final JSONArray projected =
          client.getJsonArray(
              getCollectionUrl(testCollection)
                  .query("f", "{\"date\": 1, \"variable\": 1}")
                  .toString());
      assertEquals(3, projected.length());
      projected.forEach(o -> assertEquals(3, ((JSONObject) o).keySet().size()));

    } finally {
      dropCollection(testCollection);
    }
  }

  private ApiPathBuilder getCollectionUrl(final String collection) {
    return ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).db(TEST_DB).collection(collection);
  }

  private Document makeTestDocument(final String variable) {
    return new Document(
        BasicDBObjectBuilder.start()
            .add("date", new Date(100000000))
            .add("long", 10L)
            .add("int", 100)
            .add("string", "string")
            .add("objectid", new ObjectId())
            .add("boolean", false)
            .add("array", new JSONArray(List.of("a", "b", "c")))
            .add("object", BasicDBObjectBuilder.start().add("a", 1).add("b", 2).add("c", 3).get())
            .add("variable", variable)
            .get()
            .toMap());
  }

  private void dropCollection(final String collection) {
    try {
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(collection)
          .drop();
    } catch (final Exception e) {
      // ignore
    }
  }
}

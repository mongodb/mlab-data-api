package org.olabs.portal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectlabs.mongodb.MongoUtils;

public class CollectionResourceIntTests extends BaseResourceTest {

  private static final Date TEST_DATE = new Date(1000000000);
  private static final String TEST_DB = "test";
  private static final String TEST_COLLECTION = new ObjectId().toString();

  @BeforeClass
  public static void setUpClass() {
    // add dummy data
    final MongoCollection c =
        ApiConfig.getInstance()
            .getClusterConnection(DEDICATED_CLUSTER_ID)
            .getDatabase(TEST_DB)
            .getCollection(TEST_COLLECTION);
    c.insertMany(List.of(makeTestDocument("1"), makeTestDocument("2"), makeTestDocument("3")));
  }

  @AfterClass
  public static void tearDownClass() {
    try {
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(TEST_COLLECTION)
          .drop();
    } catch (final Exception e) {
      // ignore
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
    final JSONArray all = client.getJsonArray(getCollectionUrl(TEST_COLLECTION).toString());
    assertEquals(3, all.length());
    assertTrue(
        all.toList().stream()
            .map(o -> ((Map) o).get("variable"))
            .collect(Collectors.toList())
            .containsAll(List.of("1", "2", "3")));
  }

  @Test
  public void testGet_query() throws IOException {
    // query with filter
    final JSONArray filtered =
        client.getJsonArray(
            getCollectionUrl(TEST_COLLECTION).query("q", "{\"variable\": \"2\"}").toString());
    assertEquals(1, filtered.length());
    assertEquals("2", ((JSONObject) filtered.get(0)).get("variable"));

    // query with non-matching filter
    final JSONArray unmatched =
        client.getJsonArray(
            getCollectionUrl(TEST_COLLECTION).query("q", "{\"variable\": \"X\"}").toString());
    assertEquals(0, unmatched.length());
  }

  @Test
  public void testGet_projection() throws IOException {

    // query with projection
    final JSONArray projected =
        client.getJsonArray(
            getCollectionUrl(TEST_COLLECTION)
                .query("f", "{\"date\": 1, \"variable\": 1}")
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
            getCollectionUrl(TEST_COLLECTION).query("s", "{\"variable\": 1}").toString());
    assertEquals(3, sorted.length());
    assertEquals(
        List.of("1", "2", "3"),
        sorted.toList().stream().map(o -> ((Map) o).get("variable")).collect(Collectors.toList()));
    final JSONArray reversed =
        client.getJsonArray(
            getCollectionUrl(TEST_COLLECTION).query("s", "{\"variable\": -1}").toString());
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
        client.get(getCollectionUrl(TEST_COLLECTION).query("c", "true").toString());
    assertEquals("3", count);
    final String filteredCount =
        client.get(
            getCollectionUrl(TEST_COLLECTION)
                .query("c", "true")
                .query("q", "{\"variable\": \"1\"}")
                .toString());
    assertEquals("1", filteredCount);
  }

  @Test
  public void testGet_findOne() throws IOException {
    // findOne
    final JSONObject one =
        client.getJson(getCollectionUrl(TEST_COLLECTION).query("fo", "true").toString());
    assertNotNull(one);
    assertEquals(10, one.length());
  }

  @Test
  public void testGet_findOne_sorted() throws IOException {
    // this only works correctly in new API
    assumeTrue(isTestClient());
    final JSONObject oneSorted =
        client.getJson(
            getCollectionUrl(TEST_COLLECTION)
                .query("fo", "true")
                .query("s", "{\"variable\": -1}")
                .toString());
    assertNotNull(oneSorted);
    assertEquals("3", oneSorted.get("variable"));
  }

  private ApiPathBuilder getCollectionUrl(final String collection) {
    return ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).db(TEST_DB).collection(collection);
  }

  private static Document makeTestDocument(final String variable) {
    return new Document(
        Map.of(
            "date",
            TEST_DATE,
            "long",
            10L,
            "int",
            100,
            "string",
            "string",
            "objectid",
            new ObjectId(),
            "boolean",
            false,
            "array",
            new JSONArray(List.of("a", "b", "c")),
            "object",
            BasicDBObjectBuilder.start().add("a", 1).add("b", 2).add("c", 3).get(),
            "variable",
            variable));
  }
}

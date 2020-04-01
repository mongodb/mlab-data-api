package com.mlab.api;

import static com.mlab.mongodb.MongoUtils.oid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBRef;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.Binary;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.mlab.mongodb.MongoUtils;

@RunWith(Parameterized.class)
public abstract class BaseResourceTest {

  public static final String DEDICATED_CLUSTER_ID = "rs-ds113926";
  public static final String SHARED_CLUSTER_ID = "rs-ds253357";
  public static final String TEST_CLIENT_NAME = "test";
  public static final String PROD_CLIENT_NAME = "production";
  public static final Date TEST_DATE = new Date(1000000000);
  public static final UUID TEST_UUID = UUID.randomUUID();
  public static final Binary TEST_BINARY = new Binary(new byte[] {1, 2, 3, 4});

  private static final String TEST_CONFIG_ENV_VAR = "MLAB_DATA_API_TEST_CONFIG";
  private static final String TEST_PROD_API_KEY_ENV_VAR = "MLAB_DATA_API_TEST_PROD_KEY";
  private static final String TEST_API_KEY = "PggfZYDE6EWh2FCLsvGm42cq";

  @Parameter public MlabDataApiClient client;

  @Parameters(name = "{0}")
  public static Iterable<? extends Object> data() {
    return List.of(getTestClient(), getProductionClient());
  }

  @Before
  public void setUp() throws Exception {
    System.setProperty(ApiConfig.APP_DIR_PROPERTY, "src/main/webapp");
    Main.start();
  }

  public static MlabDataApiClient getTestClient() {
    final String config = System.getenv(TEST_CONFIG_ENV_VAR);
    if (config == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_CONFIG_ENV_VAR));
    }
    System.setProperty(ApiConfig.CONFIG_PROPERTY, config);
    System.setProperty(ApiConfig.API_KEY_PROPERTY, TEST_API_KEY);
    return new MlabDataApiClient(
        TEST_CLIENT_NAME,
        String.format("http://localhost:%s", ApiConfig.getInstance().getPort()),
        TEST_API_KEY);
  }

  public static MlabDataApiClient getProductionClient() {
    final String prodApiKey = System.getenv(TEST_PROD_API_KEY_ENV_VAR);
    if (prodApiKey == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_PROD_API_KEY_ENV_VAR));
    }
    return new MlabDataApiClient(PROD_CLIENT_NAME, "https://api.mlab.com", prodApiKey);
  }

  public boolean isTestClient() {
    return client.getName().equals(TEST_CLIENT_NAME);
  }

  public boolean isProductionClient() {
    return client.getName().equals(PROD_CLIENT_NAME);
  }

  protected static Map makeTestMap(final String variable) {
    final Map<String, Object> m = new HashMap<>();
    m.put("date", TEST_DATE);
    m.put("int", 100);
    m.put("string", "string");
    m.put("objectid", oid(1000));
    m.put("boolean", false);
    m.put("array", new JSONArray(List.of("a", "b", "c")));
    m.put("object", BasicDBObjectBuilder.start().add("a", 1).add("b", 2).add("c", 3).get());
    m.put("uuid", TEST_UUID);
    m.put("binary", TEST_BINARY);
    m.put("dbref", new DBRef("this", "that"));
    m.put("timestamp", new BsonTimestamp(1000000000, 100));
    m.put("variable", variable);
    return m;
  }

  protected static Document makeTestDocument(final String variable) {
    return new Document(makeTestMap(variable));
  }

  protected void assertTestDocumentValid(final JSONObject o) {
    // date
    assertEquals(MongoUtils.toISODateString(TEST_DATE), ((JSONObject) o.get("date")).get("$date"));
    // int
    assertEquals(100, o.get("int"));
    // string
    assertEquals("string", o.get("string"));
    // ObjectId
    assertTrue(((JSONObject) o.get("objectid")).has("$oid"));
    assertEquals(oid(1000).toHexString(), ((JSONObject) o.get("objectid")).get("$oid"));
    // boolean
    assertEquals(false, o.get("boolean"));
    // array
    assertEquals(List.of("a", "b", "c"), ((JSONArray) o.get("array")).toList());
    // object
    assertEquals(Map.of("a", 1, "b", 2, "c", 3), ((JSONObject) o.get("object")).toMap());
    // uuid
    assertEquals(TEST_UUID.toString(), ((JSONObject) o.get("uuid")).get("$uuid"));
    // binary
    assertEquals("00", ((JSONObject) o.get("binary")).getString("$type"));
    assertEquals("AQIDBA==", ((JSONObject) o.get("binary")).getString("$binary"));
    // dbref
    assertEquals("this", ((JSONObject) o.get("dbref")).get("$ref"));
    assertEquals("that", ((JSONObject) o.get("dbref")).get("$id"));
    // timestamp
    assertEquals(1000000000, ((JSONObject) o.get("timestamp")).get("$ts"));
    assertEquals(100, ((JSONObject) o.get("timestamp")).get("$inc"));
  }
}

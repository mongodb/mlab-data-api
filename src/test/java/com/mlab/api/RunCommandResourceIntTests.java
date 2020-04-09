package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.mlab.ws.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class RunCommandResourceIntTests extends ParameterizedClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(RunCommandResource.class);
  private static final String TEST_DB = "test";
  private static final String TEST_COLLECTION = new ObjectId().toHexString();
  private static final String INVALID_COMMAND = "invalidCommand";
  private static final String DEDICATED_CLUSTER_URL =
      ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).runCommand().toString();
  private static final String DEDICATED_DB_URL =
      ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).db(TEST_DB).runCommand().toString();
  private static final String SHARED_DB_URL =
      ApiPathBuilder.start()
          .cluster(SHARED_CLUSTER_ID)
          .db("aws-us-east-1-shared")
          .runCommand()
          .toString();
  private static final String SANDBOX_URL =
      ApiPathBuilder.start().db("aws-us-east-1-sandbox").runCommand().toString();
  private static final String ATLAS_CLUSTER_URL =
      ApiPathBuilder.start().cluster(ATLAS_CLUSTER_ID).runCommand().toString();

  private static final List<String> ALL_URLS =
      List.of(
          DEDICATED_CLUSTER_URL, DEDICATED_DB_URL, SHARED_DB_URL, SANDBOX_URL, ATLAS_CLUSTER_URL);
  private static final Consumer<JSONObject> ASSERT_SUCCESS =
      result -> {
        assertNotNull(result);
        assertEquals(1d, result.getDouble("ok"), 0d);
      };
  private static final Consumer<JSONObject> ASSERT_FAILURE =
      result -> {
        assertNotNull(result);
        assertEquals(0d, result.getDouble("ok"), 0d);
      };
  @Parameter public MlabDataApiClient client;

  @Parameter(1)
  public String url;

  @Parameter(2)
  public String command;

  @Parameters(name = "{2}:{1}:{0}")
  public static Collection<Object[]> data() {
    final Collection<Object[]> data = new ArrayList<>();
    ALL_URLS.forEach(
        url -> {
          RunCommandResource.SUPPORTED_COMMANDS.forEach(
              cmd -> {
                data.add(new Object[] {getTestClient(), url, cmd});
                data.add(new Object[] {getProductionClient(), url, cmd});
              });
          RunCommandResource.SUPPORTED_ADMIN_COMMANDS.forEach(
              cmd -> {
                data.add(new Object[] {getTestClient(), url, cmd});
                data.add(new Object[] {getProductionClient(), url, cmd});
              });
          data.add(new Object[] {getTestClient(), url, INVALID_COMMAND});
          data.add(new Object[] {getProductionClient(), url, INVALID_COMMAND});
        });
    return data;
  }

  @BeforeClass
  public static void beforeClass() {
    seedCollection(
        ApiConfig.getInstance()
            .getClusterConnection(DEDICATED_CLUSTER_ID)
            .getDatabase(TEST_DB)
            .getCollection(TEST_COLLECTION));
    seedCollection(
        ApiConfig.getInstance()
            .getClusterConnection(ATLAS_CLUSTER_ID)
            .getDatabase(TEST_DB)
            .getCollection(TEST_COLLECTION));
  }

  private static void seedCollection(final MongoCollection c) {
    // add geospatial index
    c.createIndex(new BasicDBObject("loc", "2dsphere"));
    final List<Document> docs = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      final Document d = makeTestDocument(String.valueOf(i));
      // add random geospatial coords
      d.put("loc", List.of(Math.random() * 360 - 180, Math.random() * 180 - 90));
      docs.add(d);
    }
    c.insertMany(docs);
  }

  @AfterClass
  public static void tearDownClass() {
    try {
      ApiConfig.getInstance()
          .getClusterConnection(DEDICATED_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(TEST_COLLECTION)
          .drop();
      ApiConfig.getInstance()
          .getClusterConnection(ATLAS_CLUSTER_ID)
          .getDatabase(TEST_DB)
          .getCollection(TEST_COLLECTION)
          .drop();
    } catch (final Exception e) {
      LOG.error("Unable to clean up test collections", e);
    }
  }

  @Test
  public void testPost() throws IOException {
    // only test admin commands on dedicated cluster
    assumeTrue(
        RunCommandResource.SUPPORTED_COMMANDS.contains(command)
            || url.contains(DEDICATED_CLUSTER_ID));
    // don't test admin commands against database endpoints
    if (RunCommandResource.SUPPORTED_ADMIN_COMMANDS.contains(command)) {
      assumeFalse(url.contains("databases"));
    }
    // only test Atlas URL on test client
    if (url.contains(ATLAS_CLUSTER_ID)) {
      assumeTrue(client.getName().equals(TEST_CLIENT_NAME));
    }

    final BasicDBObject commandObj = getCommandObject();
    assumeNotNull(commandObj);

    try {
      final JSONObject result = client.postJson(url, commandObj.toJson());
      getAssertions().accept(result);
    } catch (final ResourceException e) {
      handleError(e);
    }
  }

  private BasicDBObject getCommandObject() {
    switch (command) {
      case "profile":
        assumeTrue(!url.contains("databases"));
        return new BasicDBObject(command, -1);
      case "collStats":
        return new BasicDBObject(command, TEST_COLLECTION);
      case "aggregate":
        return new BasicDBObject(command, TEST_COLLECTION)
            .append("cursor", new BasicDBObject())
            .append(
                "pipeline",
                List.of(new BasicDBObject("$match", new BasicDBObject("variable", "3"))));
      case "distinct":
        return new BasicDBObject(command, TEST_COLLECTION).append("key", "variable");
      case "findAndModify":
        assumeTrue(url.equals(DEDICATED_DB_URL));
        return new BasicDBObject(command, TEST_COLLECTION)
            .append("query", new BasicDBObject("variable", "5"))
            .append("update", new BasicDBObject("$set", new BasicDBObject("newField", "new")))
            .append("new", true);
      case "geoNear":
        assumeTrue(url.equals(DEDICATED_DB_URL));
        return new BasicDBObject(command, TEST_COLLECTION)
            .append("near", List.of(10, 20))
            .append("limit", 2)
            .append("spherical", true);
      case "convertToCapped":
      case "reIndex":
      case "replSetStepDown":
      case "replSetFreeze":
        return null;
      case "repairDatabase":
        assumeTrue(url.equals(DEDICATED_DB_URL));
        assumeTrue(client.getName().equals(TEST_CLIENT_NAME));
      default:
        return new BasicDBObject(command, 1);
    }
  }

  private Consumer<JSONObject> getAssertions() {
    switch (command) {
      case "collStats":
        return result -> {
          if (url.equals(DEDICATED_DB_URL)) {
            ASSERT_SUCCESS.accept(result);
          } else {
            ASSERT_FAILURE.accept(result);
            assertTrue(result.getString("errmsg").contains(TEST_COLLECTION));
            assertTrue(result.getString("errmsg").contains("not found"));
          }
        };
      case "aggregate":
        return result -> {
          ASSERT_SUCCESS.accept(result);
          assertTrue(result.has("cursor"));
          if (url.equals(DEDICATED_DB_URL)) {
            assertEquals(1, result.getJSONObject("cursor").getJSONArray("firstBatch").length());
          } else {
            assertTrue(result.getJSONObject("cursor").getJSONArray("firstBatch").isEmpty());
          }
        };
      case "distinct":
        return result -> {
          ASSERT_SUCCESS.accept(result);
          if (url.equals(DEDICATED_DB_URL)) {
            assertEquals(
                List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
                result.getJSONArray("values").toList());
          } else {
            assertTrue(result.getJSONArray("values").isEmpty());
          }
        };
      case "geoNear":
        return result -> {
          ASSERT_SUCCESS.accept(result);
          assertEquals(2, result.getJSONArray("results").length());
        };
      case "findAndModify":
        return result -> {
          ASSERT_SUCCESS.accept(result);
          assertEquals("new", result.getJSONObject("value").getString("newField"));
        };
      case INVALID_COMMAND:
        return result -> fail("Expected invalid command to fail");
      default:
        return ASSERT_SUCCESS;
    }
  }

  private void handleError(final ResourceException e) {
    if (command.equals(INVALID_COMMAND)) {
      assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatusCode());
      assertTrue(e.getMessage().contains("invalid or unsupported"));
    } else {
      fail(String.format("%s failed for %s: %s", command, url, e.getMessage()));
    }
  }
}

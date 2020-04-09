package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

public class AtlasConnectionIntTests extends BaseResourceTest {

  private static final MlabDataApiClient CLIENT = ParameterizedClientTest.getTestClient();
  private static final String TEST_DB = new ObjectId().toHexString();

  @After
  public void cleanUp() {
    try {
      ApiConfig.getInstance().getClusterConnection(ATLAS_CLUSTER_ID).getDatabase(TEST_DB).drop();
    } catch (final Exception e) {
      // ignore
    }
  }

  @Test
  public void testListDatabases() throws IOException {
    final JSONArray dbs =
        CLIENT.getJsonArray(
            ApiPathBuilder.start().cluster(ATLAS_CLUSTER_ID).databases().toString());
    assertNotNull(dbs);
    assertTrue(dbs.toList().contains("test"));
    assertTrue(dbs.toList().contains("admin"));
    assertTrue(dbs.toList().contains("local"));
  }

  @Test
  public void testCreateDatabase() throws IOException {
    final JSONArray collections =
        CLIENT.getJsonArray(
            ApiPathBuilder.start().cluster(ATLAS_CLUSTER_ID).db(TEST_DB).collections().toString());
    assertTrue(collections.isEmpty());
  }

  @Test
  public void testCreateDocument() throws IOException {
    final String collectionName = "c1";
    final JSONArray docs =
        CLIENT.getJsonArray(
            ApiPathBuilder.start()
                .cluster(ATLAS_CLUSTER_ID)
                .db(TEST_DB)
                .collection(collectionName)
                .toString());
    assertTrue(docs.isEmpty());
    final JSONObject doc =
        CLIENT.postJson(
            ApiPathBuilder.start()
                .cluster(ATLAS_CLUSTER_ID)
                .db(TEST_DB)
                .collection(collectionName)
                .toString(),
            new BasicDBObject(makeTestMap(collectionName)));
    assertNotNull(doc);
    assertTestDocumentValid(doc);
    final JSONArray newDocs =
        CLIENT.getJsonArray(
            ApiPathBuilder.start()
                .cluster(ATLAS_CLUSTER_ID)
                .db(TEST_DB)
                .collection(collectionName)
                .toString());
    assertFalse(newDocs.isEmpty());
    assertEquals(1, newDocs.length());

    final JSONObject created =
        CLIENT.getJson(
            ApiPathBuilder.start()
                .cluster(ATLAS_CLUSTER_ID)
                .db(TEST_DB)
                .collection(collectionName)
                .object(((JSONObject) doc.get("_id")).get("$oid").toString())
                .toString());
    assertTestDocumentValid(created);
    assertEquals(doc.get("_id").toString(), created.get("_id").toString());
  }
}

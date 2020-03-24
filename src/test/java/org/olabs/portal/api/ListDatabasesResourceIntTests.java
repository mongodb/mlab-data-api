package org.olabs.portal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ListDatabasesResourceIntTests extends BaseResourceTest {

  @Test
  public void testGet_dedicated() throws IOException {
    final JSONArray dbs =
        client.getJsonArray(
            ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).listDatabases().toString());
    assertNotNull(dbs);
    assertEquals(4, dbs.length());
    dbs.forEach(
        o -> {
          final JSONObject db = (JSONObject) o;
          assertNotNull(db.getString("name"));
          assertTrue(db.getDouble("sizeOnDisk") > 0);
          assertFalse(db.getBoolean("empty"));
        });
  }

  @Test
  public void testGet_shared() throws IOException {
    final JSONArray dbs =
        client.getJsonArray(
            ApiPathBuilder.start().cluster(SHARED_CLUSTER_ID).listDatabases().toString());
    assertNotNull(dbs);
    // Production API allows access to local and system dbs
    assertEquals(isProductionClient() ? 3 : 1, dbs.length());
    assertTrue(
        dbs.toList().stream()
            .anyMatch(db -> ((Map) db).get("name").equals("aws-us-east-1-shared")));
  }
}

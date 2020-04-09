package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.junit.Test;

public class MongoDBConnectionResourceIntTests extends ParameterizedClientTest {

  private static Map<String, List<String>> TEST_CASES =
      Map.of(
          "", List.of("admin", "config", "local", "test"),
          "?systemOnly=true", List.of("admin", "config", "local"),
          "?userOnly=true", List.of("test"));

  @Test
  public void testClusterGet() {
    TEST_CASES.forEach(
        (urlParams, expectedDbs) -> {
          try {
            final JSONArray dbs =
                client.getJsonArray(
                    String.format("clusters/%s/databases%s", DEDICATED_CLUSTER_ID, urlParams));
            assertNotNull(dbs);
            assertEquals(expectedDbs.size(), dbs.length());
            assertTrue(expectedDbs.containsAll(dbs.toList()));
          } catch (final IOException e) {
            fail(e.getMessage());
          }
        });
  }
}

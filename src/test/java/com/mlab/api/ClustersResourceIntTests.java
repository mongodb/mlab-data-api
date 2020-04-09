package com.mlab.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.json.JSONArray;
import org.junit.Test;

public class ClustersResourceIntTests extends ParameterizedClientTest {

  @Test
  public void testGet() throws IOException {
    final JSONArray clusters = client.getJsonArray("clusters");
    assertNotNull(clusters);
    assertTrue(clusters.toList().contains(DEDICATED_CLUSTER_ID));
    assertTrue(clusters.toList().contains(SHARED_CLUSTER_ID));
  }
}

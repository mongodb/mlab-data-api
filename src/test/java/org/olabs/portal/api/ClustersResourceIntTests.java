package org.olabs.portal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.json.JSONArray;
import org.junit.Test;

public class ClustersResourceIntTests extends BaseResourceTest {

  @Test
  public void testGet() throws IOException {
    final JSONArray clusters = getTestClient().getJsonArray("clusters");
    assertNotNull(clusters);
    assertEquals(2, clusters.length());
    assertTrue(clusters.toList().contains("rs-ds113926"));
    assertTrue(clusters.toList().contains("rs-ds253357"));

    final JSONArray prodClusters = getProductionClient().getJsonArray("clusters");
    assertNotNull(prodClusters);
    assertTrue(prodClusters.toList().contains("rs-ds113926"));
    assertTrue(prodClusters.toList().contains("rs-ds253357"));
  }
}

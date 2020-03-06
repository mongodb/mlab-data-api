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
    final JSONArray clusters = doJsonArrayGet("clusters");
    assertNotNull(clusters);
    assertEquals(2, clusters.length());
    assertTrue(clusters.toList().contains("rs-ds253817"));
    assertTrue(clusters.toList().contains("rs-ds253357"));
  }
}

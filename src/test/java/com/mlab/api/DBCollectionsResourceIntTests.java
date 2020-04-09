package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.json.JSONArray;
import org.junit.Test;

public class DBCollectionsResourceIntTests extends ParameterizedClientTest {

  @Test
  public void testGet_test() throws IOException {
    final JSONArray collections =
        client.getJsonArray(
            String.format("clusters/%s/databases/test/collections", DEDICATED_CLUSTER_ID));
    assertNotNull(collections);
    assertTrue(collections.toList().contains("c1"));
    assertTrue(collections.toList().contains("c2"));
    assertTrue(collections.toList().contains("c3"));
    assertTrue(collections.toList().contains("view1"));
    assertTrue(collections.toList().contains("objectlabs-system"));
  }

  @Test
  public void testGet_test_views() throws IOException {
    final JSONArray collections =
        client.getJsonArray(
            String.format("clusters/%s/databases/test/collections?type=view", DEDICATED_CLUSTER_ID));
    assertNotNull(collections);
    assertEquals(1, collections.length());
    assertTrue(collections.toList().contains("view1"));
    assertFalse(collections.toList().contains("c1"));
    assertFalse(collections.toList().contains("c2"));
    assertFalse(collections.toList().contains("c3"));
  }

  @Test
  public void testGet_test_collections() throws IOException {
    final JSONArray collections =
        client.getJsonArray(
            String.format("clusters/%s/databases/test/collections?type=collection", DEDICATED_CLUSTER_ID));
    assertNotNull(collections);
    assertTrue(collections.toList().contains("c1"));
    assertTrue(collections.toList().contains("c2"));
    assertTrue(collections.toList().contains("c3"));
    assertFalse(collections.toList().contains("view1"));
  }

  @Test
  public void testGet_admin() throws IOException {
    final JSONArray collections =
        client.getJsonArray(
            String.format("clusters/%s/databases/admin/collections", DEDICATED_CLUSTER_ID));
    assertNotNull(collections);
    assertFalse(collections.toList().contains("system.users"));
  }
}

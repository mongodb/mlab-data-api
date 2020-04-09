package com.mlab.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.json.JSONArray;
import org.junit.Test;

public class DatabasesResourceIntTests extends ParameterizedClientTest {
  @Test
  public void testGet() throws IOException {
    final JSONArray databases = client.getJsonArray("databases");
    assertNotNull(databases);
    assertTrue(databases.toList().contains("aws-us-east-1-sandbox"));
    assertTrue(databases.toList().contains("azr-north-europe-sandbox"));
  }
}

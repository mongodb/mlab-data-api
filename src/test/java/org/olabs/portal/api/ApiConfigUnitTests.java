package org.olabs.portal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.mongodb.MongoClientURI;
import org.junit.Test;
import org.objectlabs.ws.ResourceException;

public class ApiConfigUnitTests {

  @Test
  public void testGetInstance_empty() {
    try {
      ApiConfig.getInstance(null);
      fail("Expected getInstance() to fail with empty config");
    } catch(final ResourceException e) {
      assertTrue(e.getMessage().contains("MLAB_DATA_API_CONFIG is missing"));
    }
  }

  @Test
  public void testGetInstance_json() {
    final String raw = "{port: 1234}";
    final ApiConfig config = ApiConfig.getInstance(raw);
    assertNotNull(config);
    assertEquals(1234, config.getPort());
  }

  @Test
  public void testGetInstance_file() {
    final String file = "src/test/fixtures/test-config.yaml";
    final ApiConfig config = ApiConfig.getInstance(file);
    assertNotNull(config);
    assertEquals(5678, config.getPort());
    assertEquals(1, config.getClusters().size());
    final MongoClientURI uri = config.getClusterUri("foo");
    assertNotNull(uri);
    assertEquals("user", uri.getUsername());
    assertEquals("admin", uri.getDatabase());
    assertEquals("localhost:27001", uri.getHosts().get(0));
  }
}

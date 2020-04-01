package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.mongodb.MongoClientURI;
import org.junit.Test;
import com.mlab.ws.ResourceException;

public class ApiConfigUnitTests {

  @Test
  public void testGetInstance_empty() {
    try {
      ApiConfig.getInstance(null);
      fail("Expected getInstance() to fail with empty config");
    } catch(final ResourceException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testGetInstance_json() {
    final ApiConfig config = TestUtils.getTestApiConfig();
    assertNotNull(config);
    assertEquals(1234, config.getPort());
    assertEquals(2, config.getClusters().size());
    final MongoClientURI uriA = config.getClusterUri("a");
    assertNotNull(uriA);
    assertEquals("user", uriA.getUsername());
    assertEquals("host-a:27001", uriA.getHosts().get(0));
    assertEquals("admin", uriA.getDatabase());
    final MongoClientURI uriB = config.getClusterUri("b");
    assertNotNull(uriB);
    assertEquals("user", uriB.getUsername());
    assertEquals("host-b:27001", uriB.getHosts().get(0));
    assertEquals("admin", uriB.getDatabase());
    final MongoClientURI uriFoo = config.getDatabaseUri("foo");
    assertNotNull(uriFoo);
    assertEquals("user", uriFoo.getUsername());
    assertEquals("host-c:27001", uriFoo.getHosts().get(0));
    assertEquals("foo", uriFoo.getDatabase());
    final MongoClientURI uriBar = config.getDatabaseUri("bar");
    assertNotNull(uriBar);
    assertEquals("user", uriBar.getUsername());
    assertEquals("host-d:27001", uriBar.getHosts().get(0));
    assertEquals("bar", uriBar.getDatabase());
  }

  @Test
  public void testGetInstance_file() {
    final String file = "src/test/fixtures/test-config.yaml";
    final ApiConfig config = ApiConfig.getInstance(file);
    assertNotNull(config);
    assertEquals(5678, config.getPort());
    assertEquals(2, config.getClusters().size());
    final MongoClientURI uriA = config.getClusterUri("a");
    assertNotNull(uriA);
    assertEquals("user", uriA.getUsername());
    assertEquals("admin", uriA.getDatabase());
    assertEquals("host-a:27001", uriA.getHosts().get(0));
    final MongoClientURI uriB = config.getClusterUri("b");
    assertNotNull(uriB);
    assertEquals("user", uriB.getUsername());
    assertEquals("admin", uriB.getDatabase());
    assertEquals(2, uriB.getHosts().size());
    assertEquals("host-b0:27001", uriB.getHosts().get(0));
    assertEquals("host-b1:27001", uriB.getHosts().get(1));
    final MongoClientURI uriFoo = config.getDatabaseUri("foo");
    assertNotNull(uriFoo);
    assertEquals("user", uriFoo.getUsername());
    assertEquals("host-c:27001", uriFoo.getHosts().get(0));
    assertEquals("foo", uriFoo.getDatabase());
    final MongoClientURI uriBar = config.getDatabaseUri("bar");
    assertNotNull(uriBar);
    assertEquals("user", uriBar.getUsername());
    assertEquals("host-d:27001", uriBar.getHosts().get(0));
    assertEquals("bar", uriBar.getDatabase());
  }
}

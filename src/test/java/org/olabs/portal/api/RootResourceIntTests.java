package org.olabs.portal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.objectlabs.ws.ResourceException;

public class RootResourceIntTests extends BaseResourceTest {

  @Test
  public void testGet() throws IOException {
    try {
       doGet("");
       fail("Expected GET / to fail");
    } catch(final ResourceException e) {
      assertEquals(HttpStatus.SC_NOT_FOUND, e.getStatusCode());
    }
  }
}

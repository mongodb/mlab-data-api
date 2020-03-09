package org.olabs.portal.api;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class DatabaseResourceIntTests extends BaseResourceTest {

  @Test
  public void testGet() throws IOException {
    final List<String> expectedChildren = List.of("collections", "commands", "runCommand");
    assertTrue(
        client
            .getJsonArray("clusters/rs-ds113926/databases/test")
            .toList()
            .containsAll(expectedChildren));
  }
}

package com.mlab.api;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class DatabaseResourceIntTests extends ParameterizedClientTest {

  @Test
  public void testGet() throws IOException {
    final List<String> expectedChildren = List.of("collections", "commands", "runCommand");
    assertTrue(
        client
            .getJsonArray(String.format("clusters/%s/databases/test", DEDICATED_CLUSTER_ID))
            .toList()
            .containsAll(expectedChildren));
  }
}

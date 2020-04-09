package com.mlab.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;

public class CommandsResourceIntTests extends ParameterizedClientTest {

  @Test
  public void testGet() throws IOException {
    final JSONObject result =
        client.getJson(
            ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).db("test").commands().toString());
    assertNotNull(result);
    assertTrue(result.has("commands"));
    final JSONObject commands = (JSONObject) result.get("commands");
    assertTrue(commands.has("aggregate"));
    assertTrue(commands.has("applyOps"));
    assertTrue(commands.has("collStats"));
  }
}

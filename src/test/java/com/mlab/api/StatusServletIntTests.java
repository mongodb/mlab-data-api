package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

public class StatusServletIntTests extends ParameterizedClientTest {

  @Test
  public void testStatus() throws IOException {
    assumeTrue(isTestClient());
    final HttpResponse response = client.doRequest(new HttpGet(client.getHost()));
    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }
}

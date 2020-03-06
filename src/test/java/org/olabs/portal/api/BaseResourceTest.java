package org.olabs.portal.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.objectlabs.ws.ResourceException;

public abstract class BaseResourceTest {
  private static final String TEST_CONFIG_ENV_VAR = "MLAB_DATA_API_TEST_CONFIG";
  private static final String TEST_API_KEY = "PggfZYDE6EWh2FCLsvGm42cq";
  private CloseableHttpClient _client = HttpClients.createMinimal();

  @Before
  public void setUp() throws Exception {
    System.setProperty(ApiConfig.APP_DIR_PROPERTY, "src/main/webapp");
    final String config = System.getenv(TEST_CONFIG_ENV_VAR);
    if (config == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_CONFIG_ENV_VAR));
    }
    System.setProperty(ApiConfig.CONFIG_PROPERTY, config);
    System.setProperty(ApiConfig.API_KEY_PROPERTY, TEST_API_KEY);
    Main.start();
  }

  public String doGet(final String pPath) throws IOException {
    return doRequest(new HttpGet(getPathUrl(pPath)));
  }

  public String doPost(final String pPath, final JSONObject pData) throws IOException {
    final HttpPost post = new HttpPost(getPathUrl(pPath));
    post.setEntity(new StringEntity(pData.toString(), ContentType.APPLICATION_JSON));
    return doRequest(post);
  }

  public String doRequest(final HttpUriRequest pRequest) throws IOException {
    final CloseableHttpResponse response = _client.execute(pRequest);
    final int status = response.getStatusLine().getStatusCode();
    if (status == HttpStatus.SC_OK) {
      return EntityUtils.toString(response.getEntity());
    } else {
      throw new ResourceException(status);
    }
  }

  public JSONObject doJsonGet(final String pPath) throws IOException {
    return new JSONObject(doGet(pPath));
  }

  public JSONArray doJsonArrayGet(final String pPath) throws IOException {
    return new JSONArray(doGet(pPath));
  }

  private String getPathUrl(final String pPath) throws MalformedURLException {
    final URL url =
        new URL(
            String.format(
                "http://localhost:%s/api/1/%s", ApiConfig.getInstance().getPort(), pPath));
    final String delim = url.getQuery() == null || url.getQuery().isEmpty() ? "?" : "&";
    return String.format("%s%sapiKey=%s", url.toString(), delim, TEST_API_KEY);
  }
}

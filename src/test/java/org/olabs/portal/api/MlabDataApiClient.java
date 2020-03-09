package org.olabs.portal.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.objectlabs.ws.ResourceException;

public class MlabDataApiClient {
  private final String _name;
  private final String _host;
  private final CloseableHttpClient _client;
  private final String _apiKey;

  public MlabDataApiClient(final String pName, final String pHost, final String pApiKey) {
    _name = pName;
    _client = HttpClients.createMinimal();
    _host = pHost;
    _apiKey = pApiKey;
  }

  public String getName() {
    return _name;
  }

  public String get(final String pPath) throws IOException {
    return doRequest(new HttpGet(getPathUrl(pPath)));
  }

  public String post(final String pPath, final JSONObject pData) throws IOException {
    final HttpPost post = new HttpPost(getPathUrl(pPath));
    post.setEntity(new StringEntity(pData.toString(), ContentType.APPLICATION_JSON));
    return doRequest(post);
  }

  public JSONObject getJson(final String pPath) throws IOException {
    return new JSONObject(get(pPath));
  }

  public JSONArray getJsonArray(final String pPath) throws IOException {
    return new JSONArray(get(pPath));
  }

  public String toString() {
    return getName();
  }

  private String getPathUrl(final String pPath) throws MalformedURLException, UnsupportedEncodingException {
    final URL url = new URL(String.format("%s/api/1/%s", _host, pPath));
    final String delim = url.getQuery() == null || url.getQuery().isEmpty() ? "?" : "&";
    return String.format(
        "%s%sapiKey=%s", url.toString(), delim, URLEncoder.encode(_apiKey, "UTF-8"));
  }

  private String doRequest(final HttpUriRequest pRequest) throws IOException {
    final CloseableHttpResponse response = _client.execute(pRequest);
    final int status = response.getStatusLine().getStatusCode();
    if (status == HttpStatus.SC_OK) {
      return EntityUtils.toString(response.getEntity());
    } else {
      throw new ResourceException(status);
    }
  }
}

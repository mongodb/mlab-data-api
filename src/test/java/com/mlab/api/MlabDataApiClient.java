package com.mlab.api;

import com.mlab.json.JsonParser;
import com.mongodb.DBObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mlab.ws.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MlabDataApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(MlabDataApiClient.class);
  private static final JsonParser JSON_PARSER = new JsonParser();

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

  public String getHost() {
    return _host;
  }

  public String get(final String pPath) throws IOException {
    return doRequestString(new HttpGet(getPathUrl(pPath)));
  }

  public JSONObject getJson(final String pPath) throws IOException {
    final String result = get(pPath);
    return result == null ? null : new JSONObject(result);
  }

  public JSONArray getJsonArray(final String pPath) throws IOException {
    final String result = get(pPath);
    return result == null ? null : new JSONArray(result);
  }

  public String post(final String pPath, final String pData) throws IOException {
    final HttpPost post = new HttpPost(getPathUrl(pPath));
    post.setEntity(new StringEntity(pData, ContentType.APPLICATION_JSON));
    return doRequestString(post);
  }

  public JSONObject postJson(final String pPath, final DBObject pData) throws IOException {
    final String result = post(pPath, JSON_PARSER.serialize(pData));
    return result == null ? null : new JSONObject(result);
  }

  public JSONObject postJson(final String pPath, final String pData) throws IOException {
    final String result = post(pPath, pData);
    return result == null ? null : new JSONObject(result);
  }

  public String put(final String pPath, final String pData) throws IOException {
    final HttpPut put = new HttpPut(getPathUrl(pPath));
    put.setEntity(new StringEntity(pData, ContentType.APPLICATION_JSON));
    return doRequestString(put);
  }

  public JSONObject putJson(final String pPath, final String pData) throws IOException {
    final String result = put(pPath, pData);
    return result == null ? null : new JSONObject(result);
  }

  public String delete(final String pPath) throws IOException {
    return doRequestString(new HttpDelete(getPathUrl(pPath)));
  }

  public JSONObject deleteJson(final String pPath) throws IOException {
    final String result = delete(pPath);
    return result == null ? null : new JSONObject(result);
  }

  public String toString() {
    return getName();
  }

  private String getPathUrl(final String pPath)
      throws MalformedURLException, UnsupportedEncodingException {
    final URL url = new URL(String.format("%s/api/1/%s", getHost(), pPath));
    final String delim = url.getQuery() == null || url.getQuery().isEmpty() ? "?" : "&";
    return String.format(
        "%s%sapiKey=%s", url.toString(), delim, URLEncoder.encode(_apiKey, "UTF-8"));
  }

  public String doRequestString(final HttpUriRequest pRequest) throws IOException {
    final HttpResponse response = doRequest(pRequest);
    final int status = response.getStatusLine().getStatusCode();
    if (status == HttpStatus.SC_OK) {
      final String s = EntityUtils.toString(response.getEntity());
      if (s == null) {
        return null;
      }
      final String trimmed = s.trim();
      if (trimmed.equals("null")) {
        return null;
      }
      return trimmed;
    } else {
      final String msg = EntityUtils.toString(response.getEntity());
      throw new ResourceException(status, msg);
    }
  }

  public HttpResponse doRequest(final HttpUriRequest pRequest) throws IOException {
    return _client.execute(pRequest);
  }
}

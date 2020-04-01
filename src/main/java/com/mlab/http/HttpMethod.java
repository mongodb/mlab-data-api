package com.mlab.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

public enum HttpMethod {
  GET(HttpGet.class),
  POST(HttpPost.class),
  PUT(HttpPut.class),
  DELETE(HttpDelete.class),
  CREATE(null),
  PATCH(HttpPatch.class),
  HEAD(HttpHead.class),
  OPTIONS(HttpOptions.class);

  private Class<? extends HttpUriRequest> mRequestClass;

  HttpMethod(final Class<? extends HttpUriRequest> requestClass) {
    setRequestClass(requestClass);
  }

  public Class<? extends HttpUriRequest> getRequestClass() {
    return mRequestClass;
  }

  public void setRequestClass(final Class<? extends HttpUriRequest> requestClass) {
    mRequestClass = requestClass;
  }
}

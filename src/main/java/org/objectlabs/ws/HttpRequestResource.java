package org.objectlabs.ws;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestResource extends DirectoryResource {

  private HttpServletRequest request;

  public HttpRequestResource() {
    super();
  }

  public HttpRequestResource(HttpServletRequest request) {
    super();
    setRequest(request);
  }

  public HttpServletRequest getRequest() {
    return (request);
  }

  public void setRequest(HttpServletRequest value) {
    request = value;
  }
}

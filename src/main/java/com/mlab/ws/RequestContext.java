package com.mlab.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

  private HttpServletRequest servletRequest;
  private HttpServletResponse servletResponse;

  public RequestContext() {
    super();
  }

  public HttpServletRequest getServletRequest() {
    return servletRequest;
  }

  public void setServletRequest(final HttpServletRequest value) {
    servletRequest = value;
  }

  public HttpServletResponse getServletResponse() {
    return servletResponse;
  }

  public void setServletResponse(final HttpServletResponse value) {
    servletResponse = value;
  }
}

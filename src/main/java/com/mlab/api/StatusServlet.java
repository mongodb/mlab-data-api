package com.mlab.api;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatusServlet extends HttpServlet {

  @Override
  public void service(final HttpServletRequest req, final HttpServletResponse res) {
    res.setStatus(HttpServletResponse.SC_OK);
  }
}

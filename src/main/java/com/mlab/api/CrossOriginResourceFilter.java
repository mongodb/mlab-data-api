package com.mlab.api;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CrossOriginResourceFilter implements Filter {

  public static final String ACCESS_CONTROL_MAX_AGE = "1728000";

  @Override
  public void init(final FilterConfig config) throws ServletException {}

  @Override
  public void destroy() {}

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    response.setHeader("Access-Control-Allow-Credentials", "true");

    final String origin = request.getHeader("Origin");
    response.setHeader(
        "Access-Control-Allow-Origin", origin == null || origin.equals("null") ? "*" : origin);
    if ("OPTIONS".equals(request.getMethod())) {
      response.setHeader(
          "Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
      response.setHeader("Access-Control-Max-Age", ACCESS_CONTROL_MAX_AGE);
    }
    chain.doFilter(request, response);
  }
}

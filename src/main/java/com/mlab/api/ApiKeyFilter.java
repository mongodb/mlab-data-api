package com.mlab.api;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ApiKeyFilter implements Filter {
  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(
      final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    final String apiKey = ApiConfig.getApiKey();
    final String requestKey = request.getParameter("apiKey");

    if (apiKey == null || !apiKey.equals(requestKey)) {
      ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}
}

package com.mlab.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import com.mlab.MlabException;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(ApiServlet.class);

  private static String generateRedactedRequestInfo(final HttpServletRequest request) {
    // Redact secrets from the following parameters according to the need.
    String queryString = request.getQueryString(); // This is being redacted
    if (queryString != null) {
      final List<NameValuePair> params =
          redactParamValues(URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8), "apiKey");
      queryString = URLEncodedUtils.format(params, StandardCharsets.UTF_8);
    }

    final String requestURI = request.getRequestURI(); // Not redacted
    final String requestServletPath = request.getServletPath(); // Not redacted
    final String requestPathInfo = request.getPathInfo(); // Not redacted

    return new StringBuilder()
        .append(request.getMethod())
        .append(" ")
        .append(requestURI)
        .append(queryString == null || queryString.isEmpty() ? "" : "?" + queryString)
        .append(" (_method = ")
        .append(request.getParameter("_method"))
        .append(", servletPath = ")
        .append(requestServletPath)
        .append(", pathInfo = ")
        .append(requestPathInfo)
        .append(")")
        .toString();
  }

  private static List<NameValuePair> redactParamValues(
      final List<NameValuePair> params, final String... paramNamesToRedact) {
    final List paramNamesToRedactList = Arrays.asList(paramNamesToRedact);
    for (int i = 0; i < params.size(); i++) {
      final NameValuePair p = params.get(i);
      final String paramName = p.getName();
      if (paramNamesToRedactList.contains(paramName)) {
        params.set(i, new BasicNameValuePair(paramName, "REDACTED"));
      }
    }
    return params;
  }

  private static DBObject errorToDBObject(final Throwable t, final boolean wantingDetails) {
    final DBObject error = new BasicDBObject("message", t.getMessage());
    if (wantingDetails) {
      Throwable cause = t;
      while (cause.getCause() != null) cause = cause.getCause();
      final List<String> stackTrace = new LinkedList<>();
      for (final StackTraceElement element : cause.getStackTrace()) {
        stackTrace.add(element.toString());
      }
      error.put("class", t.getClass().getName());
      error.put("stackTrace", stackTrace);
    }
    return error;
  }

  @Override
  public void service(final HttpServletRequest req, final HttpServletResponse res) {
    try {
      final String path = req.getPathInfo();
      final Resource resource = new RootResource().resolve(path);
      if (resource == null) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
        resource.service(req, res);
      }
    } catch (final Exception e) {
      final String requestInfo = generateRedactedRequestInfo(req);
      final boolean isMerelyResourceException = e instanceof ResourceException;
      final DBObject error = errorToDBObject(e, !isMerelyResourceException);
      if (isMerelyResourceException) {
        final ResourceException re = (ResourceException) e;
        res.setStatus(re.getStatusCode());
        if (re.isSevere()) {
          LOG.error(
              String.format(
                  "Exception handling request, returning status code %d: %s",
                  re.getStatusCode(), requestInfo),
              e);
        } else if (LOG.isDebugEnabled()) {
          LOG.debug(
              String.format(
                  "Exception handling request, returning status code %d: %s: %s",
                  re.getStatusCode(), requestInfo, JSON.serialize(error)));
        }
      } else {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        LOG.error("Error handling request: " + requestInfo, e);
      }
      res.setContentType("application/json; charset=utf-8");
      try {
        res.getWriter().println(JSON.serialize(error));
      } catch (final IOException ioe) {
        throw new MlabException(ioe);
      }
    }
  }
}

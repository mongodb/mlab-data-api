package com.mlab.api;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import com.mlab.json.JsonParseException;
import com.mlab.json.JsonParser;
import com.mlab.ws.ResourceException;
import com.mlab.ws.RestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PortalRESTResource extends RestResource {

  private static final Logger log = LoggerFactory.getLogger(PortalRESTResource.class);
  private final JsonParser mJsonParser = new JsonParser();

  public static Object parseJsonFromRequest(
      final HttpServletRequest request, final JsonParser parser) {
    boolean strictContentTypeEnforcement = true;
    final String contentTypeEnforcement = request.getHeader("Content-Type-Enforcement");
    if (contentTypeEnforcement != null) {
      if (contentTypeEnforcement.equalsIgnoreCase("strict")) {
        strictContentTypeEnforcement = true;
      } else if (contentTypeEnforcement.equalsIgnoreCase("relaxed")) {
        strictContentTypeEnforcement = false;
      } else {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Unrecognized Content-Type-Enforcement '"
                + contentTypeEnforcement
                + "', please use one of 'strict' or 'relaxed'");
      }
    }
    final String contentType = request.getContentType();
    if (contentType == null) {
      final String requestString =
          request.getMethod()
              + " "
              + request.getRequestURI()
              + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
      log.warn("No Content-Type set for request: {}", requestString);
      if (strictContentTypeEnforcement) {
        throw new ResourceException(
            HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
            "No Content-Type set, expecting 'application/json'.");
      }
    } else if (!(contentType.equals("application/json")
        || contentType.startsWith("application/json;"))) {
      final String requestString =
          request.getMethod()
              + " "
              + request.getRequestURI()
              + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
      log.warn("Unexpected Content-Type ({}) set for request: {}", contentType, requestString);
      if (strictContentTypeEnforcement) {
        throw new ResourceException(
            HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
            "Unexpected Content-Type '" + contentType + "', expecting 'application/json'.");
      }
    }
    try {
      // We do this here and not in the parser so that we can remember the string of the body in
      // case of error
      final String body =
          IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
      request.setAttribute("__rawBody", body);
      if (body.isEmpty()) {
        return null;
      } else {
        return JsonParser.mongoParse(body);
      }
    } catch (final UnsupportedEncodingException uee) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Unsupported charset encoding: " + uee.getMessage(),
          uee);
    } catch (final Exception e) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Unable to read JSON from request: " + e.getMessage(),
          e);
    }
  }

  public ApiConfig getApiConfig() {
    return ApiConfig.getInstance();
  }

  @Override
  protected Object getObjectFromRequestBody(final HttpServletRequest request) {
    try {
      return parseJsonFromRequest(request, getJsonParser());
    } catch (final JsonParseException e) {
      handleJsonParseException(e, request);
      return null;
    } catch (final Exception e) {
      if (e instanceof ResourceException) {
        throw (ResourceException) e;
      } else {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Unable to read JSON from request: " + e.getMessage(),
            e);
      }
    }
  }

  protected JsonParser getJsonParser() {
    return mJsonParser;
  }

  protected Object handleJsonParseException(
      final JsonParseException jpe, final HttpServletRequest request) {
    throw new ResourceException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Could not parse JSON, please double-check syntax and encoding: " + jpe.getMessage(),
        jpe);
  }

  protected Object getJsonFromRequestBody(final HttpServletRequest request) {
    return parseJsonFromRequest(request, getJsonParser());
  }
}

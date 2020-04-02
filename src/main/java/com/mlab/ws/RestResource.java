package com.mlab.ws;

import com.mlab.json.JsonParser;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mlab.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestResource<T> extends Resource {

  private static final Logger log = LoggerFactory.getLogger(RestResource.class);
  private final JsonParser jsonParser = new JsonParser();
  List<String> methodNames = null;
  private String[] methods = new String[0];
  private String[] supportedMethods = null;

  protected abstract T getObjectFromRequestBody(HttpServletRequest request);

  public String[] getMethods() {
    return methods;
  }

  public void setMethods(String[] value) {
    if (value == null) {
      value = new String[0];
    }
    methods = value;
    methodNames = null;
    supportedMethods = null;
  }

  public final String[] getSupportedMethods() {
    if (supportedMethods == null) {
      final Set<String> ms = new HashSet<>(Arrays.asList(getMethods()));
      ms.add(HttpMethod.OPTIONS.name());
      supportedMethods = ms.toArray(new String[0]);
    }
    return supportedMethods;
  }

  public boolean supportsMethod(final String method) {
    final String[] methods = getSupportedMethods();
    for (final String m : methods) {
      if (m.equals(method)) {
        return true;
      }
    }
    return false;
  }

  public void service(final HttpServletRequest request, final HttpServletResponse response) {
    final String method = getMethod(request);

    if (method == null) {
      throw new WebServiceException("Unexpected null Http method");
    }

    if (!supportsMethod(method) && !method.equals(HttpMethod.OPTIONS.name())) {
      throw new ResourceException(
          HttpServletResponse.SC_METHOD_NOT_ALLOWED, method + " not allowed.");
    }
    switch (HttpMethod.valueOf(method)) {
      case GET:
        serviceGet(request, response);
        break;
      case POST:
        servicePost(request, response);
        break;
      case PUT:
        servicePut(request, response);
        break;
      case DELETE:
        serviceDelete(request, response);
        break;
      case CREATE:
        serviceCreate(request, response);
        break;
      case HEAD:
        serviceHead(request, response);
        break;
      case OPTIONS:
        serviceOptions(request, response);
        break;
      default:
        log.error("Unsupported method: " + method);
        throw new ResourceException("Unsupported method");
    }
  }

  public void serviceGet(final HttpServletRequest request, final HttpServletResponse response) {
    final Map parameters = getParameters(request);
    final T result = handleGet(parameters, makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void servicePost(final HttpServletRequest request, final HttpServletResponse response) {
    final T object = getObjectFromRequestBody(request);
    final T result = handlePost(object, makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void servicePut(final HttpServletRequest request, final HttpServletResponse response) {
    final T object = getObjectFromRequestBody(request);
    final T result = handlePut(object, makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void serviceDelete(final HttpServletRequest request, final HttpServletResponse response) {
    final T result = handleDelete(makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void serviceCreate(final HttpServletRequest request, final HttpServletResponse response) {
    final T result = handleCreate(makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void serviceHead(final HttpServletRequest request, final HttpServletResponse response) {
    throw new WebServiceException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not implemented");
  }

  public void serviceOptions(final HttpServletRequest request, final HttpServletResponse response) {
    final String[] methods = getSupportedMethods();
    if (methods != null && methods.length > 0) {
      final StringBuilder methodStr = new StringBuilder();
      for (int i = 0; i < methods.length - 1; i++) {
        methodStr.append(methods[i]);
        methodStr.append(",");
      }
      methodStr.append(methods[methods.length - 1]);
      final String str = methodStr.toString();
      response.addHeader("Allow", str);
      response.addHeader("Access-Control-Allow-Methods", str);
    }
  }

  protected T handleGet(final Map parameters, final RequestContext context) {
    return null;
  }

  protected T handlePost(final T object, final RequestContext context) {
    return null;
  }

  protected T handlePut(final T object, final RequestContext context) {
    return null;
  }

  protected T handleDelete(final RequestContext context) {
    return null;
  }

  protected T handleCreate(final RequestContext context) {
    return null;
  }

  protected T handleHead(final RequestContext context) {
    return null;
  }

  protected T handleOptions(final RequestContext context) {
    return null;
  }

  protected void processResponse(
      final T result, final HttpServletRequest request, final HttpServletResponse response) {
    response.setContentType("application/json; charset=utf-8");
    Writer w = null;

    try {
      w = response.getWriter();
      jsonParser.serialize(result, w);
    } catch (final Exception e) {
      throw new WebServiceException(e);
    } finally {
      try {
        if (w != null) {
          w.flush();
        }
      } catch (final Exception e) {
        log.warn("Unexpected exception flushing output stream", e);
      }
    }
  }

  private String getMethod(final HttpServletRequest request) {
    String result = request.getMethod();
    if (result != null) {
      result = result.toUpperCase();
    }
    return result;
  }

  protected Map getParameters(final HttpServletRequest request) {
    final Map result = new HashMap();

    final Enumeration e = request.getParameterNames();
    while (e.hasMoreElements()) {
      final String pname = (String) e.nextElement();
      result.put(pname, request.getParameter(pname));
    }

    return result;
  }

  protected RequestContext makeRequestContext(
      final HttpServletRequest request, final HttpServletResponse response) {
    final RequestContext result = new RequestContext();
    result.setServletRequest(request);
    result.setServletResponse(response);
    return result;
  }
}

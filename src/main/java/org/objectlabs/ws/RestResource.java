package org.objectlabs.ws;

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
import org.objectlabs.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestResource<T> extends ViewableResource {

  private static final Logger log = LoggerFactory.getLogger(RestResource.class);
  protected static ThreadLocal mResultObject = new ThreadLocal();
  List<String> methodNames = null;
  private String[] methods = new String[0];
  private String[] supportedMethods = null;

  private boolean mAuditing = false;

  protected abstract String getDefaultViewName();

  protected abstract View getDefaultView();

  protected abstract T getObjectFromRequestBody(HttpServletRequest request);

  public View getView() {
    View result = super.getView();
    if (result == null) {
      result = getDefaultView();
    }
    return (result);
  }

  public View getView(String viewName) {
    View result = super.getView(viewName);
    if (result == null) {
      if (viewName.equals(getDefaultViewName())) {
        result = getDefaultView();
      }
    }
    return (result);
  }

  public String[] getMethods() {
    return (methods);
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
      Set<String> ms = new HashSet<String>(Arrays.asList(getMethods()));
      ms.add(HttpMethod.OPTIONS.name());
      supportedMethods = ms.toArray(new String[ms.size()]);
    }
    return supportedMethods;
  }

  public List<String> getSupportedMethodNames() {
    if (methodNames == null) {
      List<String> names = new ArrayList<String>();
      for (String m : getSupportedMethods()) {
        names.add(m);
      }
      methodNames = names;
    }
    return methodNames;
  }

  public boolean isAuditing() {
    return mAuditing;
  }

  public void setAuditing(boolean auditing) {
    mAuditing = auditing;
  }

  public boolean supportsMethod(String method) {
    String[] methods = getSupportedMethods();
    for (String m : methods) {
      if (m.equals(method)) {
        return (true);
      }
    }
    return (false);
  }

  public void service(HttpServletRequest request, HttpServletResponse response) {
    mResultObject.remove(); // Probably not necessary, but best to be safe!
    String method = getMethod(request);

    if (method == null) {
      throw (new WebServiceException("Unexpected null Http method"));
    }

    if (!supportsMethod(method) && !method.equals(HttpMethod.OPTIONS.name())) {
      // TODO: The response MUST include an Allow header containing a list of valid methods for the
      // requested resource.
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
        throw (new ResourceException("Unsupported method"));
    }
  }

  public void serviceGet(HttpServletRequest request, HttpServletResponse response) {
    Map parameters = getParameters(request);
    T result = handleGet(parameters, makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void servicePost(HttpServletRequest request, HttpServletResponse response) {
    T object = getObjectFromRequestBody(request);
    T result = handlePost(object, makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void servicePut(HttpServletRequest request, HttpServletResponse response) {
    T object = getObjectFromRequestBody(request);
    T result = handlePut(object, makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void serviceDelete(HttpServletRequest request, HttpServletResponse response) {
    T result = handleDelete(makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  /**
   * serviceCreate
   *
   * <p>This is a custom ObjectLabs HTTP method
   */
  public void serviceCreate(HttpServletRequest request, HttpServletResponse response) {
    T result = handleCreate(makeRequestContext(request, response));
    processResponse(result, request, response);
  }

  public void serviceHead(HttpServletRequest request, HttpServletResponse response) {
    throw new WebServiceException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not implemented");
  }

  public void serviceOptions(HttpServletRequest request, HttpServletResponse response) {
    String[] methods = getSupportedMethods();
    if (methods != null && methods.length > 0) {
      StringBuilder methodStr = new StringBuilder();
      for (int i = 0; i < methods.length - 1; i++) {
        methodStr.append(methods[i]);
        methodStr.append(",");
      }
      methodStr.append(methods[methods.length - 1]);
      String str = methodStr.toString();
      response.addHeader("Allow", str);
      response.addHeader("Access-Control-Allow-Methods", str);
    }
  }

  protected T handleGet(Map parameters, RequestContext context) {
    return (null);
  }

  protected T handlePost(T object, RequestContext context) {
    return (null);
  }

  protected T handlePut(T object, RequestContext context) {
    return (null);
  }

  protected T handleDelete(RequestContext context) {
    return (null);
  }

  protected T handleCreate(RequestContext context) {
    return (null);
  }

  protected T handleHead(RequestContext context) {
    return (null);
  }

  protected T handleOptions(RequestContext context) {
    return (null);
  }

  /**
   * processResponse
   *
   * <p>This is eventually going to get elaborate combining view selection with HTTP content
   * negotiation to select the best view and format. Will probably need to know original method or
   * at least differentiate GET and the others via some flags that encode intentions (without
   * passing METHOD per say).
   */
  protected void processResponse(
      T result, HttpServletRequest request, HttpServletResponse response) {
    if (isAuditing()) {
      mResultObject.set(result);
    }
    render(result, request, response);
  }

  private String getMethod(HttpServletRequest request) {
    String result = request.getMethod();
    if (result != null) {
      result = result.toUpperCase();
    }
    return (result);
  }

  protected Map getParameters(HttpServletRequest request) {
    Map result = new HashMap();

    Enumeration e = request.getParameterNames();
    while (e.hasMoreElements()) {
      String pname = (String) e.nextElement();
      result.put(pname, request.getParameter(pname));
    }

    return (result);
  }

  protected RequestContext makeRequestContext(
      HttpServletRequest request, HttpServletResponse response) {
    RequestContext result = new RequestContext();
    result.setServletRequest(request);
    result.setServletResponse(response);
    return (result);
  }
}

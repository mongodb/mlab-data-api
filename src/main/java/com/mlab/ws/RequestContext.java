package com.mlab.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

    public RequestContext() {
        super();
    }

    private HttpServletRequest servletRequest;

    public HttpServletRequest getServletRequest() {
        return(servletRequest);
    }

    public void setServletRequest(HttpServletRequest value) {
        servletRequest = value;
    }

    private HttpServletResponse servletResponse;

    public HttpServletResponse getServletResponse() {
        return(servletResponse);
    }

    public void setServletResponse(HttpServletResponse value) {
        servletResponse = value;
    }

}

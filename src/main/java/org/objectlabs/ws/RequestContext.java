package org.objectlabs.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

    /*************************************************************************
     * Constructor                                                 
     */
    public RequestContext() {
        super();
    }

    /*************************************************************************
     * servletRequest
     */
    private HttpServletRequest servletRequest;

    public HttpServletRequest getServletRequest() {
        return(servletRequest);
    }

    public void setServletRequest(HttpServletRequest value) {
        servletRequest = value;
    }

    /*************************************************************************
     * servletResponse
     */
    private HttpServletResponse servletResponse;

    public HttpServletResponse getServletResponse() {
        return(servletResponse);
    }

    public void setServletResponse(HttpServletResponse value) {
        servletResponse = value;
    }

}

package org.objectlabs.ws;

import javax.servlet.http.HttpServletRequest;

/******************************************************************************
 * HttpRequestResource
 *
 * @author William Shulman
 *
 * 06.09.2010
 */
public class HttpRequestResource extends DirectoryResource {

    /********************************************************************     
     * Constructor
     */
    public HttpRequestResource() {
        super();
    }

    /********************************************************************     
     * Constructor
     */
    public HttpRequestResource(HttpServletRequest request) {
        super();
        setRequest(request);
    }

    /********************************************************************     
     * request
     */
    private HttpServletRequest request;

    public HttpServletRequest getRequest() {
        return(request);
    }

    public void setRequest(HttpServletRequest value) {
        request = value;
    }

}

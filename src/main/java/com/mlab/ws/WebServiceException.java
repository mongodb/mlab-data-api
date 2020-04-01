package com.mlab.ws;

import javax.servlet.http.HttpServletResponse;
import com.mlab.MlabException;
import com.mlab.http.HttpStatus;


public class WebServiceException extends MlabException {

    private int mStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    public int getStatusCode ( )
    { return mStatusCode; }
    public void setStatusCode ( int statusCode )
    { mStatusCode = statusCode; }

    public boolean isSevere ( ) {
        int statusCode = getStatusCode();
        return (statusCode / 100 == 5) &&
               statusCode != HttpServletResponse.SC_NOT_IMPLEMENTED &&
               statusCode != HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED;
    }

    @Override
    public String getMessage () {
        String superMessage = super.getMessage();
        if ( superMessage != null ) {
            return superMessage;
        }
        return HttpStatus.lookup(getStatusCode()).toString();
    }

    public WebServiceException( ) { super(); }
    public WebServiceException(String message ) { super(message); }
    public WebServiceException(Throwable cause ) { super(cause); }
    public WebServiceException(String message, Throwable cause ) { super(message, cause); }

    public WebServiceException(int statusCode ) { this(); setStatusCode(statusCode); }
    public WebServiceException(int statusCode, String message ) { this(message); setStatusCode(statusCode); }
    public WebServiceException(int statusCode, Throwable cause ) { this(cause); setStatusCode(statusCode); }
    public WebServiceException(int statusCode, String message, Throwable cause ) { this(message, cause); setStatusCode(statusCode); }

}
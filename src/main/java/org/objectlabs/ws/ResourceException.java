package org.objectlabs.ws;


public class ResourceException extends WebServiceException {

    public ResourceException( ) { super(); }
    public ResourceException(String message ) { super(message); }
    public ResourceException(Throwable cause ) { super(cause); }
    public ResourceException(String message, Throwable cause ) { super(message, cause); }

    public ResourceException(int statusCode ) { super(statusCode); }
    public ResourceException(int statusCode, String message ) { super(statusCode, message); }
    public ResourceException(int statusCode, Throwable cause ) { super(statusCode, cause); }
    public ResourceException(int statusCode, String message, Throwable cause ) { super(statusCode, message, cause); }

}
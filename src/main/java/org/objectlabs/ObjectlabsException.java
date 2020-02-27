package org.objectlabs;

public class ObjectlabsException extends RuntimeException {

    public ObjectlabsException( ) { super(); }
    public ObjectlabsException(String message ) { super(message); }
    public ObjectlabsException(Throwable cause ) { super(cause); }
    public ObjectlabsException(String message, Throwable cause ) { super(message, cause); }

}
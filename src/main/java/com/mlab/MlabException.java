package com.mlab;

public class MlabException extends RuntimeException {

    public MlabException( ) { super(); }
    public MlabException(String message ) { super(message); }
    public MlabException(Throwable cause ) { super(cause); }
    public MlabException(String message, Throwable cause ) { super(message, cause); }

}
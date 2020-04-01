package com.mlab.json;


public class JsonParseException extends RuntimeException {

    public JsonParseException( ) { super(); }
    public JsonParseException(final String message ) { super(message); }
    public JsonParseException(final Throwable cause ) { super(cause); }
    public JsonParseException(final String message, final Throwable cause ) { super(message, cause); }

}
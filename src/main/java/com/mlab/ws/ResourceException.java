package com.mlab.ws;

public class ResourceException extends WebServiceException {

  public ResourceException() {
    super();
  }

  public ResourceException(final String message) {
    super(message);
  }

  public ResourceException(final Throwable cause) {
    super(cause);
  }

  public ResourceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ResourceException(final int statusCode) {
    super(statusCode);
  }

  public ResourceException(final int statusCode, final String message) {
    super(statusCode, message);
  }

  public ResourceException(final int statusCode, final Throwable cause) {
    super(statusCode, cause);
  }

  public ResourceException(final int statusCode, final String message, final Throwable cause) {
    super(statusCode, message, cause);
  }
}

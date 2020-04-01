package com.mlab;

public class MlabException extends RuntimeException {

  public MlabException() {
    super();
  }

  public MlabException(final String message) {
    super(message);
  }

  public MlabException(final Throwable cause) {
    super(cause);
  }

  public MlabException(final String message, final Throwable cause) {
    super(message, cause);
  }
}

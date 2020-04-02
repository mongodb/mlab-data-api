package com.mlab.ws;

import javax.servlet.http.HttpServletResponse;
import com.mlab.MlabException;
import com.mlab.http.HttpStatus;

public class WebServiceException extends MlabException {

  private int mStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

  public WebServiceException() {
    super();
  }

  public WebServiceException(final String message) {
    super(message);
  }

  public WebServiceException(final Throwable cause) {
    super(cause);
  }

  public WebServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public WebServiceException(final int statusCode) {
    this();
    setStatusCode(statusCode);
  }

  public WebServiceException(final int statusCode, final String message) {
    this(message);
    setStatusCode(statusCode);
  }

  public WebServiceException(final int statusCode, final Throwable cause) {
    this(cause);
    setStatusCode(statusCode);
  }

  public WebServiceException(final int statusCode, final String message, final Throwable cause) {
    this(message, cause);
    setStatusCode(statusCode);
  }

  public int getStatusCode() {
    return mStatusCode;
  }

  public void setStatusCode(final int statusCode) {
    mStatusCode = statusCode;
  }

  public boolean isSevere() {
    final int statusCode = getStatusCode();
    return statusCode / 100 == 5
        && statusCode != HttpServletResponse.SC_NOT_IMPLEMENTED
        && statusCode != HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED;
  }

  @Override
  public String getMessage() {
    final String superMessage = super.getMessage();
    if (superMessage != null) {
      return superMessage;
    }
    final HttpStatus status = HttpStatus.lookup(getStatusCode());
    return status == null ? null : status.toString();
  }
}

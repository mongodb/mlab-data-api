package org.olabs.portal.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiConfig {
  public static final String PORT_FIELD = "port";

  @JsonProperty(PORT_FIELD)
  private int _port;
  public int getPort() {
    return _port;
  }
}

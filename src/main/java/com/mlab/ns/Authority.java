package com.mlab.ns;

public class Authority {

  private String userInfo;
  private String host;
  private int port = -1;

  public Authority(final String authority) {
    if (authority == null || authority.equals("")) {
      throw new IllegalArgumentException("authority cannot be null");
    }

    final int indexOfAt = authority.indexOf("@");
    if (indexOfAt != -1) {
      setUserInfo(authority.substring(0, indexOfAt));
    }

    final int hostIndex = indexOfAt + 1;
    final int colonIndex = authority.lastIndexOf(":");
    if (colonIndex == -1) {
      setHost(authority.substring(hostIndex));
    } else {
      setHost(authority.substring(hostIndex, colonIndex));
      final String portString = authority.substring(colonIndex + 1);
      if (!portString.equals("")) {
        try {
          setPort(Integer.parseInt(portString));
        } catch (final Exception e) {
          throw new IllegalArgumentException("port must be numeric");
        }
      }
    }
  }

  public String getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(final String value) {
    userInfo = value;
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String value) {
    host = value;
  }

  public int getPort() {
    return port;
  }

  public void setPort(final int value) {
    port = value;
  }

  public String toString() {
    final StringBuilder s = new StringBuilder();

    if (getUserInfo() != null) s.append(getUserInfo()).append("@");
    s.append(getHost());
    if (getPort() != -1) s.append(":").append(getPort());

    return s.toString();
  }
}

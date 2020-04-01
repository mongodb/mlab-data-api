package com.mlab.ns;

public class Authority {

    public Authority() {
        super();
    }

    public Authority(String authority) {
        if (authority == null || authority.equals("")) {
            throw(new IllegalArgumentException("authority cannot be null"));
        }

        int indexOfAt = authority.indexOf("@");
        if (indexOfAt != -1) {
            setUserInfo(authority.substring(0, indexOfAt));
        }

        int hostIndex = indexOfAt + 1;
        int colonIndex = authority.lastIndexOf(":");
        if (colonIndex == -1) {
            setHost(authority.substring(hostIndex));
        } else {
            setHost(authority.substring(hostIndex, colonIndex));
            String portString = authority.substring(colonIndex + 1);
            if (portString != null && !portString.equals("")) {
                try {
                    setPort(Integer.parseInt(portString));
                } catch (Exception e) {
                    throw(new IllegalArgumentException("port must be numeric"));
                }
            }
        }
    }

    public Authority(String host, int port) {
        this(null, host, port);
    }

    public Authority(String userInfo, String host, int port) {
        setUserInfo(userInfo);
        setHost(host);
        setPort(port);
    }

    private String userInfo;

    public String getUserInfo() {
        return(userInfo);
    }

    public void setUserInfo(String value) {
        userInfo = value;
    }

    private String host;

    public String getHost() {
        return(host);
    }

    public void setHost(String value) {
        host = value;
    }

    private int port = -1;

    public int getPort() {
        return(port);
    }

    public void setPort(int value) {
        port = value;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();

        if (getUserInfo() != null) buff.append(getUserInfo()).append("@");
        buff.append(getHost());
        if (getPort() != -1) buff.append(":").append(getPort());

        return(buff.toString());
    }
}

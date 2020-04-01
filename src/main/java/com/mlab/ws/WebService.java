package com.mlab.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mlab.ns.Namespace;

public interface WebService extends Namespace {

  public void service(HttpServletRequest request, HttpServletResponse response);
}

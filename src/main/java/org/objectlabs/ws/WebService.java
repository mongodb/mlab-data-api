package org.objectlabs.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.objectlabs.ns.Namespace;

public interface WebService extends Namespace {

  public void service(HttpServletRequest request, HttpServletResponse response);
}

package org.olabs.portal.api;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.objectlabs.ws.Resource;

public class ApiServlet extends HttpServlet {

  @Override
  public void service(final HttpServletRequest req, final HttpServletResponse res)
      throws ServletException, IOException {
    final String path = req.getPathInfo();
    final Resource resource = new RootResource().resolve(path);
    if (resource == null) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      resource.service(req, res);
    }
  }
}

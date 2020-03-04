package org.olabs.portal.api;

import java.io.File;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.objectlabs.ws.ResourceException;

public class Main {
  private static final String WEB_APP_DIR = "src/main/api";

  public static void main(String[] args) throws Exception {
    System.out.println("Starting mLab Data API...");
    final Tomcat tomcat = new Tomcat();
    try {
      tomcat.setPort(ApiConfig.getInstance().getPort());
    } catch (final ResourceException e) {
      System.out.println(String.format("Error getting config: %s", e.getMessage()));
      System.exit(1);
    }
    final StandardContext ctx =
        (StandardContext) tomcat.addWebapp("", new File(WEB_APP_DIR).getAbsolutePath());
    final WebResourceRoot resources = new StandardRoot(ctx);
    final File additionWebInfClasses = new File("target/classes");
    resources.addPreResources(
        new DirResourceSet(
            resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
    ctx.setResources(resources);
    tomcat.start();
    tomcat.getServer().await();
  }
}

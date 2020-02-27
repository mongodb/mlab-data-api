package org.olabs.portal.api;

import java.io.File;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

public class Main {
  public static final String WEB_APP_DIR = "src/main/api";

  public static void main(String[] args) throws Exception {
    final Tomcat tomcat = new Tomcat();
    tomcat.setPort(9090);
    final StandardContext ctx = (StandardContext) tomcat.addWebapp("", new File(WEB_APP_DIR).getAbsolutePath());
    final File additionWebInfClasses = new File("target/classes");
    final WebResourceRoot resources = new StandardRoot(ctx);
    resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
            additionWebInfClasses.getAbsolutePath(), "/"));
    ctx.setResources(resources);
    tomcat.start();
    tomcat.getServer().await();
  }
}

package org.olabs.portal.api;

import java.io.File;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.objectlabs.ws.ResourceException;

public class Main {
  private static final String CONFIG_ENV_VAR = "MLAB_DATA_API_CONFIG";
  private static final String API_KEY_ENV_VAR = "MLAB_DATA_API_KEY";
  private static final String WEB_APP_DIR = "www";
  private static final String WEB_APP_PATH = "/api/1";

  private static Tomcat _tomcat = null;

  public static void main(String[] args) throws Exception {
    System.setProperty(ApiConfig.CONFIG_PROPERTY, System.getenv(CONFIG_ENV_VAR));
    System.setProperty(ApiConfig.API_KEY_PROPERTY, System.getenv(API_KEY_ENV_VAR));
    System.setProperty(ApiConfig.APP_DIR_PROPERTY, WEB_APP_DIR);
    start();
    _tomcat.getServer().await();
  }

  public static void start() throws LifecycleException, ServletException {
    if (_tomcat != null) {
      return;
    }
    _tomcat = new Tomcat();
    try {
      _tomcat.setPort(ApiConfig.getInstance().getPort());
    } catch (final ResourceException e) {
      System.out.println(String.format("Error getting config: %s", e.getMessage()));
      System.exit(1);
    }
    final String webAppDir = System.getProperty(ApiConfig.APP_DIR_PROPERTY);
    final StandardContext ctx =
        (StandardContext) _tomcat.addWebapp(WEB_APP_PATH, new File(webAppDir).getAbsolutePath());
    final WebResourceRoot resources = new StandardRoot(ctx);
    final File additionWebInfClasses = new File("target/classes");
    if (additionWebInfClasses.isDirectory() && additionWebInfClasses.exists()) {
      resources.addPreResources(
          new DirResourceSet(
              resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
    }
    ctx.setResources(resources);
    System.out.println("Starting mLab Data API...");
    _tomcat.start();
  }

  public static void stop() throws LifecycleException {
    _tomcat.stop();
  }
}

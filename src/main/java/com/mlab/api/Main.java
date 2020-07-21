package com.mlab.api;

import java.io.File;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import com.mlab.ws.ResourceException;

public class Main {
  private static final String CONFIG_ENV_VAR = "MLAB_DATA_API_CONFIG";
  private static final String API_KEY_ENV_VAR = "MLAB_DATA_API_KEY";
  private static final String PORT_ENV_VAR = "PORT";
  private static final String WEB_APP_DIR = "www";
  private static final String WEB_APP_PATH = "";

  private static Tomcat _tomcat = null;

  public static void main(final String[] args) throws Exception {
    readEnvToProp(CONFIG_ENV_VAR, ApiConfig.CONFIG_PROPERTY);
    readEnvToProp(API_KEY_ENV_VAR, ApiConfig.API_KEY_PROPERTY);
    System.setProperty(ApiConfig.APP_DIR_PROPERTY, WEB_APP_DIR);
    start();
    _tomcat.getServer().await();
  }

  private static void readEnvToProp(final String env, final String prop) {
    final String value = System.getenv(env);
    if(value == null) {
      System.out.println(String.format("%s environment variable is required", env));
      System.exit(1);
    }
    System.setProperty(prop, value);
  }

  public static void start() throws LifecycleException, ServletException {
    if (_tomcat != null) {
      return;
    }
    _tomcat = new Tomcat();
    int port = ApiConfig.getInstance().getPort();
    if(port <= 0) {
      final String portEnv = System.getenv(PORT_ENV_VAR);
      if(portEnv == null || portEnv.isEmpty()) {
        System.out.println("PORT is required");
        System.exit(1);
      }
      port = Integer.valueOf(portEnv);
    }
    try {
      _tomcat.setPort(port);
    } catch (final ResourceException e) {
      System.out.println(String.format("Error getting config: %s", e.getMessage()));
      System.exit(1);
    }
    _tomcat.getConnector().setAttribute("relaxedQueryChars", "\"<>[]{}");
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
    System.out.println("Starting mLab Data API on port "+port+"...");
    _tomcat.start();
  }

  public static void stop() throws LifecycleException {
    _tomcat.stop();
  }
}

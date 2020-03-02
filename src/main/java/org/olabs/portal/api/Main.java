package org.olabs.portal.api;

import java.io.File;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

public class Main {
  private static final String WEB_APP_DIR = "src/main/api";
  private static final int API_KEY_LENGTH = 24;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  private static String generateApiKey(int numBytes) {
    byte[] randomBytes = new byte[numBytes];
    SECURE_RANDOM.nextBytes(randomBytes);
    return ENCODER.encodeToString(randomBytes);
  }

  public static void main(String[] args) throws Exception {
    if (args != null && args.length > 0) {
      switch (Command.named(args[0])) {
        case API_KEY:
          System.out.println(generateApiKey(API_KEY_LENGTH));
          System.exit(0);
        case START:
          System.out.println("Starting mLab Data API...");
        default:
          System.out.println("Invalid command");
          System.exit(1);
      }
    }

    final Tomcat tomcat = new Tomcat();
    tomcat.setPort(9090);
    final StandardContext ctx =
        (StandardContext) tomcat.addWebapp("", new File(WEB_APP_DIR).getAbsolutePath());
    final File additionWebInfClasses = new File("target/classes");
    final WebResourceRoot resources = new StandardRoot(ctx);
    resources.addPreResources(
        new DirResourceSet(
            resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
    ctx.setResources(resources);
    tomcat.start();
    tomcat.getServer().await();
  }

  public enum Command {
    START("start"),
    API_KEY("api-key");

    private final String _name;

    Command(final String pName) {
      _name = pName;
    }

    public static Command named(String pName) {
      return Arrays.stream(Command.values())
          .filter(c -> c.getName().equals(pName))
          .findFirst()
          .orElse(null);
    }

    public String getName() {
      return _name;
    }
  }
}

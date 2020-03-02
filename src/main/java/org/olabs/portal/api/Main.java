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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.objectlabs.ws.ResourceException;

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

    final CommandLineParser parser = new DefaultParser();
    final CommandLine mainCmd = parser.parse(getMainOptions(), args);

    if (mainCmd.hasOption("h") || mainCmd.getArgs().length < 1) {
      printMainHelp();
      System.exit(0);
    }

    final Command cmd = Command.named(mainCmd.getArgs()[0]);
    if(cmd == null) {
      System.out.println("Command not found: "+mainCmd.getArgs()[0]);
      printMainHelp();
      System.exit(1);
    }
    switch (cmd) {
      case API_KEY:
        System.out.println(generateApiKey(API_KEY_LENGTH));
        System.exit(0);
      case START:
        System.out.println("Starting mLab Data API...");
        break;
      default:
        System.out.println("Invalid command");
        System.exit(1);
    }

    final String[] startOptions =
        mainCmd.getArgs().length > 1
            ? Arrays.copyOfRange(mainCmd.getArgs(), 1, mainCmd.getArgs().length)
            : new String[] {};
    final CommandLine startCmd = parser.parse(getStartOptions(), startOptions);

    if (startCmd.hasOption("h")) {
      printStartHelp();
      System.exit(0);
    }

    final Tomcat tomcat = new Tomcat();
    try {
      tomcat.setPort(ApiConfig.getInstance().getPort());
    } catch(final ResourceException e) {
      System.out.println(String.format("Error getting config: %s", e.getMessage()));
      System.exit(1);
    }
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

  private static Options getMainOptions() {
    final Options result = new Options();
    result.addOption("h", "help", false, "print this message");
    return (result);
  }

  private static void printMainHelp() {
    new HelpFormatter().printHelp("mlab-data-api [OPTIONS] [api-key | start]", getMainOptions());
  }

  private static Options getStartOptions() {
    final Options result = new Options();
    result.addOption("h", "help", false, "print this message");
    return (result);
  }

  private static void printStartHelp() {
    new HelpFormatter().printHelp("mlab-data-api start [OPTIONS]", getStartOptions());
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

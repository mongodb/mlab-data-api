package com.mlab.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.mlab.ws.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiConfig {
  public static final String CONFIG_PROPERTY = "mlab.apiConfig";
  public static final String API_KEY_PROPERTY = "mlab.apiKey";
  public static final String APP_DIR_PROPERTY = "mlab.appDir";
  public static final String PORT_FIELD = "port";
  public static final String CLUSTERS_FIELD = "clusters";
  public static final String DATABASES_FIELD = "databases";
  private static final Logger LOG = LoggerFactory.getLogger(ApiConfig.class);
  private static ApiConfig instance;
  private static String apiKey;

  @JsonProperty(PORT_FIELD)
  private int _port;

  @JsonProperty(CLUSTERS_FIELD)
  private Map<String, String> _clusters;

  @JsonProperty(DATABASES_FIELD)
  private Map<String, String> _databases;

  private ConcurrentHashMap<String, MongoClient> clusterConnections = new ConcurrentHashMap<>();

  private ConcurrentHashMap<String, MongoClient> databaseConnections = new ConcurrentHashMap<>();

  public static ApiConfig getInstance() throws ResourceException {
    if (instance == null) {
      instance = getInstance(System.getProperty(CONFIG_PROPERTY));
    }
    return instance;
  }

  public static ApiConfig getInstance(final String pConfigString) throws ResourceException {
    if (pConfigString == null) {
      throw new ResourceException("Config is missing");
    }
    try {
      return parseConfig(pConfigString);
    } catch (final IOException e) {
      throw new ResourceException(e);
    }
  }

  public static String getApiKey() {
    if (apiKey == null) {
      apiKey = System.getProperty(API_KEY_PROPERTY);
    }
    return apiKey;
  }

  private static ApiConfig parseConfig(final String pConfigString) throws IOException {
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    // file?
    try {
      final File file = new File(pConfigString);
      if (!file.exists() || !file.isFile()) {
        throw new FileNotFoundException();
      }
      LOG.info("Loading config from file {}", file.getAbsolutePath());
      return mapper.readValue(file, ApiConfig.class);
    } catch (final IOException e) {
      // raw YAML?
      try {
        LOG.info("Parsing raw YAML config...");
        return mapper.readValue(pConfigString, ApiConfig.class);
      } catch (final IOException i) {
        LOG.error("Error parsing YAML", i);
        // throw original file exception
        throw e;
      }
    }
  }

  public Map<String, String> getClusters() {
    return _clusters;
  }

  public Map<String, String> getDatabases() {
    return _databases;
  }

  public MongoClientURI getClusterUri(final String pCluster) {
    final String uri = getClusters().get(pCluster);
    return uri == null ? null : new MongoClientURI(uri);
  }

  public MongoClient getClusterConnection(final String pCluster) {
    return clusterConnections.computeIfAbsent(
        pCluster,
        c -> {
          final MongoClientURI uri = getClusterUri(c);
          return uri == null ? null : new MongoClient(uri);
        });
  }

  public MongoClientURI getDatabaseUri(final String pDatabase) {
    final String uri = getDatabases().get(pDatabase);
    return uri == null ? null : new MongoClientURI(uri);
  }

  public MongoClient getDatabaseConnection(final String pDatabase) {
    return databaseConnections.computeIfAbsent(
        pDatabase,
        c -> {
          final MongoClientURI uri = getDatabaseUri(c);
          return uri == null ? null : new MongoClient(uri);
        });
  }

  public int getPort() {
    return _port;
  }
}

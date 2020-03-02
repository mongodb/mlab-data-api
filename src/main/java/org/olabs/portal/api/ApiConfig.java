package org.olabs.portal.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.objectlabs.ws.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiConfig {
  public static final String PORT_FIELD = "port";
  private static final Logger LOG = LoggerFactory.getLogger(ApiConfig.class);
  private static final String CONFIG_FILE_ENV_VAR = "MLAB_DATA_API_CONFIG";
  private static final String API_KEY_ENV_VAR = "MLAB_DATA_API_KEY";
  private static ApiConfig instance;
  private static String apiKey;

  @JsonProperty(PORT_FIELD)
  private int _port;

  public static final String CLUSTERS_FIELD = "clusters";

  @JsonProperty(CLUSTERS_FIELD)
  private Map<String,String> _clusters;
  public Map<String,String> getClusters() {
    return _clusters;
  }

  public static ApiConfig getInstance() throws ResourceException {
    if (instance == null) {
      final String configString = System.getenv(CONFIG_FILE_ENV_VAR);
      if (configString == null) {
        throw new ResourceException(String.format("%s is missing", CONFIG_FILE_ENV_VAR));
      }
      try {
        instance = parseConfig(configString);
      } catch(final IOException e) {
        throw new ResourceException(e);
      }
    }
    return instance;
  }

  public static String getApiKey() {
    if (apiKey == null) {
      apiKey = System.getenv(API_KEY_ENV_VAR);
    }
    return apiKey;
  }

  private static ApiConfig parseConfig(String pConfigString) throws IOException {
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

  public int getPort() {
    return _port;
  }
}

package org.olabs.portal.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ApiConfig {
  private static final String CONFIG_FILE_ENV_VAR = "MLAB_DATA_API_CONFIG";
  private static final String API_KEY_ENV_VAR = "MLAB_DATA_API_KEY";
  private static ApiConfig instance;
  private static String apiKey;
  public static final String PORT_FIELD = "port";

  @JsonProperty(PORT_FIELD)
  private int _port;
  public int getPort() {
    return _port;
  }

  public static ApiConfig getInstance() throws IOException {
    if(instance == null) {
      final String configFile = System.getenv(CONFIG_FILE_ENV_VAR);
      if(configFile == null) {
        System.out.println(String.format("%s is required", CONFIG_FILE_ENV_VAR));
        System.exit(1);
      }
      instance = parseConfig(configFile);
    }
    return instance;
  }

  public static String getApiKey() {
    if(apiKey == null) {
      apiKey = System.getenv(API_KEY_ENV_VAR);
    }
    return apiKey;
  }

  private static ApiConfig parseConfig(String pFile) throws IOException {
    final File file = new File(pFile);
    if(!file.exists() || !file.isFile()) {
      throw new FileNotFoundException();
    }
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    return mapper.readValue(file, ApiConfig.class);
  }

}

package org.olabs.portal.api;

import org.junit.Before;

public abstract class BaseResourceTest {
  private static final String TEST_CONFIG_ENV_VAR = "MLAB_DATA_API_TEST_CONFIG";
  private static final String TEST_PROD_API_KEY_ENV_VAR = "MLAB_DATA_API_TEST_PROD_KEY";
  private static final String TEST_API_KEY = "PggfZYDE6EWh2FCLsvGm42cq";
  private MlabDataApiClient _client;
  private MlabDataApiClient _prodClient;

  @Before
  public void setUp() throws Exception {
    System.setProperty(ApiConfig.APP_DIR_PROPERTY, "src/main/webapp");
    final String config = System.getenv(TEST_CONFIG_ENV_VAR);
    if (config == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_CONFIG_ENV_VAR));
    }
    System.setProperty(ApiConfig.CONFIG_PROPERTY, config);
    System.setProperty(ApiConfig.API_KEY_PROPERTY, TEST_API_KEY);
    _client = new MlabDataApiClient(String.format("http://localhost:%s", ApiConfig.getInstance().getPort()), TEST_API_KEY);

    final String prodApiKey = System.getenv(TEST_PROD_API_KEY_ENV_VAR);
    if(prodApiKey == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_PROD_API_KEY_ENV_VAR));
    }

    _prodClient = new MlabDataApiClient("https://api.mlab.com", prodApiKey);
    Main.start();
  }

  public MlabDataApiClient getTestClient() {
    return _client;
  }

  public MlabDataApiClient getProductionClient() {
    return _prodClient;
  }
}

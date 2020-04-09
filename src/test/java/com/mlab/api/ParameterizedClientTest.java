package com.mlab.api;

import static com.mlab.mongodb.MongoUtils.oid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBRef;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.Binary;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.mlab.mongodb.MongoUtils;

@RunWith(Parameterized.class)
public abstract class ParameterizedClientTest extends BaseResourceTest {

  public static final String TEST_CLIENT_NAME = "test";
  public static final String PROD_CLIENT_NAME = "production";

  private static final String TEST_CONFIG_ENV_VAR = "MLAB_DATA_API_TEST_CONFIG";
  private static final String TEST_PROD_API_KEY_ENV_VAR = "MLAB_DATA_API_TEST_PROD_KEY";
  private static final String TEST_API_KEY = "PggfZYDE6EWh2FCLsvGm42cq";

  @Parameter public MlabDataApiClient client;

  @Parameters(name = "{0}")
  public static Iterable<? extends Object> data() {
    return List.of(getTestClient(), getProductionClient());
  }

  public static MlabDataApiClient getTestClient() {
    final String config = System.getenv(TEST_CONFIG_ENV_VAR);
    if (config == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_CONFIG_ENV_VAR));
    }
    System.setProperty(ApiConfig.CONFIG_PROPERTY, config);
    System.setProperty(ApiConfig.API_KEY_PROPERTY, TEST_API_KEY);
    return new MlabDataApiClient(
        TEST_CLIENT_NAME,
        String.format("http://localhost:%s", ApiConfig.getInstance().getPort()),
        TEST_API_KEY);
  }

  public static MlabDataApiClient getProductionClient() {
    final String prodApiKey = System.getenv(TEST_PROD_API_KEY_ENV_VAR);
    if (prodApiKey == null) {
      throw new AssertionError(
          String.format("%s environment variable is required", TEST_PROD_API_KEY_ENV_VAR));
    }
    return new MlabDataApiClient(PROD_CLIENT_NAME, "https://api.mlab.com", prodApiKey);
  }

  public boolean isTestClient() {
    return client.getName().equals(TEST_CLIENT_NAME);
  }

  public boolean isProductionClient() {
    return client.getName().equals(PROD_CLIENT_NAME);
  }

}

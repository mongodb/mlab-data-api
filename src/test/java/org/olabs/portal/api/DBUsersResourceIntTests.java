package org.olabs.portal.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;

public class DBUsersResourceIntTests extends BaseResourceTest {

  private static final String TEST_DB = "usersTest";

  @AfterClass
  public static void afterClass() {
    getTestDatabase().runCommand(new BasicDBObject("dropAllUsersFromDatabase", 1));
    getTestDatabase().drop();
  }

  @Test
  public void testGet() throws IOException {
    final String userName = client.getName()+"userTest";
    addUser(userName, userName, List.of("read"));
    final JSONArray users = client.getJsonArray(getUsersUrl());
    assertNotNull(users);
    final Optional<Object> user = users.toList().stream().filter(u -> userName.equals(((JSONObject)u).get("user"))).findFirst();
    assertTrue(user.isPresent());
  }

  private static MongoDatabase getTestDatabase() {
    return ApiConfig.getInstance().getClusterConnection(DEDICATED_CLUSTER_ID).getDatabase(TEST_DB);
  }

  private String getUsersUrl() {
    return ApiPathBuilder.start().cluster(DEDICATED_CLUSTER_ID).db(TEST_DB).users().toString();
  }

  private static void addUser(final String name, final String pwd, final List roles) {
    final BasicDBObject cmd =
        new BasicDBObject("createUser", name).append("pwd", pwd).append("roles", roles);
    getTestDatabase().runCommand(cmd);
  }
}

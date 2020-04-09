package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

public class DBUsersResourceIntTests extends ParameterizedClientTest {

  private static final String TEST_DB = "usersTest";

  @After
  public void after() {
    getTestDatabase().runCommand(new BasicDBObject("dropAllUsersFromDatabase", 1));
    getTestDatabase().drop();
  }

  @Test
  public void testGet_noUsers() throws IOException {
    final JSONArray users = client.getJsonArray(getUsersUrl());
    assertNotNull(users);
    assertTrue(users.isEmpty());
  }

  @Test
  public void testGet_oneUser() throws IOException {
    final String userName = client.getName() + "userTest";
    addUser(userName, userName, List.of("read"));
    final JSONArray users = client.getJsonArray(getUsersUrl());
    assertNotNull(users);
    assertEquals(1, users.length());
    final JSONObject user = (JSONObject) users.get(0);
    assertFalse(user.has("credentials"));
    final JSONArray roles = (JSONArray) user.get("roles");
    assertNotNull(roles);
    assertEquals(1, roles.length());
    assertEquals(Map.of("role", "read", "db", TEST_DB), ((JSONObject) roles.get(0)).toMap());
  }

  @Test
  public void testGet_manyUsers() throws IOException {
    final Map<String, String> userData =
        Map.of(
            client.getName() + "User1",
            "read",
            client.getName() + "User2",
            "readWrite",
            client.getName() + "User4",
            "dbAdmin",
            client.getName() + "User3",
            "dbOwner");
    userData.forEach((name, roles) -> addUser(name, name, List.of(roles)));

    final JSONArray users = client.getJsonArray(getUsersUrl());
    assertNotNull(users);
    assertEquals(4, users.length());
    users.forEach(
        u -> {
          final JSONObject user = (JSONObject) u;
          assertTrue(userData.containsKey(user.get("user")));
          final JSONArray roles = (JSONArray) user.get("roles");
          assertNotNull(roles);
          assertEquals(1, roles.length());
          assertEquals(
              Map.of("role", userData.get(user.get("user")), "db", TEST_DB),
              ((JSONObject) roles.get(0)).toMap());
        });
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

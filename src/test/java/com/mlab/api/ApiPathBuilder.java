package com.mlab.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiPathBuilder {

  private boolean clusters;
  private boolean databases;
  private boolean collections;
  private boolean runCommand;
  private boolean commands;
  private String cluster;
  private String db;
  private String collection;
  private String object;
  private Map<String, String> query = new HashMap<>();
  private boolean users;

  private ApiPathBuilder() {}

  public static ApiPathBuilder start() {
    return new ApiPathBuilder();
  }

  public ApiPathBuilder cluster(final String clusterId) {
    setCluster(clusterId);
    return this;
  }

  public ApiPathBuilder db(final String dbName) {
    setDb(dbName);
    return this;
  }

  public ApiPathBuilder collection(final String collectionName) {
    setCollection(collectionName);
    return this;
  }

  public ApiPathBuilder object(final String objectId) {
    setObject(objectId);
    return this;
  }

  public ApiPathBuilder clusters() {
    setClusters(true);
    return this;
  }

  public ApiPathBuilder databases() {
    setDatabases(true);
    return this;
  }

  public ApiPathBuilder runCommand() {
    setRunCommand(true);
    return this;
  }

  public ApiPathBuilder collections() {
    setCollections(true);
    return this;
  }

  public ApiPathBuilder commands() {
    setCommands(true);
    return this;
  }

  public ApiPathBuilder users() {
    setUsers(true);
    return this;
  }

  public ApiPathBuilder query(final String key, final Object value) {
    query.put(key, value.toString());
    return this;
  }

  public String toString() {
    if (getClusters()) {
      final StringBuilder sb = new StringBuilder();
      sb.append("/clusters");
      if (getCluster() != null) {
        sb.append("/").append(getCluster()).append(getDbPath());
      }
      return sb.toString();
    } else {
      return getDbPath();
    }
  }

  private String getDbPath() {
    final StringBuilder sb = new StringBuilder();
    if (getDatabases()) {
      sb.append("/databases");
      if (getDb() != null) {
        sb.append("/").append(getDb());
        if (getCollections()) {
          sb.append("/collections");
          if (getCollection() != null) {
            sb.append("/").append(getCollection());
            if (getObject() != null) {
              sb.append("/").append(getObject());
            }
          }
        } else if(getRunCommand()) {
          sb.append("/runCommand");
        } else if(getCommands()) {
          sb.append("/commands");
        } else if(getUsers()) {
          sb.append("/users");
        }
      }
    } else if(getRunCommand()) {
      sb.append("/runCommand");
    }
    if (!getQuery().isEmpty()) {
      sb.append("?");
      getQuery()
          .forEach(
              (key, value) ->
                  sb.append(key)
                      .append("=")
                      .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                      .append("&"));
    }
    return sb.toString();
  }

  private boolean getClusters() {
    return getCluster() != null || clusters;
  }

  private void setClusters(final boolean pClusters) {
    clusters = pClusters;
  }

  private boolean getDatabases() {
    return getDb() != null || databases;
  }

  private void setDatabases(final boolean pDatabases) {
    databases = pDatabases;
  }

  private boolean getCollections() {
    return getCollection() != null || collections;
  }

  private void setCollections(final boolean pCollections) {
    collections = pCollections;
  }

  private boolean getRunCommand() {
    return runCommand;
  }

  private void setRunCommand(final boolean pRunCommand) {
    runCommand = pRunCommand;
  }

  public boolean getCommands() {
    return commands;
  }

  public void setCommands(final boolean pCommands) {
    commands = pCommands;
  }

  public boolean getUsers() {
    return users;
  }

  public void setUsers(final boolean pUsers) {
    users = pUsers;
  }

  private String getCluster() {
    return cluster;
  }

  private void setCluster(final String pCluster) {
    cluster = pCluster;
  }

  private String getDb() {
    return db;
  }

  private void setDb(final String pDb) {
    db = pDb;
  }

  private String getCollection() {
    return collection;
  }

  private void setCollection(final String pCollection) {
    collection = pCollection;
  }

  private String getObject() {
    return object;
  }

  private void setObject(final String pObject) {
    object = pObject;
  }

  private Map<String, String> getQuery() {
    return query;
  }
}

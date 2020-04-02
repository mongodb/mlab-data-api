package com.mlab.mongodb;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class SecurityUtils {

  public static List<BSONObject> findJavaScriptInQuery(final BSONObject query) {
    return findWhereClausesInQuery(query);
  }

  public static List<BSONObject> findJavaScriptInCommand(final BSONObject command) {
    List<BSONObject> clauses = null;
    if (command != null) {

      // Covers findAndModify, distinct, count(?), geoNear
      if (command.containsField("query")) {
        clauses = findJavaScriptInQuery((BSONObject) command.get("query"));
      }

      // eval, mapReduce, group always contain JS so can never be used
      if (command.containsField("eval")) {
        clauses = addClause(clauses, "eval", command.get("eval"));
      }
      if (command.containsField("mapReduce")) {
        clauses = conditionallyAddClauses(clauses, command, "map", "reduce", "finalize");
      }
      if (command.containsField("group")) {
        clauses = conditionallyAddClauses(clauses, command, "reduce", "keyf", "finalize");
        if (command.containsField("cond")) {
          clauses = addAll(clauses, findJavaScriptInQuery((BSONObject) command.get("cond")));
        }
      }
    }
    return clauses;
  }

  public static List<BSONObject> findWhereClausesInQuery(final BSONObject query) {
    return findClausesInQuery(query, "$where");
  }

  public static List<BSONObject> findClausesInQuery(
      final BSONObject query, final String... fields) {
    if (fields == null) {
      return null;
    }
    // Turn the array into a list for performance reasons (we're doing a lot of contains() calls)
    return findClausesInQuery(query, Arrays.asList(fields));
  }

  public static List<BSONObject> findClausesInQuery(
      final BSONObject query, final Collection<String> fields) {
    if (query == null) {
      return null;
    }
    List<BSONObject> clauses = null;
    for (final String f : query.keySet()) {
      if (fields.contains(f)) {
        clauses = addClause(clauses, f, query.get(f));
      } else {
        final Object value = query.get(f);
        if (value instanceof BSONObject) {
          addAll(clauses, findClausesInQuery((BSONObject) value, fields));
        }
      }
    }
    return clauses;
  }

  protected static List<BSONObject> conditionallyAddClauses(
      List<BSONObject> l, final BSONObject o, final String... fields) {
    if (fields != null && o != null) {
      for (final String f : fields) {
        if (o.containsField(f)) {
          l = addClause(l, f, o.get(f));
        }
      }
    }
    return l;
  }

  protected static List<BSONObject> addClause(
      List<BSONObject> l, final String field, final Object value) {
    if (l == null) {
      l = new LinkedList<>();
    }
    l.add(new BasicBSONObject(field, value));
    return l;
  }

  protected static List<BSONObject> addAll(final List<BSONObject> l1, final List<BSONObject> l2) {
    if (l1 == null || l1.size() == 0) {
      return l2;
    }
    if (l2 == null || l2.size() == 0) {
      return l1;
    }
    l1.addAll(l2);
    return l1;
  }
}

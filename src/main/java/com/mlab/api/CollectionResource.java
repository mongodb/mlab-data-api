package com.mlab.api;

import static com.mongodb.client.model.Filters.eq;

import com.mlab.http.HttpMethod;
import com.mlab.mongodb.MongoUtils;
import com.mlab.mongodb.SecurityUtils;
import com.mlab.ns.Uri;
import com.mlab.ws.RequestContext;
import com.mlab.ws.Resource;
import com.mlab.ws.ResourceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.WriteConcernException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;

public class CollectionResource extends PortalRESTResource {

  private static final String[] methods =
      new String[] {HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name()};

  private static final int DEFAULT_LIMIT = 1000;

  private static final String QUERY_PARAM = "q";

  private static final String FIELDS_PARAM = "f";

  private static final String SORT_ORDER_PARAM = "s";

  private static final String SKIP_PARAM = "sk";

  private static final String LIMIT_PARAM = "l";

  private static final String FIND_ONE_PARAM = "fo";

  private static final String MULTI_PARAM = "m";

  private static final String UPSERT_PARAM = "u";

  private static final String COUNT_PARAM = "c";
  private MongoCollection<Document> collection;

  CollectionResource(final MongoCollection<Document> c) {
    super();
    setCollection(c);
  }

  public String[] getMethods() {
    return methods;
  }

  public String getName() {
    return getCollection().getNamespace().getCollectionName();
  }

  public MongoCollection<Document> getCollection() {
    return collection;
  }

  private void setCollection(final MongoCollection<Document> value) {
    collection = value;
  }

  @Override
  protected Object handleGet(final Map parameters, final RequestContext context)
      throws ResourceException {
    try {
      final BasicDBObject query = getQuery(parameters);
      final BasicDBObject fields = getFields(parameters);
      final BasicDBObject sortOrder = getSortOrder(parameters);
      final int skip = getSkip(parameters);
      final int limit = getLimit(parameters);
      final boolean findOne = getFindOne(parameters);
      final boolean count = getCount(parameters);
      final MongoCollection c = getCollection();
      return doQuery(c, query, fields, sortOrder, skip, limit, findOne, count);
    } catch (final IllegalArgumentException e) {
      if (e.getMessage().startsWith("bad data.")) {
        throw new ResourceException(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Bad data detected in query response. Try repairing the database.",
            e);
      }
      throw e;
    }
  }

  @Override
  public void servicePost(final HttpServletRequest request, final HttpServletResponse response) {
    if (MongoUtils.isCollectionReadOnly(getCollection())) {
      throw new ResourceException(HttpServletResponse.SC_FORBIDDEN);
    }

    final Object object = getJsonFromRequestBody(request);
    BasicDBObject result = null;
    final RequestContext context = makeRequestContext(request, response);
    if (object instanceof List) {
      result = handlePostList((List<DBObject>) object, context);
    } else if (object instanceof DBObject) {
      result = handlePostObject((BasicDBObject) object, context);
    } else if (object != null) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Invalid payload, expecting either a JSON object or a list of JSON objects: "
              + request.getAttribute("__rawBody"));
    }

    processResponse(result, request, response);
  }

  private BasicDBObject handlePostObject(final BasicDBObject object, final RequestContext context)
      throws ResourceException {
    if (object != null) {
      try {
        final Object id = object.get("_id") == null ? new ObjectId() : object.get("_id");
        object.put("_id", id);
        getCollection()
            .updateOne(
                eq("_id", id), new BasicDBObject("$set", object), new UpdateOptions().upsert(true));
      } catch (final IllegalArgumentException e) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Invalid object " + object + ": " + e.getMessage(),
            e);
      } catch (final DuplicateKeyException e) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Unique index constraint violated, duplicate key found trying to insert object "
                + object
                + ": "
                + e.getMessage(),
            e);
      } catch (final WriteConcernException e) {
        throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), e);
      }
    }
    return object;
  }

  private BasicDBObject handlePostList(final List<DBObject> objects, final RequestContext context)
      throws ResourceException {
    if (objects != null) {
      try {
        getCollection()
            .insertMany(
                objects.stream().map(o -> new Document(o.toMap())).collect(Collectors.toList()));
        return new BasicDBObject("n", objects.size());
      } catch (final IllegalArgumentException e) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST, "Invalid objects: " + e.getMessage(), e);
      } catch (final DuplicateKeyException e) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Unique index constraint violated, duplicate key found trying to insert objects: "
                + e.getMessage(),
            e);
      } catch (final WriteConcernException e) {
        throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), e);
      } catch (final Exception e) {
        throw new ResourceException(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "An unexpected error occurred while attempting to insert objects: " + e.getMessage(),
            e);
      }
    }
    return null;
  }

  @Override
  public void servicePut(final HttpServletRequest request, final HttpServletResponse response) {
    if (MongoUtils.isCollectionReadOnly(getCollection())) {
      throw new ResourceException(HttpServletResponse.SC_FORBIDDEN);
    }

    final Object object = getJsonFromRequestBody(request);
    Object result = null;
    final RequestContext context = makeRequestContext(request, response);
    if (object instanceof List) {
      result = handlePutList((List<DBObject>) object, context);
    } else if (object instanceof DBObject) {
      result = handlePutObject((BasicDBObject) object, context);
    } else if (object != null) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Invalid payload, expecting either a JSON object or a list of JSON objects: "
              + request.getAttribute("__rawBody"));
    }

    processResponse(result, request, response);
  }

  private BasicDBObject asUpdate(final BasicDBObject maybeUpdate) {
    // if maybeUpdate does not look like an update statement
    if (maybeUpdate != null
        && !maybeUpdate.isEmpty()
        && !maybeUpdate.keySet().iterator().next().startsWith("$")) {
      // change it into one
      return new BasicDBObject("$set", maybeUpdate);
    }
    return maybeUpdate;
  }

  private Object handlePutObject(final BasicDBObject object, final RequestContext context)
      throws ResourceException {
    try {

      final Map parameters = getParameters(context.getServletRequest());
      final BasicDBObject query = getQuery(parameters);
      final boolean multi = getMulti(parameters);
      final boolean upsert = getUpsert(parameters);
      final MongoCollection c = getCollection();
      final UpdateOptions options = new UpdateOptions().upsert(upsert);

      validateQuery(query);
      if (multi) {
        return updateResultToDBObject(c.updateMany(query, object, options));
      } else {
        return updateResultToDBObject(c.updateOne(query, asUpdate(object), options));
      }
    } catch (final IllegalArgumentException e) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          String.format("Invalid object %s: %s", object, e.getMessage()),
          e);
    } catch (final DuplicateKeyException e) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          String.format(
              "Unique index constraint violated, duplicate key found trying to update object %s: %s",
              object, e.getMessage()),
          e);
    }
  }

  private Object handlePutList(final List<DBObject> objects, final RequestContext context)
      throws ResourceException {
    try {
      final MongoCollection c = getCollection();

      final Map parameters = getParameters(context.getServletRequest());
      BasicDBObject query = getQuery(parameters);
      if (query == null) {
        // remove everything
        query = new BasicDBObject();
      } else {
        validateQuery(query);
      }
      final DeleteResult dr = c.deleteMany(query);

      // insert all objects
      if (objects != null && !objects.isEmpty()) {
        c.insertMany(
            objects.stream().map(o -> new Document(o.toMap())).collect(Collectors.toList()));
      }

      final DBObject result = new BasicDBObject();
      result.put("n", objects == null ? 0 : objects.size());
      result.put("removed", dr.getDeletedCount());
      return result;

    } catch (final IllegalArgumentException e) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST, "Invalid objects: " + e.getMessage(), e);
    } catch (final DuplicateKeyException e) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Unique index constraint violated, duplicate key found trying to insert objects: "
              + e.getMessage(),
          e);
    } catch (final Exception e) {
      throw new ResourceException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while attempting to insert objects: " + e.getMessage(),
          e);
    }
  }

  public Resource resolveRelative(final Uri uri) {
    final String id = uri.toString();
    final DBObject o = MongoUtils.findOneByStringId(getCollection(), id);
    final Resource result = new ObjectResource(o);
    result.setName(id);
    result.setParent(this);
    return result;
  }

  private BasicDBObject getQuery(final Map params) throws ResourceException {
    return getJSONParam(params.get(QUERY_PARAM), BasicDBObject.class, new BasicDBObject());
  }

  private BasicDBObject getFields(final Map params) throws ResourceException {
    return getJSONParam(params.get(FIELDS_PARAM), BasicDBObject.class, null);
  }

  private BasicDBObject getSortOrder(final Map params) throws ResourceException {
    return getJSONParam(params.get(SORT_ORDER_PARAM), BasicDBObject.class, null);
  }

  private <T> T getJSONParam(final Object value, final Class<T> targetType, final T defaultValue)
      throws ResourceException {
    final Object param = getJSONParam(value, defaultValue);
    if (param != null && !targetType.isInstance(param)) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Could not parse JSON parameter, please double-check syntax and encoding: " + value);
    }
    return targetType.cast(param);
  }

  private Object getJSONParam(final Object value, final Object defaultValue)
      throws ResourceException {
    if (value == null || value.equals("")) {
      return defaultValue;
    }
    try {
      return JSON.parse(value.toString());
    } catch (final Exception e) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Could not parse JSON parameter, please double-check syntax and encoding: "
              + value
              + ": "
              + e.getMessage(),
          e);
    }
  }

  private int getSkip(final Map params) throws ResourceException {
    final String param = (String) params.get(SKIP_PARAM);
    if (param == null) {
      return 0;
    }
    try {
      return Integer.parseInt(param);
    } catch (final NumberFormatException nfe) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Could not parse '" + param + "' into an integer.",
          nfe);
    }
  }

  private int getLimit(final Map params) throws ResourceException {
    final String param = (String) params.get(LIMIT_PARAM);
    if (param == null) {
      return DEFAULT_LIMIT;
    }
    try {
      final int limit = Integer.parseInt(param);
      if (limit <= 0) {
        return DEFAULT_LIMIT;
      }
      return limit;
    } catch (final NumberFormatException nfe) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Could not parse '" + param + "' into an integer.",
          nfe);
    }
  }

  private boolean getFindOne(final Map params) {
    final String param = (String) params.get(FIND_ONE_PARAM);
    if (param == null) {
      return false;
    }
    return Boolean.parseBoolean(param);
  }

  private boolean getMulti(final Map params) {
    final String param = (String) params.get(MULTI_PARAM);
    if (param == null) {
      return false;
    }
    return Boolean.parseBoolean(param);
  }

  private boolean getUpsert(final Map params) {
    final String param = (String) params.get(UPSERT_PARAM);
    if (param == null) {
      return false;
    }
    return Boolean.parseBoolean(param);
  }

  private boolean getCount(final Map params) {
    final String param = (String) params.get(COUNT_PARAM);
    if (param == null) {
      return false;
    }
    return Boolean.parseBoolean(param);
  }

  private Object doQuery(
      final MongoCollection collection,
      final BasicDBObject query,
      final BasicDBObject fields,
      final BasicDBObject sortOrder,
      final int skip,
      final int limit,
      final boolean findOne,
      final boolean count)
      throws ResourceException {
    validateQuery(query);
    try {
      if (findOne) {
        final Collection result =
            collection
                .find(query)
                .projection(fields)
                .sort(sortOrder)
                .limit(1)
                .map(o -> new BasicDBObject((Document) o))
                .into(new ArrayList());
        if (count) {
          return result.size();
        }
        return result.isEmpty() ? null : result.iterator().next();
      } else if (count) {
        return collection.countDocuments(query);
      } else {
        return collection
            .find(query)
            .sort(sortOrder)
            .projection(fields)
            .skip(skip)
            .limit(limit)
            .map(o -> new BasicDBObject((Document) o))
            .into(new ArrayList());
      }
    } catch (final MongoException e) {
      if (e.getMessage().startsWith("invalid operator:")) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Invalid operator "
                + e.getMessage().substring("invalid operator: ".length())
                + " in query: "
                + query,
            e);
      } else if (e.getMessage().startsWith("invalid query")) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST, "Invalid query: " + query, e);
      }
      throw e;
    }
  }

  private void validateQuery(final BSONObject q) {
    final List<BSONObject> js = SecurityUtils.findJavaScriptInQuery(q);
    if (js != null) {
      throw new ResourceException(
          HttpServletResponse.SC_BAD_REQUEST,
          "mLab does not support JavaScript via the API. " + js);
    }
  }

  private DBObject updateResultToDBObject(final UpdateResult ur) {
    final DBObject result = new BasicDBObject();
    if (ur.getUpsertedId() != null) {
      result.put("n", 1);
    } else {
      result.put("n", ur.getModifiedCount());
    }
    return result;
  }
}

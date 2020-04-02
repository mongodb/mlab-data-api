package com.mlab.api;

import static com.mongodb.client.model.ReturnDocument.AFTER;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import com.mlab.http.HttpMethod;
import com.mlab.mongodb.MongoUtils;
import com.mlab.ws.RequestContext;
import com.mlab.ws.ResourceException;

public class ObjectResource extends PortalRESTResource {

  private static final String[] METHODS =
      new String[] {HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name()};
  private DBObject _object;

  public ObjectResource(final DBObject o) {
    super();
    setObject(o);
  }

  public String[] getMethods() {
    return METHODS;
  }

  public String getName() {
    final DBObject o = getObject();
    return o == null ? null : o.get("_id").toString();
  }

  public DBObject getObject() {
    return _object;
  }

  public void setObject(final DBObject value) {
    _object = value;
  }

  @Override
  public Object handleGet(final Map parameters, final RequestContext context)
      throws ResourceException {
    if (getObject() == null) {
      throw new ResourceException(HttpServletResponse.SC_NOT_FOUND, "Document not found");
    }
    return getObject();
  }

  @Override
  public Object handlePut(final Object o, final RequestContext context) throws ResourceException {
    final BasicDBObject update = (BasicDBObject) o;
    if (MongoUtils.isCollectionReadOnly(getCollection())) {
      throw new ResourceException(HttpServletResponse.SC_FORBIDDEN);
    }
    if (update != null) {
      final CollectionResource parent = (CollectionResource) getParent();
      final MongoCollection<Document> c = parent.getCollection();
      try {
        final BasicDBObject idQuery = new BasicDBObject("_id", getObject().get("_id"));
        if (isUpdateDirective(update)) {
          final Document updated =
              c.findOneAndUpdate(
                  idQuery, update, new FindOneAndUpdateOptions().returnDocument(AFTER));
          return new BasicDBObject(updated);
        } else {
          final Document updated =
              c.findOneAndReplace(
                  idQuery,
                  new Document(update),
                  new FindOneAndReplaceOptions().returnDocument(AFTER));
          return new BasicDBObject(updated);
        }
      } catch (final IllegalArgumentException e) {
        throw new ResourceException(
            HttpServletResponse.SC_BAD_REQUEST,
            "Invalid object " + update + " - " + e.getMessage());
      }
    } else {
      throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST, "Update object is missing.");
    }
  }

  @Override
  public Object handleDelete(final RequestContext context) throws ResourceException {
    if (MongoUtils.isCollectionReadOnly(getCollection())) {
      throw new ResourceException(HttpServletResponse.SC_FORBIDDEN);
    }
    final DBObject object = getObject();
    if (object != null) {
      final CollectionResource parent = (CollectionResource) getParent();
      final MongoCollection c = parent.getCollection();
      c.deleteOne(new BasicDBObject("_id", object.get("_id")));
    } else {
      throw new ResourceException(HttpServletResponse.SC_NOT_FOUND, "Document not found");
    }
    return object;
  }

  protected MongoCollection<Document> getCollection() {
    return ((CollectionResource) getParent()).getCollection();
  }

  private boolean isUpdateDirective(final DBObject object) {
    return !object.keySet().isEmpty() && object.keySet().iterator().next().startsWith("$");
  }
}

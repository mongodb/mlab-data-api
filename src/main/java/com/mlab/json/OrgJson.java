package com.mlab.json;

import com.mongodb.DBObject;
import java.util.Collection;
import org.bson.BSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OrgJson {

    public static Object toOrgJson ( Object json ) {
        if ( json == null ) {
            return null;
        }
        try {
            if ( json instanceof Collection || json.getClass().isArray() ) {
                return new JSONArray(json.toString());
            } else if ( json instanceof BSONObject) {
                return new JSONObject(json.toString());
            }
        } catch ( JSONException e ) {
            throw new IllegalArgumentException(e);
        }
        throw new UnsupportedOperationException("Cannot convert type: " + json.getClass().getName());
    }

    public static DBObject fromOrgJson (JSONObject o ) {
        if ( o == null ) {
            return null;
        }
        return (DBObject)getJsonParser().parse(o.toString());
    }

    private static JsonParser mJsonParser = new JsonParser();
    protected static JsonParser getJsonParser ( )
    { return mJsonParser; }

}

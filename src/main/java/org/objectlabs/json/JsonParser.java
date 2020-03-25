package org.objectlabs.json;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.Writer;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.BSONObject;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonParser {

    public JsonParser() {
        // empty constructor
    }

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);


    public JsonParser(DB db ) {
        setDb(db);
    }

    private DB mDb;
    public DB getDb ( )
    { return mDb; }
    public void setDb ( DB db )
    { mDb = db; }

    public Object parse(String json) {
        return(parse(new StringReader(json)));
    }

    public Object parse(Reader r) {
        return read(makeJsonTokenizer(r));
    }

    private StreamTokenizer makeJsonTokenizer(Reader r) {
        StreamTokenizer tokenizer = new StreamTokenizer(r);

        tokenizer.eolIsSignificant(false);
        tokenizer.slashStarComments(false);
        tokenizer.slashSlashComments(false);
        tokenizer.parseNumbers();
        tokenizer.commentChar('#');
        tokenizer.wordChars('_', '_');
        tokenizer.wordChars('@', '@');

        return tokenizer;
    }

    public static Object mongoParse(BufferedReader in) throws JsonParseException {
        String r;
        String body = "";
        try {
            while((r = in.readLine()) != null) {
                body += r;
            }
        } catch (IOException e) {
            throw new JsonParseException(
                    "Unable to read buffer contents: "+e.getMessage(),
                    e);
        }
        if(body.isEmpty()) {
            return null;
        } else {
            return mongoParse(body);
        }
    }

    public static Object mongoParse(String json) throws JsonParseException {
        try {
            return JSON.parse(json);
        } catch(Exception e) {
            throw new JsonParseException(e.getMessage(), e);
        }
    }

    public static Object mongoParse(String json, DB db) throws JsonParseException {
        try {
            return JSON.parse(json);
        } catch(Exception e) {
            throw new JsonParseException(e.getMessage(), e);
        }
    }

    private Object read(StreamTokenizer in) {
        Object result = null;

        int c = readToken(in);
        switch (c) {

            case StreamTokenizer.TT_EOF:
                break;
            case StreamTokenizer.TT_NUMBER:
                result = readNumber(in);
                break;
            case StreamTokenizer.TT_WORD:
                result = readWord(in);
                break;
            case '\'':
            case '\"':
                result = readString(in);
                break;
            case '[':
                result = readArray(in);
                break;
            case '{':
                result = readObject(in);
                DBObject objResult = (DBObject)result;
                if ( objResult != null &&
                     getDb() != null &&
                     objResult.containsField("$ref") &&
                     objResult.containsField("$id") &&
                     objResult.keySet().size() == 2 ) {

                    Object id = objResult.get("$id");
                    if (id instanceof BSONObject && ((BSONObject)id).containsField("$oid")) {
                        id = new ObjectId((String)((BSONObject)id).get("$oid"));
                    }
                    result = new DBRef((String)objResult.get("$ref"), id);

                }
                break;

        }

        return(result);
    }

    private Number readNumber(StreamTokenizer in) {
        double d = in.nval;

        if (d == (long)d) {
            return(new Long((long)d));
        }

        return(new Double(in.nval));
    }

    private Object readWord(StreamTokenizer in) {
        Object result = null;

        String word = in.sval;
        if (word.equals("null")) {
            result = null;
        } else if (word.equals("true")) {
            result = Boolean.TRUE;
        } else if (word.equals("false")) {
            result = Boolean.FALSE;
        } else {
            throw(new JsonParseException("Unexpected token: '" + word +
                                         "' on line: " + in.lineno()));
        }

        return(result);
    }

    private String readString(StreamTokenizer in) {
        return(in.sval);
    }

    private List readArray(StreamTokenizer in) {
        BasicDBList result = new BasicDBList();

        while (peek(in) != ']') {
            result.add(read(in));
            int next = peek(in);
            if (next == ',') {
                readToken(in, ',');
            }
        }
        readToken(in, ']');

        return(result);
    }

    private DBObject readObject(StreamTokenizer in) {
        DBObject result = new BasicDBObject();

        boolean first = true;
        while (peek(in) != '}') {
            String fieldName = readFieldName(in);
            readToken(in, ':');
            Object fieldValue = read(in);

            result.put(fieldName, fieldValue);
            int next = peek(in);
            if (next == ',') {
                readToken(in, ',');
            }
        }
        readToken(in, '}');

        return(result);
    }

    private String readFieldName(StreamTokenizer in) {
        String result = null;

        int c = readToken(in);
        switch (c) {
            case StreamTokenizer.TT_WORD:
                result = in.sval;
                break;
            case '\'':
            case '\"':
                result = readString(in);
                break;
            default:
                throw(new JsonParseException("Unexpected token: '" + (char)c +
                                             "' on line: " + in.lineno()));
        }

        return(result);
    }

    private int readToken(StreamTokenizer in) {
        try {
            return(in.nextToken());
        } catch (IOException e) {
            throw(new JsonParseException("Unexpected IOException at: " +
                                         in.lineno()));
        }
    }

    private int readToken(StreamTokenizer in, int c) {
        if (peek(in) != c) {
            throw(new JsonParseException("Expecting: '" + (char)c + "' but " +
                                         "enountered: '" + (char)peek(in) +
                                         "' " + "on line: " + in.lineno()));
        }

        return(readToken(in));
    }

    private int peek(StreamTokenizer in) {
        int result = readToken(in);
        in.pushBack();
        return(result);
    }

    public static BasicDBObject constructExtendedJsonFromBinary(byte[] binData) {
        Binary binDataType = new Binary(binData);
        byte type = binDataType.getType();
        byte[] data = binDataType.getData();

        return constructExtendedJsonFromBinary(type, data);
    }

    public static BasicDBObject constructExtendedJsonFromBinary(byte type, byte[] data) {

        String base64Data = Base64.getEncoder().encodeToString(data);
        String hexType = String.format("%02X", type);

        BasicDBObject constructedBinaryDataExtendedJson = new BasicDBObject();
        constructedBinaryDataExtendedJson.put("$binary", base64Data);
        constructedBinaryDataExtendedJson.put("$type", hexType);

        return constructedBinaryDataExtendedJson;
    }

    private static Object deepMap(final Object o, final Function<Object, Object> f) {
        if(o == null) {
            return null;
        }
        if(o instanceof DBObject) {
            final DBObject obj = (DBObject)o;
            for(final String key : obj.keySet()) {
                obj.put(key, deepMap(obj.get(key), f));
            }
            return obj;
        }
        if(o instanceof List) {
            return ((List)o).stream().map(e -> deepMap(e, f)).collect(Collectors.toList());
        }
        if(o instanceof JSONObject) {
            final DBObject obj = new BasicDBObject();
            for(final String key : ((JSONObject) o).keySet()) {
                obj.put(key, deepMap(((JSONObject) o).get(key), f));
            }
            return obj;
        }
        if(o instanceof JSONArray) {
            return ((JSONArray)o).toList().stream().map(e -> deepMap(e, f)).collect(Collectors.toList());
        }
        if(o instanceof Map) {
            final DBObject obj = new BasicDBObject();
            for(final Object key : ((Map) o).keySet()) {
                obj.put(key.toString(), deepMap(((Map) o).get(key), f));
            }
            return obj;
        }
        return f.apply(o);
    }

    public static Object convertBinaryTypesToJson(final Object o) {
        return deepMap(o, v -> {
            if(v instanceof byte[]) {
                return constructExtendedJsonFromBinary((byte[])v);
            }
            if(v instanceof Binary) {
                final Binary b = (Binary)v;
                return constructExtendedJsonFromBinary(b.getType(), b.getData());
            }
            return v;
        });
    }

    public static Object convertTimestamps(final Object o) {
        return deepMap(o, v -> {
            if(v instanceof BsonTimestamp) {
                final BsonTimestamp t = (BsonTimestamp)v;
                return new BSONTimestamp(t.getTime(), t.getInc());
            }
            return v;
        });
    }

    public String serialize(final Object o) {
        return JSON.serialize(
            convertTimestamps(
                convertBinaryTypesToJson(o)));
    }

    public void serialize(Object o, Writer w) throws IOException {
        if ( o instanceof List ) {
            w.write("[ ");
            Iterator iter = ((List)o).iterator();
            while ( iter.hasNext() ) {
                w.write(serialize(iter.next()));
                if ( iter.hasNext() ) {
                    w.write(" , ");
                }
            }
            w.write(" ]");
        } else if ( o instanceof DBCursor) {
            DBCursor c = (DBCursor)o;
            w.write("[ ");
            while ( c.hasNext() ) {
                w.write(serialize(c.next()));
                if ( c.hasNext() ) {
                    w.write(" , ");
                }
            }
            w.write(" ]");
        } else {
            w.write(serialize(o));
        }
    }

    public Object jsonify(final Object o) {
        return parse(serialize(o));
    }

    public static String prettyPrint(Object o, int indentFactor) {
        if ( o == null ) {
            return null;
        }
        Object orgJson = OrgJson.toOrgJson(o);
        try {
            if ( orgJson instanceof JSONArray ) {
                return ((JSONArray)orgJson).toString(indentFactor);
            } else if ( orgJson instanceof JSONObject ) {
                return ((JSONObject)orgJson).toString(indentFactor);
            }
        } catch ( JSONException e ) {
            throw new IllegalArgumentException(e);
        }
        throw new UnsupportedOperationException("Cannot convert type: " + o.getClass().getName());
    }

    public static void deepRemoveField(String field, Object dbo) {
        deepRemoveField(field, dbo, true);
    }
    public static void deepRemoveField(String[] fields, Object dbo) {
        deepRemoveField(fields, dbo, true);
    }
    public static void deepRemoveField(String field, Object dbo, boolean deep) {
        String [] toRemove = {field};
        deepRemoveField(toRemove, dbo, deep);
    }
    public static void deepRemoveField(String[] fields, Object dbo, boolean deep) {
        int depth = deep ? Integer.MAX_VALUE : 1;
        deepRemoveFieldHelper(fields, dbo, depth, deep);
    }

    private static void deepRemoveFieldHelper(String[] fields, Object dbo, int depth, boolean deep) {
        if (deep || depth > 0) {
            if (dbo instanceof BasicDBObject) {
                BasicDBObject basic = (BasicDBObject)dbo;

                for (String field: fields) {
                    basic.removeField(field);
                }

                for (String key: basic.keySet()) {
                    deepRemoveFieldHelper(fields, basic.get(key), depth - 1, deep);
                }
            }
            else if (dbo instanceof List) {
                List dbl = (List)dbo;
                for (Object listItem: dbl) {
                    deepRemoveFieldHelper(fields, listItem, depth - 1, deep);
                }
            }
        }
    }

}

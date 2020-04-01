package com.mlab.json;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
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
import org.bson.BsonTimestamp;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParser {

  public JsonParser() {}

  public static Object mongoParse(final String json) throws JsonParseException {
    try {
      return JSON.parse(json);
    } catch (final Exception e) {
      throw new JsonParseException(e.getMessage(), e);
    }
  }

  public static BasicDBObject constructExtendedJsonFromBinary(final byte[] binData) {
    final Binary binDataType = new Binary(binData);
    final byte type = binDataType.getType();
    final byte[] data = binDataType.getData();

    return constructExtendedJsonFromBinary(type, data);
  }

  public static BasicDBObject constructExtendedJsonFromBinary(final byte type, final byte[] data) {

    final String base64Data = Base64.getEncoder().encodeToString(data);
    final String hexType = String.format("%02X", type);

    final BasicDBObject constructedBinaryDataExtendedJson = new BasicDBObject();
    constructedBinaryDataExtendedJson.put("$binary", base64Data);
    constructedBinaryDataExtendedJson.put("$type", hexType);

    return constructedBinaryDataExtendedJson;
  }

  private static Object deepMap(final Object o, final Function<Object, Object> f) {
    if (o == null) {
      return null;
    }
    if (o instanceof DBObject) {
      final DBObject obj = (DBObject) o;
      for (final String key : obj.keySet()) {
        obj.put(key, deepMap(obj.get(key), f));
      }
      return obj;
    }
    if (o instanceof List) {
      return ((List) o).stream().map(e -> deepMap(e, f)).collect(Collectors.toList());
    }
    if (o instanceof JSONObject) {
      final DBObject obj = new BasicDBObject();
      for (final String key : ((JSONObject) o).keySet()) {
        obj.put(key, deepMap(((JSONObject) o).get(key), f));
      }
      return obj;
    }
    if (o instanceof JSONArray) {
      return ((JSONArray) o).toList().stream().map(e -> deepMap(e, f)).collect(Collectors.toList());
    }
    if (o instanceof Map) {
      final DBObject obj = new BasicDBObject();
      for (final Object key : ((Map) o).keySet()) {
        obj.put(key.toString(), deepMap(((Map) o).get(key), f));
      }
      return obj;
    }
    return f.apply(o);
  }

  public static Object convertBinaryTypesToJson(final Object o) {
    return deepMap(
        o,
        v -> {
          if (v instanceof byte[]) {
            return constructExtendedJsonFromBinary((byte[]) v);
          }
          if (v instanceof Binary) {
            final Binary b = (Binary) v;
            return constructExtendedJsonFromBinary(b.getType(), b.getData());
          }
          return v;
        });
  }

  public static Object convertTimestamps(final Object o) {
    return deepMap(
        o,
        v -> {
          if (v instanceof BsonTimestamp) {
            final BsonTimestamp t = (BsonTimestamp) v;
            return new BSONTimestamp(t.getTime(), t.getInc());
          }
          return v;
        });
  }

  public Object parse(final String json) {
    return parse(new StringReader(json));
  }

  public Object parse(final Reader r) {
    return read(makeJsonTokenizer(r));
  }

  private StreamTokenizer makeJsonTokenizer(final Reader r) {
    final StreamTokenizer tokenizer = new StreamTokenizer(r);

    tokenizer.eolIsSignificant(false);
    tokenizer.slashStarComments(false);
    tokenizer.slashSlashComments(false);
    tokenizer.parseNumbers();
    tokenizer.commentChar('#');
    tokenizer.wordChars('_', '_');
    tokenizer.wordChars('@', '@');

    return tokenizer;
  }

  private Object read(final StreamTokenizer in) {
    Object result = null;

    final int c = readToken(in);
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
        break;
    }

    return result;
  }

  private Number readNumber(final StreamTokenizer in) {
    final double d = in.nval;

    if (d == (long) d) {
      return (long) d;
    }

    return in.nval;
  }

  private Object readWord(final StreamTokenizer in) {
    Object result;

    final String word = in.sval;
    if (word.equals("null")) {
      result = null;
    } else if (word.equals("true")) {
      result = Boolean.TRUE;
    } else if (word.equals("false")) {
      result = Boolean.FALSE;
    } else {
      throw new JsonParseException("Unexpected token: '" + word + "' on line: " + in.lineno());
    }

    return result;
  }

  private String readString(final StreamTokenizer in) {
    return in.sval;
  }

  private List readArray(final StreamTokenizer in) {
    final BasicDBList result = new BasicDBList();

    while (peek(in) != ']') {
      result.add(read(in));
      final int next = peek(in);
      if (next == ',') {
        readToken(in, ',');
      }
    }
    readToken(in, ']');

    return result;
  }

  private DBObject readObject(final StreamTokenizer in) {
    final DBObject result = new BasicDBObject();

    while (peek(in) != '}') {
      final String fieldName = readFieldName(in);
      readToken(in, ':');
      final Object fieldValue = read(in);

      result.put(fieldName, fieldValue);
      final int next = peek(in);
      if (next == ',') {
        readToken(in, ',');
      }
    }
    readToken(in, '}');

    return result;
  }

  private String readFieldName(final StreamTokenizer in) {
    String result;

    final int c = readToken(in);
    switch (c) {
      case StreamTokenizer.TT_WORD:
        result = in.sval;
        break;
      case '\'':
      case '\"':
        result = readString(in);
        break;
      default:
        throw new JsonParseException(
            "Unexpected token: '" + (char) c + "' on line: " + in.lineno());
    }

    return result;
  }

  private int readToken(final StreamTokenizer in) {
    try {
      return in.nextToken();
    } catch (final IOException e) {
      throw new JsonParseException("Unexpected IOException at: " + in.lineno());
    }
  }

  private int readToken(final StreamTokenizer in, final int c) {
    if (peek(in) != c) {
      throw new JsonParseException(
          "Expecting: '"
              + (char) c
              + "' but "
              + "enountered: '"
              + (char) peek(in)
              + "' "
              + "on line: "
              + in.lineno());
    }

    return readToken(in);
  }

  private int peek(final StreamTokenizer in) {
    final int result = readToken(in);
    in.pushBack();
    return result;
  }

  public String serialize(final Object o) {
    return JSON.serialize(convertTimestamps(convertBinaryTypesToJson(o)));
  }

  public void serialize(final Object o, final Writer w) throws IOException {
    if (o instanceof List) {
      w.write("[ ");
      final Iterator iter = ((List) o).iterator();
      while (iter.hasNext()) {
        w.write(serialize(iter.next()));
        if (iter.hasNext()) {
          w.write(" , ");
        }
      }
      w.write(" ]");
    } else if (o instanceof DBCursor) {
      final DBCursor c = (DBCursor) o;
      w.write("[ ");
      while (c.hasNext()) {
        w.write(serialize(c.next()));
        if (c.hasNext()) {
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
}

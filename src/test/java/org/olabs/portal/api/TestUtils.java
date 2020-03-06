package org.olabs.portal.api;

public class TestUtils {
  public static ApiConfig getTestApiConfig() {
    final StringBuilder raw = new StringBuilder();
    raw.append("{");
    raw.append("port: 1234,");
    raw.append("clusters: {");
    raw.append("a: 'mongodb://user:pass@host-a:27001/admin',");
    raw.append("b: 'mongodb://user:pass@host-b:27001/admin'},");
    raw.append("databases: {");
    raw.append("foo: 'mongodb://user:pass@host-c:27001/foo',");
    raw.append("bar: 'mongodb://user:pass@host-d:27001/bar'}");
    raw.append("}");
    return ApiConfig.getInstance(raw.toString());
  }
}

package org.objectlabs.ns;

public interface Namespace { // XXX should be name resolver?

  public String getName();

  public void setName(String name);

  public String getAbsoluteName();

  public Namespace getParent();

  public void setParent(Namespace value);

  public Namespace getParent(Class c);

  public Namespace getRoot();

  public Namespace resolve(String name);

  public Namespace resolve(Uri name);
}

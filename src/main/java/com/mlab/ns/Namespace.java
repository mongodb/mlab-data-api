package com.mlab.ns;

public interface Namespace {

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

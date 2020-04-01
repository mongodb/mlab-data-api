package com.mlab.ns;

public interface Namespace {

  String getName();

  void setName(String name);

  String getAbsoluteName();

  Namespace getParent();

  void setParent(Namespace value);

  Namespace getRoot();

  Namespace resolve(String name);

  Namespace resolve(Uri name);
}

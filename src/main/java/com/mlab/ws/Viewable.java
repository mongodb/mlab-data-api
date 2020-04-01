package com.mlab.ws;

public interface Viewable {

    public View getView();

    public void setView(View value);

    public View getView(String viewName);
    
}

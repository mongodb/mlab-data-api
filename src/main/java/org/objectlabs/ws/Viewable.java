package org.objectlabs.ws;

/******************************************************************************
 * Viewable
 * 
 * @author William Shulman
 *
 * 05.10.2010
 */
public interface Viewable {

    /*************************************************************************
     * view
     */
    public View getView();

    public void setView(View value);

    /*************************************************************************
     * getView
     */
    public View getView(String viewName);
    
}

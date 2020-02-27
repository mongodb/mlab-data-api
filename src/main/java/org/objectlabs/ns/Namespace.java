package org.objectlabs.ns;

/******************************************************************************
 * Namespace
 *
 * @author William Shulman
 *
 * 02.07.2010
 */
public interface Namespace { // XXX should be name resolver?

    /*******************************************************************
     * getName
     */
    public String getName();

    /*******************************************************************
     * setName
     */
    public void setName(String name);

    /*******************************************************************
     * getAbsoluteName
     */
    public String getAbsoluteName();

    /*******************************************************************
     * getParent
     */
    public Namespace getParent();

    /*******************************************************************
     * getParent
     */
    public Namespace getParent(Class c);

    /*******************************************************************
     * setParent
     */
    public void setParent(Namespace value);

    /*******************************************************************
     * getRoot
     */
    public Namespace getRoot();

    /*******************************************************************
     * resolve
     */
    public Namespace resolve(String name);

    /*******************************************************************
     * resolve
     */
    public Namespace resolve(Uri name);

}

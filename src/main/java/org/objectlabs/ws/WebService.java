package org.objectlabs.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.objectlabs.ns.Namespace;

/******************************************************************************
 * WebService
 *
 * @author William Shulman
 *
 * 05.10.2010
 */
public interface WebService extends Namespace {

    /*************************************************************************
     * service                                                               
     */
    public void service(HttpServletRequest request, HttpServletResponse response);

}

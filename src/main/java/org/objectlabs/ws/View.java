package org.objectlabs.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/******************************************************************************
 * View
 *
 * @author William Shulman
 *
 * 01.26.2010
 */
public interface View {

    /*************************************************************************
     * render
     */
    public void render(Object resourceResult,
                       Resource resource,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws WebServiceException;

}

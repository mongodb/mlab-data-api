package org.objectlabs.ws;

import java.util.ArrayList;
import java.util.List;
import org.objectlabs.http.HttpMethod;
import org.objectlabs.ns.Uri;
import org.olabs.portal.api.PortalRESTResource;

/******************************************************************************
 * DirectoryResource
 *
 * @author William Shulman
 *
 * 05.10.2010
 */
public class DirectoryResource extends PortalRESTResource {

    /*************************************************************************
     * methods
     */
    public String[] getMethods() {
        String[] result = { HttpMethod.GET.name() };
        return(result);
    }

    /*************************************************************************
     * children
     */
    private List<Resource> children;

    public List<Resource> getChildren() {
        if (children == null) {
            children = new ArrayList<Resource>();
        }
        return(children);
    }

    public void setChildren(List<Resource> value) {
        children = value;
    }

    /*************************************************************************
     * resolveRelative
     */
    public Resource resolveRelative(Uri uri) {
        String head = uri.getHead();
        for (Resource r : getChildren()) {
            if (r.getName().equals(head)) {
                return(r.resolve(uri.getTail()));
            }
        }
        return(null);
    }

}

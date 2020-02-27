package org.objectlabs.ws;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewableResource extends Resource implements Viewable {

    /*************************************************************************
     * VIEW_HTTP_REQUEST_PARAMETER_NAME
     */
    private static final String VIEW_HTTP_REQUEST_PARAMETER_NAME = "view";

    /*************************************************************************
     * logger                                                                 
     */
    private static Logger logger =
            LoggerFactory.getLogger(WebService.class);

    private static Logger getLogger() {
        return(logger);
    }

    /*************************************************************************
     * view
     */
    private View view;

    public View getView() {
        return(view);
    }

    public void setView(View value) {
        view = value;
    }

    /*************************************************************************
     * views
     */
    private Map<String, View> views;

    public Map<String, View> getViews() {
        if (views == null) {
            views = new HashMap<String, View>();
        }
        return(views);
    }

    public void setViews(Map<String, View> value) {
        views = value;
    }

    /*************************************************************************
     * getView
     */
    public View getView(String viewName) {
        View result = null;

        Map views = getViews();
        if (views != null) {
            result = (View)views.get(viewName);
        }

        return(result);
    }

    /**************************************************************************
     * service 
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
    {
        getLogger().info("service(): " + getName());

        DBObject model = new BasicDBObject("resource", this);
        render(model, request, response);
    }

    /*************************************************************************
     * render
     */
    protected void render(Object result,
                          HttpServletRequest request,
                          HttpServletResponse response)
    {
        View view = selectView(request);
        if (view != null) {
            view.render(result, this, request, response);
        } else {
            try {
                response.setContentType("text/html; charset=utf-8");
                response.getWriter().println(result.toString());
            } catch (Exception e) {
                throw(new WebServiceException(e));
            } finally {
                try {
                    response.getWriter().flush();
                } catch (Exception e) {
                    getLogger().warn(e.toString());
                }
            }
        }
    }

    /*************************************************************************
     * selectView
     */
    protected View selectView(HttpServletRequest request) {
        View result = null;

        String viewName =
                request.getParameter(VIEW_HTTP_REQUEST_PARAMETER_NAME);
        if (viewName != null) {
            result = getView(viewName);
        }

        if (result == null) {
            return(getView());
        }

        return(result);
    }

}

package org.olabs.portal.api;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.objectlabs.json.JsonParseException;
import org.objectlabs.json.JsonParser;
import org.objectlabs.ws.HttpRequestResource;
import org.objectlabs.ws.JsonView;
import org.objectlabs.ws.ResourceException;
import org.objectlabs.ws.RestResource;
import org.objectlabs.ws.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class PortalRESTResource extends RestResource {

    private static final Logger log = LoggerFactory.getLogger(PortalRESTResource.class);

    @Override
    protected String getDefaultViewName () {
        return "json";
    }

    private final JsonView mJsonView = new JsonView();
    @Override
    protected View getDefaultView () {
        return mJsonView;
    }

    public HttpServletRequest getRequest() {
        HttpRequestResource r = (HttpRequestResource)getParent(HttpRequestResource.class);
        return r == null ? null : r.getRequest();
    }

    public ApiConfig getApiConfig() {
        return ApiConfig.getInstance();
    }

    @Override
    protected Object getObjectFromRequestBody(HttpServletRequest request) {
        try {
            return parseJsonFromRequest(request, getJsonParser());
        } catch(JsonParseException e) {
            handleJsonParseException(e, request);
            return null;
        } catch(Exception e) {
            if(e instanceof ResourceException) {
                throw (ResourceException)e;
            } else {
                throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST,
                    "Unable to read JSON from request: "+e.getMessage(),
                    e);
            }
        }
    }

    private JsonParser mJsonParser = new JsonParser();
    protected JsonParser getJsonParser() {
        return(mJsonParser);
    }

    protected Object handleJsonParseException ( JsonParseException jpe, HttpServletRequest request ) {
        // TODO: Might separate out different kinds of issues parsing.
        // TODO: Would be nice to get the JSON on error. Maybe that's something the parser should handle when throwing.
        throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST,
            "Could not parse JSON, please double-check syntax and encoding: " + jpe.getMessage(),
            jpe);
    }

    protected Object getJsonFromRequestBody(HttpServletRequest request) {
        return parseJsonFromRequest(request, getJsonParser());
    }

    public static final Object parseJsonFromRequest(HttpServletRequest request, JsonParser parser) {
        boolean strictContentTypeEnforcement = true;
        String contentTypeEnforcement = request.getHeader("Content-Type-Enforcement");
        if ( contentTypeEnforcement != null ) {
            if ( contentTypeEnforcement.equalsIgnoreCase("strict") ) {
                strictContentTypeEnforcement = true;
            } else if ( contentTypeEnforcement.equalsIgnoreCase("relaxed") ) {
                strictContentTypeEnforcement = false;
            } else {
                throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST,
                    "Unrecognized Content-Type-Enforcement '" + contentTypeEnforcement +
                        "', please use one of 'strict' or 'relaxed'");
            }
        }
        String contentType = request.getContentType();
        if ( contentType == null ) {
            String requestString = request.getMethod() + " " + request.getRequestURI() +
                (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            log.warn("No Content-Type set for request: {}", requestString);
            if ( strictContentTypeEnforcement ) {
                throw new ResourceException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "No Content-Type set, expecting 'application/json'.");
            }
        } else if ( !(contentType.equals("application/json") || contentType.startsWith("application/json;")) ) {
            String requestString = request.getMethod() + " " + request.getRequestURI() +
                (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            log.warn("Unexpected Content-Type ({}) set for request: {}", contentType, requestString);
            if ( strictContentTypeEnforcement ) {
                throw new ResourceException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Unexpected Content-Type '" + contentType + "', expecting 'application/json'.");
            }
        }
        try {
            // We do this here and not in the parser so that we can remember the string of the body in case of error
            String body = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            request.setAttribute("__rawBody", body);
            if ( body.isEmpty() ) {
                return null;
            } else {
                return parser.mongoParse(body);
            }
        } catch ( UnsupportedEncodingException uee ) {
            throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST,
                "Unsupported charset encoding: "+uee.getMessage(),
                uee);
        } catch ( Exception e ) {
            throw new ResourceException(HttpServletResponse.SC_BAD_REQUEST,
                "Unable to read JSON from request: "+e.getMessage(),
                e);
        }
    }
}
package org.objectlabs.ws;

import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.objectlabs.json.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonView implements View {

    private static final Logger log = LoggerFactory.getLogger(JsonView.class);

    protected static final JsonParser jsonParser = new JsonParser();

    public void render(Object resourceResult,
                       Resource resource,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws WebServiceException
    {
        response.setContentType("application/json; charset=utf-8");
        Writer w = null;

        try {
            w = response.getWriter();
            jsonParser.serialize(resourceResult, w);
        } catch (Exception e) {
            throw(new WebServiceException(e));
        } finally {
            try {
                if(w != null) {
                    w.flush();
                }
            } catch (Exception e) {
                log.warn("Unexpected exception flushing output stream", e);
            }
        }
    }
}

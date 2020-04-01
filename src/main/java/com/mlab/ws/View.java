package com.mlab.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {

    public void render(Object resourceResult,
                       Resource resource,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws WebServiceException;

}

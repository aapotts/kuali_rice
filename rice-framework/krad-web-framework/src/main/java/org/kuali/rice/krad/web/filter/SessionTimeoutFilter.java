/**
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.web.filter;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.view.ViewSessionPolicy;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.web.controller.UifControllerHelper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles session timeouts for KRAD views based on the configured view session policy
 *
 * <p>
 * IMPORTANT! In order to work correctly this filter should be the first filter invoked (even before the login
 * filter)
 * </p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SessionTimeoutFilter implements Filter {

    private int sessionTimeoutErrorCode = 403;

    public void init(FilterConfig filterConfig) throws ServletException {
        String timeoutErrorCode = filterConfig.getInitParameter("sessionTimeoutErrorCode");

        if (timeoutErrorCode != null) {
            sessionTimeoutErrorCode = Integer.parseInt(timeoutErrorCode);
        }
    }

    /**
     * Checks for a session timeout and if one has occurred pulls the view session policy to determine whether
     * a redirect needs to happen
     *
     * <p>
     * To determine whether a session timeout has occurred, the filter looks for the existence of a request parameter
     * named {@link org.kuali.rice.krad.uif.UifParameters#SESSION_ID}. If found it then compares that id to the id
     * on the current session. If they are different, or a session does not currently exist a timeout is assumed.
     * </p>
     *
     * <p>
     * If a timeout has occurred an attempt is made to resolve a view from the request (based on the view id or
     * type parameters), then the associated {@link ViewSessionPolicy} is pulled which indicates how the timeout should
     * be handled. This either results in doing a redirect or nothing
     * </p>
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filerChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpSession httpSession = (httpServletRequest).getSession(false);

        // if we don't have a session id on the request we cannot determine a session timeout, so just
        // continue the filter chain
        if (request.getParameter(UifParameters.SESSION_ID) == null) {
            filerChain.doFilter(request, response);
        }

        // compare session id in request to id on current session, if different or a session does not exist
        // then assume a session timeout has occurred
        String requestedSessionId = request.getParameter(UifParameters.SESSION_ID);

        if ((httpSession != null) && StringUtils.equals(httpSession.getId(), requestedSessionId)) {
            // within same session
            filerChain.doFilter(request, response);
        }

        // check for an ajax request since the redirects need to happen differently for them
        boolean ajaxRequest = false;

        String ajaxHeader = ((HttpServletRequest) request).getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(ajaxHeader)) {
            ajaxRequest = true;
        }

        // retrieve timeout policy associated with the view to determine what steps to take
        String viewId = UifControllerHelper.getViewIdFromRequest(httpServletRequest);

        if (StringUtils.isNotBlank(viewId)) {
            ViewSessionPolicy sessionPolicy = KRADServiceLocatorWeb.getViewDictionaryService().getViewSessionPolicy(
                    viewId);

            if (sessionPolicy.isRedirectToHome()
                    || StringUtils.isNotBlank(sessionPolicy.getRedirectUrl())
                    || sessionPolicy.isRenderTimeoutView()) {
                String redirectUrl = getRedirectUrl(sessionPolicy, httpServletRequest);

                sendRedirect((HttpServletResponse) response, redirectUrl, ajaxRequest);
            }
        }
    }

    /**
     * Inspects the given view session policy to determine how the request should be redirected
     *
     * <p>
     * The request will either be redirected to the application home, a custom URL, the same request URL but
     * modified to call the <code>sessionTimeout</code> method, or a redirect to show the session timeout view
     * </p>
     *
     * @param sessionPolicy session policy instance to inspect
     * @param httpServletRequest request instance for pulling parameters
     * @return redirect URL or null if no redirect was configured
     */
    protected String getRedirectUrl(ViewSessionPolicy sessionPolicy, HttpServletRequest httpServletRequest) {
        String redirectUrl = null;

        if (sessionPolicy.isRedirectToHome()) {
            redirectUrl = CoreApiServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
                    KRADConstants.APPLICATION_URL_KEY);
        } else if (StringUtils.isNotBlank(sessionPolicy.getRedirectUrl())) {
            redirectUrl = sessionPolicy.getRedirectUrl();
        } else if (sessionPolicy.isRenderTimeoutView()) {
            String kradUrl = CoreApiServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
                    KRADConstants.KRAD_URL_KEY);
            redirectUrl = KRADUtils.buildViewUrl(kradUrl, KRADConstants.REQUEST_MAPPING_SESSION_TIMEOUT,
                    KRADConstants.SESSION_TIMEOUT_VIEW_ID);
        }

        return redirectUrl;
    }

    /**
     * Sends a redirect request either through the standard http redirect mechanism, or by sending back
     * an Ajax response indicating a redirect should occur
     *
     * @param httpServletResponse response object that redirect should occur on
     * @param redirectUrl url to redirect to
     * @param ajaxRequest indicates whether the original request was made with Ajax
     * @throws IOException
     */
    protected void sendRedirect(HttpServletResponse httpServletResponse, String redirectUrl,
            boolean ajaxRequest) throws IOException {
        if (ajaxRequest) {
            httpServletResponse.setContentType("text/html; charset=UTF-8");
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setStatus(sessionTimeoutErrorCode);

            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.print(redirectUrl);

            printWriter.flush();
        } else {
            httpServletResponse.sendRedirect(redirectUrl);
        }
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // do nothing
    }
}

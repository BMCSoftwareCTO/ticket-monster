/**
 * 
 */
package org.jboss.examples.ticketmonster.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * @author cholin
 *
 */
public class CacheFilter implements Filter {

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        // TODO Auto-generated method stub
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        FakeHeadersRequest fakeHttpServletRequest = new FakeHeadersRequest(httpServletRequest);

        // Set cache directives
        httpServletResponse.setHeader(HTTPCacheHeader.CACHE_CONTROL.getName(), "private, max-age=0, no-store, must-revalidate");
        httpServletResponse.setHeader("ETag", null);
        // httpServletResponse.setDateHeader(HTTPCacheHeader.EXPIRES.getName(),
        // (new Date()).getTime() + seconds * 1000L);
        filterChain.doFilter(fakeHttpServletRequest, resp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

    public class FakeHeadersRequest extends HttpServletRequestWrapper {

        /**
         * Constructor.
         * 
         * @param request
         *            HttpServletRequest.
         */
        public FakeHeadersRequest(HttpServletRequest request) {
            super(request);
        }

        public String getHeader(String name) {
            // get the request object and cast it
            HttpServletRequest request = (HttpServletRequest) getRequest();

            // if we are looking for the "username" request header
            if ("If-Modified-Since".equalsIgnoreCase(name)) {
                return null;
            }

            // otherwise fall through to wrapped request object
            return request.getHeader(name);
        }

        public Enumeration getHeaderNames() {
            // create an enumeration of the request headers
            // additionally, add the "username" request header

            // create a list
            List list = new ArrayList();

            // loop over request headers from wrapped request object
            HttpServletRequest request = (HttpServletRequest) getRequest();
            Enumeration e = request.getHeaderNames();
            while (e.hasMoreElements()) {
                // add the names of the request headers into the list
                String n = (String) e.nextElement();
                if ("If-Modified-Since".equalsIgnoreCase(n)) {
                    continue;
                }
                list.add(n);
            }

            // create an enumeration from the list and return
            Enumeration en = Collections.enumeration(list);
            return en;
        }

    }

    public enum CacheConfigParameter {
        /** Defines whether a component is static or not. */
        STATIC("static"),
        /** Cache directive to control where the response may be cached. */
        PRIVATE("private"),
        /**
         * Cache directive to set an expiration date relative to the current
         * date.
         */
        EXPIRATION_TIME("cacheexpirationtime");

        private String name;

        private CacheConfigParameter(String name) {
            this.name = name;
        }

        /**
         * Gets the parameter name.
         * 
         * @return the parameter name
         */
        public String getName() {
            return this.name;
        }
    }

    public enum HTTPCacheHeader {
        /**
         * The Cache-Control general-header field is used to specify directives
         * that MUST be obeyed by all caching mechanisms along the
         * request/response chain.
         */
        CACHE_CONTROL("Cache-Control"),
        /**
         * The Expires entity-header field gives the date/time after which the
         * response is considered stale.
         */
        EXPIRES("Expires"),
        /**
         * The Pragma general-header field is used to include implementation-
         * specific directives that might apply to any recipient along the
         * request/response chain.
         */
        PRAGMA("Pragma"),
        /**
         * The ETag response-header field provides the current value of the
         * entity tag for the requested variant.
         */
        ETAG("ETag");

        private String name;

        private HTTPCacheHeader(String name) {
            this.name = name;
        }

        /**
         * Gets the parameter name.
         * 
         * @return the parameter name
         */
        public String getName() {
            return this.name;
        }
    }

    public enum Cacheability {
        /**
         * Indicates that the response MAY be cached by any cache, even if it
         * would normally be non-cacheable or cacheable only within a non-shared
         * cache.
         */
        PUBLIC("public"),
        /**
         * Indicates that all or part of the response message is intended for a
         * single user and MUST NOT be cached by a shared cache. This allows an
         * origin server to state that the specified parts of the response are
         * intended for only one user and are not a valid response for requests
         * by other users. A private (non-shared) cache MAY cache the response.
         */
        PRIVATE("private");

        private String value;

        private Cacheability(String value) {
            this.value = value;
        }

        /**
         * Gets the Cache-Control directive value.
         * 
         * @return the Cache-Control directive value
         */
        public String getValue() {
            return this.value;
        }
    }

}

/*
 * AuthenticationFilter.java
 *
 * Created on March 25, 2004, 12:14 PM
 */

package deckserver.login;

import nbclient.vtesmodel.JolAdminFactory;
import webclient.state.JolAdmin;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Joe User
 */

public class AuthenticationFilter implements Filter {

    private static final boolean debug = false;

    public AuthenticationFilter() {
    }
    /*
    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
    throws IOException, ServletException {
        if (debug) log("AuthenticationFilter:DoAfterProcessing");
    }*/

    public static String getStackTrace(Throwable t) {

        String stackTrace = null;

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    private boolean checkAuthentication(RequestWrapper request)
            throws IOException, ServletException {
        if (debug) log("AuthenticationFilter:DoBeforeProcessing");

        String player = null;
        String password = null;
        request.setParameter("player", null);
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("deckserver_login"))
                    player = cookies[i].getValue();
                if (cookies[i].getName().equals("deckserver_password"))
                    password = cookies[i].getValue();
            }
        if (authenticate(player, password)) {
            request.setParameter("player", new String[]{player});
            return true;
        }
        return false;

    }

    /**
     * @param request The servlet request we are processing
     * @param chain   The filter chain we are processing
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        if (debug) log("AuthenticationFilter:doFilter()");
        //
        // Create wrappers for the request and response objects.
        // Using these, you can extend the capabilities of the
        // request and response, for example, allow setting parameters
        // on the request before sending the request to the rest of the filter chain,
        // or keep track of the cookies that are set on the response.
        //
        // Caveat: some servers do not handle wrappers very well for forward or
        // include requests.
        //
        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);

        if (!checkAuthentication(wrappedRequest)) {
            /*
            try {
                response.setContentType("text/html");
                PrintWriter pw = wrappedResponse.getWriter();
                pw.println("<html>");
                pw.println("<head></head><body>"); //NOI18N
                pw.println("<h1>Bad authentication.</h1>");
                pw.println("To log on, go to the <a href=/login>login</a> page.");
                pw.println("</body></html>"); //NOI18N
                pw.close();
                return;
            }
            catch(Exception ex){ }
             **/
        }

        Throwable problem = null;

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Throwable t) {
            //
            // If an exception is thrown somewhere down the filter chain,
            // we still want to execute our after processing, and then
            // rethrow the problem after that.
            //
            problem = t;
            t.printStackTrace();
        }

        // doAfterProcessing(wrappedRequest, wrappedResponse);

        //
        // If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
        //
        if (problem != null) {
            //         if (problem instanceof ServletException) throw (ServletException)problem;
            //         if (problem instanceof IOException) throw (IOException)problem;
            sendProcessingError(problem, wrappedResponse);
        }
    }

    public boolean authenticate(String player, String password) {
        return JolAdminFactory.INSTANCE.authenticate(player, password);
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        try {
            new JolAdmin(System.getProperty("JOL_DATA"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Return a String representation of this object.
     */
    public String toString() {
        return "Authentication Filter";
    }

    private void sendProcessingError(Throwable t, ResponseWrapper response) {

        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {

            try {

                response.setContentType("text/html");
                PrintWriter pw = response.getWriter();
                pw.print("<html>\n<head>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
                ;
            } catch (Exception ex) {
            }
        }
    }

    public void log(String msg) {
        //       filterConfig.getServletContext().log(msg);
    }

    /**
     * This request wrapper class extends the support class HttpServletRequestWrapper,
     * which implements all the methods in the HttpServletRequest interface, as
     * delegations to the wrapped request.
     * You only need to override the methods that you need to change.
     * You can get access to the wrapped request using the method getRequest()
     */
    class RequestWrapper extends HttpServletRequestWrapper {

        //
        // You might, for example, wish to add a setParameter() method. To do this
        // you must also override the getParameter, getParameterValues, getParameterMap,
        // and getParameterNames methods.
        //
        protected Hashtable<String, String[]> localParams = null;

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @SuppressWarnings("unchecked")
        public void setParameter(String name, String[] values) {
            if (debug)
                System.out.println("AuthenticationFilter::setParameter(" + name + "=" + values + ")" + " localParams = " + localParams);

            if (localParams == null) {
                localParams = new Hashtable<String, String[]>();
                //
                // Copy the parameters from the underlying request.
                Map<String, String[]> wrappedParams = getRequest().getParameterMap();
                Set<String> keySet = wrappedParams.keySet();
                for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    String[] value = wrappedParams.get(key);
                    localParams.put(key, value);
                }
            }
            if (values == null) localParams.remove(name);
            else
                localParams.put(name, values);
        }

        public String getParameter(String name) {
            if (debug)
                System.out.println("AuthenticationFilter::getParameter(" + name + ") localParams = " + localParams);
            if (localParams == null)
                return getRequest().getParameter(name);
            Object val = localParams.get(name);
            if (val instanceof String)
                return (String) val;
            if (val instanceof String[]) {
                String[] values = (String[]) val;
                return values[0];
            }
            return (val == null ? null : val.toString());
        }

        public String[] getParameterValues(String name) {
            if (debug)
                System.out.println("AuthenticationFilter::getParameterValues(" + name + ") localParams = " + localParams);
            if (localParams == null)
                return getRequest().getParameterValues(name);

            return localParams.get(name);
        }

        @SuppressWarnings("unchecked")
        public Enumeration<String> getParameterNames() {
            if (debug) System.out.println("AuthenticationFilter::getParameterNames() localParams = " + localParams);
            if (localParams == null)
                return getRequest().getParameterNames();

            return localParams.keys();
        }

        @SuppressWarnings("unchecked")
        public Map<String, String[]> getParameterMap() {
            if (debug) System.out.println("AuthenticationFilter::getParameterMap() localParams = " + localParams);
            if (localParams == null)
                return getRequest().getParameterMap();
            return localParams;
        }
    }

    /**
     * This response wrapper class extends the support class HttpServletResponseWrapper,
     * which implements all the methods in the HttpServletResponse interface, as
     * delegations to the wrapped response.
     * You only need to override the methods that you need to change.
     * You can get access to the wrapped response using the method getResponse()
     */
    class ResponseWrapper extends HttpServletResponseWrapper {

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }


        //
        // You might, for example, wish to know what cookies were set on the response
        // as it went throught the filter chain. Since HtytpServletRequest doesn't
        // have a get cookies method, we will need to store them locally as they
        // are being set.
        //
        /*
            protected Vector cookies = null;

            //
            // Create a new method that doesn't exist in HttpServletResponse
            //
            public Enumeration getCookies() {
                if (cookies == null)
                    cookies = new Vector();
                return cookies.elements();
            }

            //
            // Override this method from HttpServletResponse to keep track
            // of cookies locally as well as in the wrapped response.
            //
            public void addCookie (Cookie cookie) {
                if (cookies == null)
                    cookies = new Vector();
                cookies.add(cookie);
                ((HttpServletResponse)getResponse()).addCookie(cookie);
            }
         */
    }
}



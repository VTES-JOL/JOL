package net.deckserver.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Renders a JSP to a string by capturing the response output.
 * Replaces DWR's WebContextFactory.forwardToString().
 */
public final class JspRenderer {

    private JspRenderer() {}

    public static String render(HttpServletRequest request, HttpServletResponse response, String jspPath) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public PrintWriter getWriter() {
                return pw;
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return new ServletOutputStream() {
                    @Override public boolean isReady() { return true; }
                    @Override public void setWriteListener(WriteListener wl) {}
                    @Override public void write(int b) { pw.write(b); }
                };
            }
        };
        request.getRequestDispatcher(jspPath).include(request, wrapper);
        pw.flush();
        return sw.toString();
    }
}

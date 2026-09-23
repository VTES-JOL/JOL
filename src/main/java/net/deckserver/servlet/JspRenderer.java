package net.deckserver.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Renders a JSP to a string by capturing the response output.
 * Replaces DWR's WebContextFactory.forwardToString().
 */
public final class JspRenderer {

    private JspRenderer() {}

    public static String render(HttpServletRequest request, HttpServletResponse response, String jspPath) throws Exception {
        // <jsp:include> + <jsp:param> (e.g. state.jsp's per-player loop) compiles to a synthetic
        // "page.jsp?name=value" URL built via JspRuntimeLibrary.URLEncode(value,
        // request.getCharacterEncoding()) - i.e. it percent-encodes using the ORIGINAL request's
        // encoding, not this render's response. This request is a REST call that never declares an
        // encoding, so it defaults to ISO-8859-1: a non-ASCII char (e.g. accented player name) gets
        // percent-encoded as its single Latin-1 byte, then decoded back as UTF-8 by Tomcat's query
        // string parser - an invalid single-byte sequence, silently mangled. Forcing UTF-8 here
        // makes the encode/decode side agree.
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // Jasper may write a given JSP's output through getWriter() (chars) or getOutputStream()
        // (bytes) depending on internal buffering - both must land in the same byte sink so a
        // multi-byte UTF-8 character never gets split across the two, and decoded exactly once at
        // the end. Feeding bytes one at a time into a char-based Writer misinterprets each raw byte
        // as its own code point, mangling any non-ASCII text (e.g. accented player names).
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(bytes, StandardCharsets.UTF_8));
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public String getCharacterEncoding() {
                // Jasper's own JspWriter encodes template text/expression output to bytes using
                // this before it ever reaches getWriter()/getOutputStream() below - included JSPs
                // here (e.g. state.jsp) declare no contentType/charset, so without this override
                // Jasper falls back to ISO-8859-1, silently mangling any non-ASCII character (e.g.
                // accented player names) before we ever see it.
                return "UTF-8";
            }

            @Override
            public void setCharacterEncoding(String charset) {
                // no-op: keep this pinned to UTF-8 regardless of what a JSP directive requests
            }

            @Override
            public PrintWriter getWriter() {
                return pw;
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return new ServletOutputStream() {
                    @Override public boolean isReady() { return true; }
                    @Override public void setWriteListener(WriteListener wl) {}
                    @Override public void write(int b) {
                        pw.flush();
                        bytes.write(b);
                    }
                    @Override public void write(byte[] b, int off, int len) {
                        pw.flush();
                        bytes.write(b, off, len);
                    }
                };
            }
        };
        request.getRequestDispatcher(jspPath).include(request, wrapper);
        pw.flush();
        return bytes.toString(StandardCharsets.UTF_8);
    }
}

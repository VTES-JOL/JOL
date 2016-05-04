/*
 * StreamReader.java
 *
 * Created on March 8, 2005, 8:59 PM
 */

package util;

import java.io.*;

/**
 * @author gfinklan
 */
public class StreamReader {

    public static String read(InputStream in) throws IOException {
        try {
            LineNumberReader reader = get(in);
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                out.println(tmp);
            reader.close();
            tmp = writer.getBuffer().toString();
            writer.close();
            out.close();
            return tmp;
        } finally {
            in.close();
        }
    }

    public static LineNumberReader get(InputStream in) throws IOException {
        byte[] bytes = toByteArray(in);
        in = new ByteArrayInputStream(bytes);
        InputStreamReader r = new InputStreamReader(in, "ISO-8859-1");
        LineNumberReader reader = new LineNumberReader(r);
        return reader;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1000];
        int read = 0;
        while ((read = in.read(buf)) >= 0)
            out.write(buf, 0, read);
        in.close();
        return out.toByteArray();
    }
}

/*
 * InteractiveAdmin.java
 *
 * Created on March 19, 2005, 4:30 PM
 */

package webclient.state;

import nbclient.vtesmodel.JolAdminFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

/**
 *
 * @author  gfinklan
 */
public class InteractiveAdmin {
    
    public static String executeBlock(String block) {
        StringReader reader = new StringReader(block);
        LineNumberReader r = new LineNumberReader(reader);
        StringWriter res = new StringWriter();
        try {
            String line = "";
            while((line = r.readLine()) != null) {
                res.write(line + " : " + execute(line));
            }
        } catch (IOException ie) {
            res.write(ie.toString());
        }
        return res.getBuffer().toString();
    }
    
    public static String execute(String line) {
        try {
            JolAdminFactory factory = JolAdminFactory.INSTANCE;
            Class<JolAdminFactory> cl = JolAdminFactory.class;
            StringTokenizer tok = new StringTokenizer(line,",");
            int argc = tok.countTokens() - 1;
            String cmd = tok.nextToken();
            if(cmd.equals("list")) {
                StringWriter w = new StringWriter();
                Method[] m = cl.getMethods();
                for(int i = 0; i < m.length; i++)
                    w.write(m[i].toString() + "\n");
                return w.getBuffer().toString();
            }
            String[] argv = new String[argc];
            Class[] clarr = new Class[argc];
            for(int i = 0; i < argc; i++) {
                argv[i] = tok.nextToken();
                clarr[i] = String.class;
            }
            
            Method m = cl.getMethod(cmd,clarr);
            Object ret = m.invoke(factory,(Object[])argv);
            if(ret instanceof Object[]) {
                Object[] arr = (Object[]) ret;
                String tmp = "[";
                for(int i = 0; i < arr.length; i++) {
                    if( i > 0) tmp += ",";
                    tmp += arr[i].toString();
                }
                tmp += "]";
                ret = tmp;
            }
            return "Successful - " + ret;
        } catch (NoSuchMethodException e) {
            return "No such method on admin interface.";
        } catch (SecurityException e) {
            return "Badly configured server - this functionality is disabled";
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            return "Bad error " + t.getMessage();
        }
    }
    
}

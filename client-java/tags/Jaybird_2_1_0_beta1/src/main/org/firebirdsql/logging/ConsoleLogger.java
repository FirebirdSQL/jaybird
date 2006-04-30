/*
 * Created on 04.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.firebirdsql.logging;

/**
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class ConsoleLogger extends Logger {
    
    private static final boolean debugEnabled = false;
    private static final boolean traceEnabled = true;
    private static final boolean infoEnabled = true;
    private static final boolean warnEnabled = true;
    private static final boolean errEnabled = true;
    private static final boolean fatalEnabled = true;
    
    private String name;
    
    public ConsoleLogger(String name){
        int lastPoint = name.lastIndexOf('.');
        if (lastPoint == -1)
            this.name = name;
        else
            this.name = name.substring(lastPoint + 1);
    }
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    private void out(Object message, Throwable t) {
        synchronized(System.out) {
            System.out.println("[" + name + "]" + message);
            if (t != null)
                t.printStackTrace(System.out);
        }
    }

    private void err(Object message, Throwable t) {
        synchronized(System.out) {
            System.err.println("[" + name + "]" + message);
            if (t != null)
                t.printStackTrace(System.err);
        }
    }
    
    public void debug(Object message) {
        debug(message, null);
    }
    
    public void debug(Object message, Throwable t) {
        if (isDebugEnabled()) {
            out(message, t);
        }
    }
    
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void trace(Object message, Throwable t) {
        if (isTraceEnabled()) {
            out(message, t);
        }
    }

    public void trace(Object message) {
        trace(message, null);
    }

    public boolean isInfoEnabled() {
        return infoEnabled;
    }
    
    public void info(Object message) {
        info(message, null);
    }
    
    public void info(Object message, Throwable t) {
        if (isInfoEnabled())
            out(message, t);
    }
    
    public boolean isWarnEnabled() {
        return warnEnabled;
    }
    
    public void warn(Object message) {
        warn(message, null);
    }
    
    public void warn(Object message, Throwable t) {
        if (isWarnEnabled())
            err(message, t);
    }
    
    public boolean isErrorEnabled() {
        return errEnabled;
    }
    
    public void error(Object message) {
        error(message, null);
    }
    
    public void error(Object message, Throwable t) {
        if (isErrorEnabled())
            err(message, t);
    }
    
    public boolean isFatalEnabled() {
        return fatalEnabled;
    }
    
    public void fatal(Object message) {
        fatal(message, null);
    }
    
    public void fatal(Object message, Throwable t) {
        if (isFatalEnabled())
            err(message, t);
    }

}

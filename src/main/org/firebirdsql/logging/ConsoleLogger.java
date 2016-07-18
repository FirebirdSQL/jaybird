/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.logging;

/**
 * Logger implementation that writes to the console output.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
final class ConsoleLogger implements Logger {
    
    private static final boolean debugEnabled = false;
    private static final boolean traceEnabled = false;
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

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    @Override
    public void debug(String message) {
        debug(message, null);
    }

    @Override
    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            out(message, t);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    @Override
    public void trace(String message) {
        trace(message, null);
    }

    @Override
    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            out(message, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return infoEnabled;
    }

    @Override
    public void info(String message) {
         info(message, null);
    }

    @Override
    public void info(String message, Throwable t) {
        if (isInfoEnabled()) {
            out(message, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return warnEnabled;
    }

    @Override
    public void warn(String message) {
         warn(message, null);
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isWarnEnabled()) {
            err(message, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return errEnabled;
    }

    @Override
    public void error(String message) {
        error(message, null);
    }

    @Override
    public void error(String message, Throwable t) {
        if (isErrorEnabled()) {
            err(message, t);
        }
    }

    @Override
    public boolean isFatalEnabled() {
        return fatalEnabled;
    }

    @Override
    public void fatal(String message) {
         fatal(message, null);
    }

    @Override
    public void fatal(String message, Throwable t) {
        if (isFatalEnabled()) {
            err(message, t);
        }
    }

}

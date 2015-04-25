/*
 * $Id$
 *
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
    private static final boolean traceEnabled = true;
    private static final boolean infoEnabled = true;
    private static final boolean warnEnabled = true;
    private static final boolean errEnabled = true;
    private static final boolean fatalEnabled = true;

    private String name;

    public ConsoleLogger(String name) {
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
        synchronized (System.out) {
            System.out.println("[" + name + "]" + message);
            if (t != null)
                t.printStackTrace(System.out);
        }
    }

    private void err(Object message, Throwable t) {
        synchronized (System.out) {
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

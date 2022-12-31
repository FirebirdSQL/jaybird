/*
 * Firebird Open Source JDBC Driver
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

import static java.lang.String.format;

/**
 * Logger is a facade to hide the logging implementation used from the rest of Jaybird.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("unused")
public interface Logger {

    boolean isDebugEnabled();

    void debug(String message);

    default void debugf(String formatMessage, Object p1) {
        if (isDebugEnabled()) {
            debug(format(formatMessage, p1));
        }
    }

    default void debugf(String formatMessage, Object p1, Object p2) {
        if (isDebugEnabled()) {
            debug(format(formatMessage, p1, p2));
        }
    }

    default void debugf(String formatMessage, Object p1, Object p2, Object p3) {
        if (isDebugEnabled()) {
            debug(format(formatMessage, p1, p2, p3));
        }
    }

    default void debugf(String formatMessage, Object... params) {
        if (isDebugEnabled()) {
            debug(format(formatMessage, params));
        }
    }

    void debug(String message, Throwable t);

    default void debugfe(String message, Object p1, Throwable t) {
        if (isDebugEnabled()) {
            debug(format(message, p1), t);
        }
    }

    default void debugfe(String message, Object p1, Object p2, Throwable t) {
        if (isDebugEnabled()) {
            debug(format(message, p1, p2), t);
        }
    }

    default void debugfe(String message, Object p1, Object p2, Object p3, Throwable t) {
        if (isDebugEnabled()) {
            debug(format(message, p1, p2, p3), t);
        }
    }

    boolean isTraceEnabled();

    void trace(String message);

    default void tracef(String formatMessage, Object p1) {
        if (isTraceEnabled()) {
            trace(format(formatMessage, p1));
        }
    }

    default void tracef(String formatMessage, Object p1, Object p2) {
        if (isTraceEnabled()) {
            trace(format(formatMessage, p1, p2));
        }
    }

    default void tracef(String formatMessage, Object p1, Object p2, Object p3) {
        if (isTraceEnabled()) {
            trace(format(formatMessage, p1, p2, p3));
        }
    }

    default void tracef(String formatMessage, Object... params) {
        if (isTraceEnabled()) {
            trace(format(formatMessage, params));
        }
    }

    void trace(String message, Throwable t);

    default void tracefe(String message, Object p1, Throwable t) {
        if (isTraceEnabled()) {
            trace(format(message, p1), t);
        }
    }

    default void tracefe(String message, Object p1, Object p2, Throwable t) {
        if (isTraceEnabled()) {
            trace(format(message, p1, p2), t);
        }
    }

    default void tracefe(String message, Object p1, Object p2, Object p3, Throwable t) {
        if (isTraceEnabled()) {
            trace(format(message, p1, p2, p3), t);
        }
    }

    boolean isInfoEnabled();

    void info(String message);

    default void infof(String formatMessage, Object p1) {
        if (isInfoEnabled()) {
            info(format(formatMessage, p1));
        }
    }

    default void infof(String formatMessage, Object p1, Object p2) {
        if (isInfoEnabled()) {
            info(format(formatMessage, p1, p2));
        }
    }

    default void infof(String formatMessage, Object p1, Object p2, Object p3) {
        if (isInfoEnabled()) {
            info(format(formatMessage, p1, p2, p3));
        }
    }

    default void infof(String formatMessage, Object... params) {
        if (isInfoEnabled()) {
            info(format(formatMessage, params));
        }
    }

    void info(String message, Throwable t);

    default void infofe(String message, Object p1, Throwable t) {
        if (isInfoEnabled()) {
            info(format(message, p1), t);
        }
    }

    default void infofe(String message, Object p1, Object p2, Throwable t) {
        if (isInfoEnabled()) {
            info(format(message, p1, p2), t);
        }
    }

    default void infofe(String message, Object p1, Object p2, Object p3, Throwable t) {
        if (isInfoEnabled()) {
            info(format(message, p1, p2, p3), t);
        }
    }

    boolean isWarnEnabled();

    void warn(String message);

    default void warnf(String formatMessage, Object p1) {
        if (isWarnEnabled()) {
            warn(format(formatMessage, p1));
        }
    }

    default void warnf(String formatMessage, Object p1, Object p2) {
        if (isWarnEnabled()) {
            warn(format(formatMessage, p1, p2));
        }
    }

    default void warnf(String formatMessage, Object p1, Object p2, Object p3) {
        if (isWarnEnabled()) {
            warn(format(formatMessage, p1, p2, p3));
        }
    }

    default void warnf(String formatMessage, Object... params) {
        if (isWarnEnabled()) {
            warn(format(formatMessage, params));
        }
    }

    void warn(String message, Throwable t);

    default void warnfe(String message, Object p1, Throwable t) {
        if (isWarnEnabled()) {
            warn(format(message, p1), t);
        }
    }

    default void warnfe(String message, Object p1, Object p2, Throwable t) {
        if (isWarnEnabled()) {
            warn(format(message, p1, p2), t);
        }
    }

    default void warnfe(String message, Object p1, Object p2, Object p3, Throwable t) {
        if (isWarnEnabled()) {
            warn(format(message, p1, p2, p3), t);
        }
    }

    boolean isErrorEnabled();

    void error(String message);

    default void errorf(String formatMessage, Object p1) {
        if (isErrorEnabled()) {
            error(format(formatMessage, p1));
        }
    }

    default void errorf(String formatMessage, Object p1, Object p2) {
        if (isErrorEnabled()) {
            error(format(formatMessage, p1, p2));
        }
    }

    default void errorf(String formatMessage, Object p1, Object p2, Object p3) {
        if (isErrorEnabled()) {
            error(format(formatMessage, p1, p2, p3));
        }
    }

    default void errorf(String formatMessage, Object... params) {
        if (isErrorEnabled()) {
            error(format(formatMessage, params));
        }
    }

    void error(String message, Throwable t);

    default void errorfe(String message, Object p1, Throwable t) {
        if (isErrorEnabled()) {
            error(format(message, p1), t);
        }
    }

    default void errorfe(String message, Object p1, Object p2, Throwable t) {
        if (isErrorEnabled()) {
            error(format(message, p1, p2), t);
        }
    }

    default void errorfe(String message, Object p1, Object p2, Object p3, Throwable t) {
        if (isErrorEnabled()) {
            error(format(message, p1, p2, p3), t);
        }
    }

    boolean isFatalEnabled();

    void fatal(String message);

    default void fatalf(String formatMessage, Object p1) {
        if (isFatalEnabled()) {
            fatal(format(formatMessage, p1));
        }
    }

    default void fatalf(String formatMessage, Object p1, Object p2) {
        if (isFatalEnabled()) {
            fatal(format(formatMessage, p1, p2));
        }
    }

    default void fatalf(String formatMessage, Object p1, Object p2, Object p3) {
        if (isFatalEnabled()) {
            fatal(format(formatMessage, p1, p2, p3));
        }
    }

    default void fatalf(String formatMessage, Object... params) {
        if (isFatalEnabled()) {
            fatal(format(formatMessage, params));
        }
    }

    void fatal(String message, Throwable t);

    default void fatalfe(String message, Object p1, Throwable t) {
        if (isFatalEnabled()) {
            fatal(format(message, p1), t);
        }
    }

    default void fatalfe(String message, Object p1, Object p2, Throwable t) {
        if (isFatalEnabled()) {
            fatal(format(message, p1, p2), t);
        }
    }

    default void fatalfe(String message, Object p1, Object p2, Object p3, Throwable t) {
        if (isFatalEnabled()) {
            fatal(format(message, p1, p2, p3), t);
        }
    }

    /**
     * Logs the message on warn with the throwable {@code toString()} and a note that the stacktrace is on debug.
     * The {@code message} and the stacktrace of {@code t} are also logged on debug.
     *
     * @param message
     *         Message to log
     * @param t
     *         Throwable to log ({@code toString()} on warn and full stacktrace on debug)
     */
    // This is a bit of a special case, but the pattern was repeated often enough to warrant its own method
    default void warnDebug(String message, Throwable t) {
        warnf("%s: %s; see debug level for stacktrace", message, t);
        debug(message, t);
    }

    /**
     * Logs the message on error with the throwable {@code toString()} and a note that the stacktrace is on debug.
     * The {@code message} and the stacktrace of {@code t} are also logged on debug.
     *
     * @param message
     *         Message to log
     * @param t
     *         Throwable to log ({@code toString()} on error and full stacktrace on debug)
     */
    // This is a bit of a special case, but the pattern was repeated often enough to warrant its own method
    default void errorDebug(String message, Throwable t) {
        errorf("%s: %s; see debug level for stacktrace", message, t);
        debug(message, t);
    }

}

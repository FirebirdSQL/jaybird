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

/**
 * Logger is a facade to hide the logging implementation used from the rest of Jaybird.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public interface Logger {

    boolean isDebugEnabled();

    void debug(String message);

    void debug(String message, Throwable t);

    boolean isTraceEnabled();

    void trace(String message);

    void trace(String message, Throwable t);

    boolean isInfoEnabled();

    void info(String message);

    void info(String message, Throwable t);

    boolean isWarnEnabled();

    void warn(String message);

    void warn(String message, Throwable t);

    boolean isErrorEnabled();

    void error(String message);

    void error(String message, Throwable t);

    boolean isFatalEnabled();

    void fatal(String message);

    void fatal(String message, Throwable t);

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
        if (isWarnEnabled()) {
            warn(message + ": " + t + "; see debug level for stacktrace");
            debug(message, t);
        }
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
        if (isErrorEnabled()) {
            error(message + ": " + t + "; see debug level for stacktrace");
            debug(message, t);
        }
    }

}

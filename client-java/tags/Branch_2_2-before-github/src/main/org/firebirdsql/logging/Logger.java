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
 * Logger is a facade to hide the logging implementation used from the rest of Jaybird.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public interface Logger {

    boolean isDebugEnabled();

    void debug(Object message);

    void debug(Object message, Throwable t);

    boolean isTraceEnabled();

    void trace(Object message);

    void trace(Object message, Throwable t);

    boolean isInfoEnabled();

    void info(Object message);

    void info(Object message, Throwable t);

    boolean isWarnEnabled();

    void warn(Object message);

    void warn(Object message, Throwable t);

    boolean isErrorEnabled();

    void error(Object message);

    void error(Object message, Throwable t);

    boolean isFatalEnabled();

    void fatal(Object message);

    void fatal(Object message, Throwable t);
}

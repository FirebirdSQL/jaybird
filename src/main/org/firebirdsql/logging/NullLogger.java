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
 * Implementation of {@link Logger} that doesn't do anything and reports all loglevels as disabled.
 *
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
final class NullLogger implements Logger{

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void debug(String message) {
	}

	@Override
	public void debug(String message, Throwable t) {
	}

	@Override
	public boolean isTraceEnabled() {
        return false;
    }

	@Override
	public void trace(String message) {
	}

	@Override
	public void trace(String message, Throwable t) {
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public void info(String message) {
	}

	@Override
	public void info(String message, Throwable t) {
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public void warn(String message) {
	}

	@Override
	public void warn(String message, Throwable t) {
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public void error(String message) {
	}

	@Override
	public void error(String message, Throwable t) {
	}

	@Override
	public boolean isFatalEnabled() {
		return false;
	}

	@Override
	public void fatal(String message) {
	}

	@Override
	public void fatal(String message, Throwable t) {
	}

}

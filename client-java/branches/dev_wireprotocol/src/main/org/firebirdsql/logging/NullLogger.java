 /*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.logging;

/**
 * Describe class <code>NullLogger</code> here.
 *
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public class NullLogger extends Logger{
	
	public NullLogger(String name){
	}
	
	public boolean isDebugEnabled() {
		return false;
	}
	
	public void debug(Object message) {
	}
	
	public void debug(Object message, Throwable t) {
	}
	
    public boolean isTraceEnabled() {
        return false;
    }

    public void trace(Object message, Throwable t) {
    }

    public void trace(Object message) {
    }

    public boolean isInfoEnabled() {
		return false;
	}
	
	public void info(Object message) {
	}
	
	public void info(Object message, Throwable t) {
	}
	
	public boolean isWarnEnabled() {
		return false;
	}
	
	public void warn(Object message) {
	}
	
	public void warn(Object message, Throwable t) {
	}
	
	public boolean isErrorEnabled() {
		return false;
	}
	
	public void error(Object message) {
	}
	
	public void error(Object message, Throwable t) {
	}
	
	public boolean isFatalEnabled() {
		return false;
	}
	
	public void fatal(Object message) {
	}
	
	public void fatal(Object message, Throwable t) {
	}
	
}

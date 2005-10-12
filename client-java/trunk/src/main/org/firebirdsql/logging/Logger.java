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
 * Describe class <code>Logger</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public abstract class Logger{
	
	abstract public boolean isDebugEnabled();
	
	abstract public void debug(Object message);
	
	abstract public void debug(Object message, Throwable t);
    
    abstract public boolean isTraceEnabled();
    
    abstract public void trace(Object message);
    
    abstract public void trace(Object message, Throwable t);
	
	abstract public boolean isInfoEnabled();
	
	abstract public void info(Object message);
	
	abstract public void info(Object message, Throwable t);
	
	abstract public boolean isWarnEnabled();
	
	abstract public void warn(Object message);
	
	abstract public void warn(Object message, Throwable t);
	
	abstract public boolean isErrorEnabled();
	
	abstract public void error(Object message);
	
	abstract public void error(Object message, Throwable t);
	
	abstract public boolean isFatalEnabled();
	
	abstract public void fatal(Object message);
	
	abstract public void fatal(Object message, Throwable t);
}

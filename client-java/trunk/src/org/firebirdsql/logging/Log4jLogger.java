/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.
 */
package org.firebirdsql.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

public class Log4jLogger extends Logger{
	
	private static boolean loggingAvailable = true;
	
	private final Category log;
	
	protected Log4jLogger(String name) {
		if (loggingAvailable) {
			Category myLog = null;
			try {
				myLog = Category.getInstance(name);
			}
			catch (Throwable t) {
				loggingAvailable = false;
			} // end of try-catch
			log = myLog;
		} // end of if ()
		else {
			log = null;
		} // end of else
	}
		
	public boolean isDebugEnabled() {
		return loggingAvailable && log.isEnabledFor(Priority.DEBUG);
	}
	
	public void debug(Object message) {
		if (isDebugEnabled()) {
			log.log(Priority.DEBUG, message);
		} // end of if ()
	}
	
	public void debug(Object message, Throwable t) {
		if (isDebugEnabled()) {
			log.log(Priority.DEBUG, message, t);
		}
	}
	
	public boolean isInfoEnabled() {
		return loggingAvailable && log.isEnabledFor(Priority.INFO);
	}
	
	public void info(Object message) {
		if (isInfoEnabled()) {
			log.log(Priority.INFO, message);
		}
	}
	
	public void info(Object message, Throwable t) {
		if (isInfoEnabled()) {
			log.log(Priority.INFO, message, t);
		}
	}
	
	public boolean isWarnEnabled() {
		return loggingAvailable && log.isEnabledFor(Priority.WARN);
	}
	
	public void warn(Object message) {
		if (isWarnEnabled()) {
			log.log(Priority.WARN, message);
		}
	}
	
	public void warn(Object message, Throwable t) {
		if (isWarnEnabled()) {
			log.log(Priority.WARN, message, t);
		}
	}
	
	public boolean isErrorEnabled() {
		return loggingAvailable && log.isEnabledFor(Priority.ERROR);
	}
	
	public void error(Object message) {
		if (isErrorEnabled()) {
			log.log(Priority.ERROR, message);
		}
	}
	
	public void error(Object message, Throwable t) {
		if (isErrorEnabled()) {
			log.log(Priority.ERROR, message, t);
		}
	}
	
	public boolean isFatalEnabled() {
		return loggingAvailable && log.isEnabledFor(Priority.FATAL);
	}
	
	public void fatal(Object message) {
		if (isFatalEnabled()) {
			log.log(Priority.FATAL, message);
		}
	}
	
	public void fatal(Object message, Throwable t) {
		if (isFatalEnabled()) {
			log.log(Priority.FATAL, message, t);
		}
	}	
}

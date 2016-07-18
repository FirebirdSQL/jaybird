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

import java.util.logging.Level;

/**
 * Logger using {@code java.util.logging}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
final class JulLogger implements Logger {

    private final java.util.logging.Logger log;

    protected JulLogger(String name) {
        log = java.util.logging.Logger.getLogger(name);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isLoggable(Level.FINE);
    }

    @Override
    public void debug(String message) {
        log.log(Level.FINE, message);
    }

    @Override
    public void debug(String message, Throwable t) {
        log.log(Level.FINE, message, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isLoggable(Level.FINER);
    }

    @Override
    public void trace(String message) {
        log.log(Level.FINER, message);
    }

    @Override
    public void trace(String message, Throwable t) {
        log.log(Level.FINER, message, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isLoggable(Level.INFO);
    }

    @Override
    public void info(String message) {
        log.log(Level.INFO, message);
    }

    @Override
    public void info(String message, Throwable t) {
        log.log(Level.INFO, message, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String message) {
        log.log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable t) {
        log.log(Level.WARNING, message, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String message) {
        log.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Throwable t) {
        log.log(Level.SEVERE, message, t);
    }

    @Override
    public boolean isFatalEnabled() {
        return isErrorEnabled();
    }

    @Override
    public void fatal(String message) {
        error(message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        error(message, t);
    }

}

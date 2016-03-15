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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Factory for Logger instances
 * 
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public final class LoggerFactory {

    private static final boolean forceConsoleLogger;
    /**
     * NullLogger to use for all getLogger requests if no logging is configured
     */
    private static final Logger NULL_LOGGER = new NullLogger();

    private static final boolean log4j;

    static {
        boolean useLog4j = false;
        boolean fallbackConsoleLogger = false;
        try {
            // TODO Add system property to documentation
            String sFallbackConsoleLogger = getSystemPropertyPrivileged("org.firebirdsql.jdbc.fallbackConsoleLogger");
            fallbackConsoleLogger = "true".equalsIgnoreCase(sFallbackConsoleLogger);
            String sLog4j = getSystemPropertyPrivileged("FBLog4j");
            // TODO Add system property to documentation
            String sUseLog4j = getSystemPropertyPrivileged("org.firebirdsql.jdbc.useLog4j");
            useLog4j = "true".equalsIgnoreCase(sLog4j) || "true".equalsIgnoreCase(sUseLog4j);

            if (useLog4j) {
                // Detect if we can load log4j
                try {
                    Class.forName("org.apache.log4j.Category");
                    useLog4j = true;
                } catch (ClassNotFoundException cnfe) {
                    useLog4j = false;
                }
            }
        } catch (Exception ex) {
            useLog4j = false;
        } finally {
            forceConsoleLogger = fallbackConsoleLogger;
            log4j = useLog4j;
        }
    }

    private LoggerFactory() {
        // Do not instantiate
    }

    public static Logger getLogger(String name) {
        if (log4j) {
            return new Log4jLogger(name);
        } else if (forceConsoleLogger) {
            return new ConsoleLogger(name);
        }
        return NULL_LOGGER;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
    
    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(propertyName);
            }
        });
    }
}

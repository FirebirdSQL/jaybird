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
package org.firebirdsql.gds;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Class to access Jaybird-specific system properties from a single place.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class JaybirdSystemProperties {

    private static final String COMMON_PREFIX = "org.firebirdsql.";
    private static final String JDBC_PREFIX = COMMON_PREFIX + "jdbc.";

    // do not include 'sensitive' properties, and only include Jaybird specific properties

    public static final String FORCE_CONSOLE_LOGGER_PROP = JDBC_PREFIX + "forceConsoleLogger";
    public static final String DISABLE_LOGGING_PROP = JDBC_PREFIX + "disableLogging";
    public static final String LOGGER_IMPLEMENTATION_PROP = JDBC_PREFIX + "loggerImplementation";
    public static final String SYNC_WRAP_NATIVE_LIBRARY_PROP = COMMON_PREFIX + "jna.syncWrapNativeLibrary";
    public static final String PROCESS_ID_PROP = JDBC_PREFIX + "pid";
    public static final String PROCESS_NAME_PROP = JDBC_PREFIX + "processName";
    public static final String DEFAULT_CONNECTION_ENCODING_PROPERTY = JDBC_PREFIX + "defaultConnectionEncoding";
    public static final String REQUIRE_CONNECTION_ENCODING_PROPERTY = JDBC_PREFIX + "requireConnectionEncoding";
    public static final String DATATYPE_CODER_CACHE_SIZE = COMMON_PREFIX + "datatypeCoderCacheSize";

    private JaybirdSystemProperties() {
        // no instances
    }

    public static boolean isForceConsoleLogger() {
        return getBooleanSystemPropertyPrivileged(FORCE_CONSOLE_LOGGER_PROP);
    }

    public static boolean isDisableLogging() {
        return getBooleanSystemPropertyPrivileged(DISABLE_LOGGING_PROP);
    }

    public static String getLoggerImplementation() {
        return getSystemPropertyPrivileged(LOGGER_IMPLEMENTATION_PROP);
    }

    public static boolean isSyncWrapNativeLibrary() {
        return getBooleanSystemPropertyPrivileged(SYNC_WRAP_NATIVE_LIBRARY_PROP);
    }

    public static Integer getProcessId() {
        return getIntegerSystemPropertyPrivileged(PROCESS_ID_PROP);
    }

    public static String getProcessName() {
        return getSystemPropertyPrivileged(PROCESS_NAME_PROP);
    }

    public static String getDefaultConnectionEncoding() {
        return getSystemPropertyPrivileged(DEFAULT_CONNECTION_ENCODING_PROPERTY);
    }

    public static boolean isRequireConnectionEncoding() {
        return getBooleanSystemPropertyPrivileged(REQUIRE_CONNECTION_ENCODING_PROPERTY);
    }

    public static int getDatatypeCoderCacheSize(int defaultValue) {
        Integer value = getIntegerSystemPropertyPrivileged(DATATYPE_CODER_CACHE_SIZE);
        return value != null ? value : defaultValue;
    }

    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(propertyName);
            }
        });
    }

    private static boolean getBooleanSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.getBoolean(propertyName);
            }
        });
    }

    private static Integer getIntegerSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() {
                return Integer.getInteger(propertyName);
            }
        });
    }
}

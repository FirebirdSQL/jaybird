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
package org.firebirdsql.gds;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Class to access Jaybird-specific system properties from a single place.
 *
 * @author Mark Rotteveel
 */
public final class JaybirdSystemProperties {

    private static final String COMMON_PREFIX = "org.firebirdsql.";
    private static final String JDBC_PREFIX = COMMON_PREFIX + "jdbc.";
    private static final String WIRE_PREFIX = COMMON_PREFIX + "wire.";

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
    public static final String NATIVE_LIBRARY_SHUTDOWN_DISABLED = COMMON_PREFIX + "nativeResourceShutdownDisabled";
    public static final String WIRE_DEFLATE_BUFFER_SIZE = WIRE_PREFIX + "deflateBufferSize";
    public static final String WIRE_INFLATE_BUFFER_SIZE = WIRE_PREFIX + "inflateBufferSize";
    public static final String WIRE_DECRYPT_BUFFER_SIZE = WIRE_PREFIX + "decryptBufferSize";
    public static final String WIRE_INPUT_BUFFER_SIZE = WIRE_PREFIX + "inputBufferSize";
    public static final String WIRE_OUTPUT_BUFFER_SIZE = WIRE_PREFIX + "outputBufferSize";

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

    public static boolean isNativeResourceShutdownDisabled() {
        return getBooleanSystemPropertyPrivileged(NATIVE_LIBRARY_SHUTDOWN_DISABLED);
    }

    public static int getDatatypeCoderCacheSize(int defaultValue) {
        return getWithDefault(DATATYPE_CODER_CACHE_SIZE, defaultValue);
    }

    public static int getWireDeflateBufferSize(int defaultValue) {
        return getWithDefault(WIRE_DEFLATE_BUFFER_SIZE, defaultValue);
    }

    public static int getWireInflateBufferSize(int defaultValue) {
        return getWithDefault(WIRE_INFLATE_BUFFER_SIZE, defaultValue);
    }

    public static int getWireDecryptBufferSize(int defaultValue) {
        return getWithDefault(WIRE_DECRYPT_BUFFER_SIZE, defaultValue);
    }

    public static int getWireInputBufferSize(int defaultValue) {
        return getWithDefault(WIRE_INPUT_BUFFER_SIZE, defaultValue);
    }

    public static int getWireOutputBufferSize(int defaultValue) {
        return getWithDefault(WIRE_OUTPUT_BUFFER_SIZE, defaultValue);
    }

    private static int getWithDefault(String propertyName, int defaultValue) {
        Integer value = getIntegerSystemPropertyPrivileged(propertyName);
        return value != null ? value : defaultValue;
    }

    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(propertyName));
    }

    private static boolean getBooleanSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean(propertyName));
    }

    private static Integer getIntegerSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged((PrivilegedAction<Integer>) () -> Integer.getInteger(propertyName));
    }
}

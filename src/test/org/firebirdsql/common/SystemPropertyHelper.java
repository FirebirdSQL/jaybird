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
package org.firebirdsql.common;

import org.firebirdsql.common.function.UncheckedCloseable;

import static java.util.Objects.requireNonNull;

/**
 * Helper methods for managing system properties during a test.
 *
 * @author Mark Rotteveel
 */
public final class SystemPropertyHelper {

    private SystemPropertyHelper() {
        // no instances
    }

    /**
     * Temporarily sets the system property {@code name} to {@code value}, restoring the original value when
     * {@link AutoCloseable#close()} is called.
     * <p>
     * The recommended use for this method is in a try-with-resources statement.
     * </p>
     *
     * @param name
     *         name of the system property
     * @param value
     *         value of the system property ({@code null} will clear/remove the system property if it exists)
     * @return auto-closeable which will restore the original value of the system property
     */
    public static UncheckedCloseable withTemporarySystemProperty(String name, String value) {
        class TemporarySystemProperty
                implements UncheckedCloseable {

            private final String name;
            private final String originalValue;

            TemporarySystemProperty(String name, String value) {
                this.name = requireNonNull(name, "name");
                this.originalValue = System.getProperty(name);
                setSystemProperty(name, value);
            }

            @Override
            public void close() {
                setSystemProperty(name, originalValue);
            }
        }

        return new TemporarySystemProperty(name, value);
    }

    /**
     * Sets the system property {@code name} to {@code value}, or clears the property if {@code value} is {@code null}.
     *
     * @param name
     *         system property name
     * @param value
     *         new value
     */
    public static void setSystemProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

}

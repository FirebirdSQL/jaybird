// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import org.firebirdsql.common.function.UncheckedCloseable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Helper methods for managing system properties during a test.
 *
 * @author Mark Rotteveel
 */
@NullMarked
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
    public static UncheckedCloseable withTemporarySystemProperty(String name, @Nullable String value) {
        record TemporarySystemProperty(String name, @Nullable String value, @Nullable String originalValue)
                implements UncheckedCloseable {

            TemporarySystemProperty {
                if (name == null) {
                    throw new NullPointerException("name");
                }
                setSystemProperty(name, value);
            }

            TemporarySystemProperty(String name, @Nullable String value) {
                this(name, value, System.getProperty(name));
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
    public static void setSystemProperty(String name, @Nullable String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

}

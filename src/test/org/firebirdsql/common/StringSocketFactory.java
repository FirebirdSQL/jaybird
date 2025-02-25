// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

/**
 * Socket factory for testing the {@code socketFactory} connection property with a {@link String} constructor argument.
 * <p>
 * Contrary to {@link NoArgSocketFactory} and {@link PropertiesSocketFactory}, this variant does not allow socket
 * creation.
 * </p>
 *
 * @author Mark Rotteveel
 */
public final class StringSocketFactory extends BaseSocketFactory {

    @SuppressWarnings("unused")
    public StringSocketFactory(String arg) {

    }

}

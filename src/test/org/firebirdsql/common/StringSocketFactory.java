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

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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBDriverNotCapableException;

import java.sql.SQLException;

/**
 * Field for {@code TIMESTAMP WITH TIME ZONE}.
 * <p>
 * Will always throw a {@link FBDriverNotCapableException} on construction as this type is not supported in
 * Java 7 / JDBC 4.1.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
class FBTimestampTzField extends FBField {

    FBTimestampTzField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        // TODO Update instruction once property defined
        throw new FBDriverNotCapableException(
                "The Java 7 / JDBC 4.1 version of Jaybird does not support type TIMESTAMP WITH TIME ZONE, "
                        + "either upgrade to Java 8 and the Java 8 or higher version of Jaybird, "
                        + "or execute SET TIME ZONE BIND LEGACY on this connection to convert "
                        + "this type to TIMESTAMP WITHOUT TIME ZONE using the session time zone.");
    }

}
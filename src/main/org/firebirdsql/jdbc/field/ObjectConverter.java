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

/**
 * Supports object conversions as specified by JDBC.
 * <p>
 * In the current implementation the object conversions are specified in {@link org.firebirdsql.jdbc.field.FBField}
 * and its subclasses. The main intention of this interface is to be able to plug in additional conversion for JDBC 4.2
 * with the jsr310 classes that are not available in Java 7 and earlier, without having to do a lot of work in
 * 'subclasses per version'.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
interface ObjectConverter {

    /**
     * If the {@code object} type is supported, and the object (or its conversion) is allowed by JDBC, it sets the
     * {@code field} with the (converted) object.
     * <p>
     * As this method is - for the time being - intended as a workaround for JDBC 4.2, it specifically only supports
     * new conversions in JDBC 4.2. It will return false for any other object type.
     * </p>
     *
     * @param field FBField implementation to set
     * @param object The object value to set
     * @return {@code true} when a conversion was applied, {@code false} when there is no conversion for the object and field
     * @throws java.sql.SQLException For exceptions when setting the object on the field (eg unsupported conversion).
     */
    boolean setObject(FBField field, Object object) throws java.sql.SQLException;

    /**
     * Get object with the specified type.
     * <p>
     * As this method is - for the time being - intended as a workaround for JDBC 4.2, it specifically only supports
     * new conversions in JDBC 4.2. It will return false for any other object type.
     * </p>
     *
     * @param field FBField implementation to set
     * @param type Type conversion to get
     * @param <T> Type parameter
     * @return Value of the field in the specified type, or {@code null} when null.
     * @throws java.sql.SQLException For conversion errors and unsupported types.
     * @since 3.0
     */
    <T> T getObject(FBField field, Class<T> type) throws java.sql.SQLException;
}

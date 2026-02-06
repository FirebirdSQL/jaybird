// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.fields;

import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Interface for calculating the blr (binary language representation) of a row.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface BlrCalculator {

    /**
     * Calculates the blr for the row descriptor.
     *
     * @param rowDescriptor
     *         Row descriptor
     * @return Byte array with the blr
     * @throws SQLException
     *         When the {@link RowDescriptor} contains an unsupported field type.
     */
    byte[] calculateBlr(RowDescriptor rowDescriptor) throws SQLException;

    /**
     * Calculates the blr for a specific row value.
     * <p>
     * This allows to optimize for the actual length of the field.
     * </p>
     *
     * @param rowDescriptor
     *         Row descriptor
     * @param rowValue
     *         Row value
     * @return Byte array with the blr
     * @throws SQLException
     *         When the {@link RowValue} contains an unsupported field type.
     */
    byte[] calculateBlr(RowDescriptor rowDescriptor, RowValue rowValue) throws SQLException;

    /**
     * Calculates the io length for the field descriptor.
     * <p>
     * The return value indicates the length and padding of the type in the buffer
     * <ul>
     * <li>&lt; 0 : Type is of specified length * -1 and not padded</li>
     * <li>== 0 : Type is of dynamic length (which is specified in the buffer as an integer) and padded</li>
     * <li>&gt; 0 : Type is of specified length minus 1 (subtracting 1 is required to avoid 0 for types of zero length) and padded</li>
     * </ul>
     * </p>
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @return The io length
     */
    int calculateIoLength(FieldDescriptor fieldDescriptor) throws SQLException;

    /**
     * Calculates the io length for the field descriptor and actual data.
     * <p>
     * The return value indicates the length and padding of the type in the buffer
     * <ul>
     * <li>&lt; 0 : Type is of specified length * -1 and not padded</li>
     * <li>== 0 : Type is of dynamic length (which is specified in the buffer as an integer) and padded</li>
     * <li>&gt; 0 : Type is of specified length minus 1 (subtracting 1 is required to avoid 0 for types of zero length)
     * and padded</li>
     * </ul>
     * </p>
     * <p>
     * This allows to optimize for the actual length of the field.
     * </p>
     * <p>
     * For <code>CHAR</code> ({@link org.firebirdsql.gds.ISCConstants#SQL_TEXT} the implementation should be consistent
     * with the lengths as given by {@link #calculateIoLength(FieldDescriptor)}.
     * </p>
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @param fieldData
     *         byte array (can be {@code null}) with field data.
     * @return The io length
     * @since 4.0
     */
    int calculateIoLength(FieldDescriptor fieldDescriptor, byte @Nullable [] fieldData) throws SQLException;

    /**
     * Calculates the batch message length.
     *
     * @param rowDescriptor
     *         row descriptor
     * @return batch message length
     * @throws SQLException
     *         when the {@link RowDescriptor} contains an unsupported field type.
     * @since 5
     */
    int calculateBatchMessageLength(RowDescriptor rowDescriptor) throws SQLException;

}

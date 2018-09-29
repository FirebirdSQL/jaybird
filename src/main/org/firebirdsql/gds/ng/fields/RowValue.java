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
package org.firebirdsql.gds.ng.fields;

import java.util.Arrays;

/**
 * Collection of values of fields. Usually a row or set of parameters.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class RowValue {

    /**
     * Marker array object for uninitialized fields
     */
    private static final byte[] NOT_INITIALIZED = new byte[0];

    public static final RowValue EMPTY_ROW_VALUE = new RowValue(0, false);

    private final byte[][] fieldData;

    /**
     * Creates an empty row value with {@code size} fields.
     * <p>
     * Use {@link #EMPTY_ROW_VALUE} for size 0.
     * </p>
     *
     * @param size
     *         Number of fields
     * @param markUninitialized
     *         {@code true} to mark all fields as not initialized
     */
    private RowValue(int size, boolean markUninitialized) {
        this.fieldData = new byte[size][];
        if (markUninitialized) {
            Arrays.fill(this.fieldData, NOT_INITIALIZED);
        }
    }

    /**
     * @return The number of fields.
     */
    public int getCount() {
        return fieldData.length;
    }

    /**
     * Sets the data of the field with {@code index}.
     *
     * @param index
     *         Index of the field
     * @param data
     *         byte array with data for field, or {@code null}
     * @throws java.lang.IndexOutOfBoundsException
     *         if index is not {@code 0 <= index > getCount()}
     */
    public void setFieldData(int index, byte[] data) {
        fieldData[index] = data;
    }

    /**
     * Get the data of the field with {@code index}.
     * <p>
     * For uninitialized fields, returns {@code null}. To distinguish between uninitialized or initialized with
     * {@code null}, use {@link #isInitialized(int)}.
     * </p>
     *
     * @param index
     *         Index of the field
     * @return byte array with data for field, or {@code null}
     * @throws java.lang.IndexOutOfBoundsException
     *         if index is not {@code 0 <= index > getCount()}
     */
    public byte[] getFieldData(int index) {
        byte[] data = fieldData[index];
        return data != NOT_INITIALIZED ? data : null;
    }

    /**
     * Resets the state of this row value to uninitialized.
     */
    public void reset() {
        Arrays.fill(fieldData, NOT_INITIALIZED);
    }

    /**
     * Initializes uninitialized fields with {@code null}.
     */
    void initializeFields() {
        for (int idx = 0; idx < fieldData.length; idx++) {
            if (fieldData[idx] == NOT_INITIALIZED) {
                fieldData[idx] = null;
            }
        }
    }

    /**
     * Is the field with {@code index} initialized.
     *
     * @param index
     *         Index of the field
     * @return {@code true} if the field is initialized
     * @throws IndexOutOfBoundsException
     *         if index is not {@code 0 <= index > getCount()}
     */
    public boolean isInitialized(int index) {
        return fieldData[index] != NOT_INITIALIZED;
    }

    /**
     * Convenience method for creating a default, uninitialized, row value for a {@link RowDescriptor}.
     *
     * @param rowDescriptor
     *         The row descriptor
     * @return {@code RowValue} object
     */
    public static RowValue defaultFor(RowDescriptor rowDescriptor) {
        int count = rowDescriptor.getCount();
        if (count == 0) {
            return EMPTY_ROW_VALUE;
        }
        return new RowValue(count, true);
    }

    /**
     * Convenience method for populating a row value from a RowDescriptor and byte arrays.
     * <p>
     * Note this method, and the similar {@link org.firebirdsql.gds.ng.fields.RowValueBuilder} are mainly intended for
     * use in {@link org.firebirdsql.jdbc.FBDatabaseMetaData}.
     * </p>
     * <p>
     * Compared to {@link #of(byte[][])}, this method has the advantage that it checks if the number of byte arrays
     * is consistent with the row descriptor.
     * </p>
     *
     * @param rowDescriptor
     *         The row descriptor
     * @param rowData
     *         An array of byte arrays with the field data.
     * @return new {@code RowValue} object
     * @throws IllegalArgumentException
     *         If the {@code rowData} byte array count does not match field count of the row descriptor
     * @see org.firebirdsql.gds.ng.fields.RowValueBuilder
     */
    public static RowValue of(RowDescriptor rowDescriptor, byte[]... rowData) {
        final int size = rowDescriptor.getCount();
        if (size != rowData.length) {
            throw new IllegalArgumentException("Expected RowDescriptor count and rowData length to be the same");
        }
        if (size == 0) {
            return EMPTY_ROW_VALUE;
        }
        final RowValue rowValue = new RowValue(size, false);
        for (int i = 0; i < size; i++) {
            rowValue.setFieldData(i, rowData[i]);
        }
        return rowValue;
    }

    /**
     * Convenience method for populating a row value from byte arrays.
     * <p>
     * This method is mainly intended for use with direct manipulation in the low-level gds-ng API.
     * </p>
     *
     * @param rowData
     *         An array of byte arrays with the field data.
     * @return new {@code RowValue} object
     * @see org.firebirdsql.gds.ng.fields.RowValueBuilder
     * @see #of(RowDescriptor, byte[][])
     */
    public static RowValue of(byte[]... rowData) {
        if (rowData.length == 0) {
            return EMPTY_ROW_VALUE;
        }
        RowValue newRowValue = new RowValue(rowData.length, false);
        for (int idx = 0; idx < rowData.length; idx++) {
            newRowValue.setFieldData(idx, rowData[idx]);
        }
        return newRowValue;
    }

    /**
     * Copies this {@code RowValue} and the values it contains.
     * <p>
     * As the field values are mutable, it is important to consider whether you need to be able
     * to see modifications to the field data, or if you need fields with the same original data. If the former,
     * pass the original, if the latter use this method to obtain a copy.
     * </p>
     *
     * @return Copy of this object with cloned field values, for empty rows (count is 0) {@link #EMPTY_ROW_VALUE}.
     */
    public RowValue deepCopy() {
        final int size = getCount();
        if (size == 0) {
            return EMPTY_ROW_VALUE;
        }
        // Implementation note: I decided not to override clone here because it didn't "feel right"
        RowValue newRowValue = new RowValue(size, false);
        for (int idx = 0; idx < size; idx++) {
            byte[] value = fieldData[idx];
            if (value != null && value != NOT_INITIALIZED) {
                value = value.clone();
            }
            newRowValue.fieldData[idx] = value;
        }
        return newRowValue;
    }

}

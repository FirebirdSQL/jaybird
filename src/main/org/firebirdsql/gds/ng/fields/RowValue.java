// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.fields;

import java.util.Arrays;

/**
 * Collection of values of fields. Usually row or parameter values.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public sealed class RowValue {

    /**
     * Marker array object for uninitialized fields
     */
    // NOTE: Do not assign ByteArrayHelper.empty(), as this must be a unique instance
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
    public final int getCount() {
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
    public final byte[] getFieldData(int index) {
        byte[] data = fieldData[index];
        return data != NOT_INITIALIZED ? data : null;
    }

    /**
     * Resets the state of this row value to uninitialized.
     * 
     * @since 4
     */
    public void reset() {
        Arrays.fill(fieldData, NOT_INITIALIZED);
    }

    /**
     * Does this row value serve as a deleted row marker.
     * <p>
     * This is not general purpose functionality, but exists solely to detect deleted rows in updatable result sets.
     * </p>
     *
     * @return {@code true} if this a deleted row marker, {@code false} otherwise
     * @since 5
     */
    public boolean isDeletedRowMarker() {
        return false;
    }

    /**
     * Initializes uninitialized fields with {@code null}.
     *
     * @since 5
     */
    public final void initializeFields() {
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
     * @since 4
     */
    public final boolean isInitialized(int index) {
        return fieldData[index] != NOT_INITIALIZED;
    }

    /**
     * @return number of initialized fields in this row value
     * @since 6
     */
    public final int initializedCount() {
        int count = 0;
        for (byte[] fieldDatum : fieldData) {
            if (fieldDatum != NOT_INITIALIZED) {
                count++;
            }
        }
        return count;
    }

    /**
     * Convenience method for creating a default, uninitialized, row value for a {@link RowDescriptor}.
     *
     * @param rowDescriptor
     *         The row descriptor
     * @return {@code RowValue} object
     * @since 4
     */
    public static RowValue defaultFor(RowDescriptor rowDescriptor) {
        int count = rowDescriptor.getCount();
        return count == 0 ? EMPTY_ROW_VALUE : new RowValue(count, true);
    }

    /**
     * Convenience method for populating a row value from a RowDescriptor and byte arrays.
     * <p>
     * Note this method is mainly intended for use in {@link org.firebirdsql.jdbc.FBDatabaseMetaData}.
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
     * Creates a row value that can serve as a deleted row marker.
     * <p>
     * All fields have a value of {@code null}, and updates will fail with an {@link UnsupportedOperationException}.
     * </p>
     *
     * @param count The number of columns
     * @return {@code RowValue} object
     * @since 5
     */
    public static RowValue deletedRowMarker(int count) {
        return new DeletedRowMarker(count);
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
    public final RowValue deepCopy() {
        final int size = getCount();
        if (this instanceof DeletedRowMarker) {
            return new DeletedRowMarker(size);
        } else if (size == 0) {
            return EMPTY_ROW_VALUE;
        }

        // Implementation note: I decided not to override clone here because it didn't "feel right"
        RowValue newRowValue = new RowValue(size, false);
        for (int idx = 0; idx < size; idx++) {
            byte[] value = fieldData[idx];
            newRowValue.fieldData[idx] = value == null || value == NOT_INITIALIZED ? value : value.clone();
        }
        return newRowValue;
    }

    /**
     * Marks a deleted row.
     */
    private static final class DeletedRowMarker extends RowValue {

        private DeletedRowMarker(int size) {
            super(size, false);
        }

        @Override
        public void setFieldData(int index, byte[] data) {
            throw new UnsupportedOperationException("Deleted row marker should not be updated");
        }

        @Override
        public void reset() {
            // do nothing
        }

        @Override
        public boolean isDeletedRowMarker() {
            return true;
        }
    }

}

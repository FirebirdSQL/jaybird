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
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.gds.ng.DatatypeCoder;

/**
 * Builder for {@link RowValue} instances.
 * <p>
 * This class allows for sparse population of column values (ie: {@code null} values can be skipped). It is
 * intended for use in tests and classes like {@link org.firebirdsql.jdbc.FBDatabaseMetaData}.
 * </p>
 * <p>
 * The main advantage over {@link RowValue#of(RowDescriptor, byte[][])} is that it is clearer to which field
 * the value is assigned, and it allows for sparse population (ie: skipping {@code null} values).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see org.firebirdsql.gds.ng.fields.RowValue#of(RowDescriptor, byte[][])
 */
public class RowValueBuilder {

    private final RowDescriptor rowDescriptor;
    private final DatatypeCoder datatypeCoder;
    private RowValue rowValue;

    private int currentIndex;

    /**
     * Creates instance of RowValueBuilder.
     *
     * @param rowDescriptor
     *         The RowDescriptor for the row(s) to be created
     */
    public RowValueBuilder(RowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
        rowValue = rowDescriptor.createDefaultFieldValues();
        datatypeCoder = rowDescriptor.getDatatypeCoder();
    }

    /**
     * Sets the index of the current field to populate.
     *
     * @param index
     *         Index of the field
     * @return this builder
     * @throws IndexOutOfBoundsException
     *         When {@code index} is not between 0 (inclusive) and {@link #getSize()} (exclusive)
     */
    public RowValueBuilder setFieldIndex(int index) {
        checkBounds(index);
        currentIndex = index;
        return this;
    }

    /**
     * Convenience shortcut for {@link #setFieldIndex(int)}.
     *
     * @param index
     *         Index
     * @return this builder
     * @see #setFieldIndex(int)
     */
    public RowValueBuilder at(int index) {
        return setFieldIndex(index);
    }

    /**
     * Sets the field data of the current field.
     *
     * @param fieldData
     *         Data
     * @return this builder
     */
    public RowValueBuilder set(byte[] fieldData) {
        rowValue.setFieldData(currentIndex, fieldData);
        return this;
    }

    /**
     * Sets the field data by encoding the provided {@code int}.
     *
     * @param value
     *         value
     * @return this builder
     * @since 5
     */
    public RowValueBuilder setInt(int value) {
        return set(datatypeCoder.encodeInt(value));
    }

    /**
     * Sets the field data by encoding the provided {@code Number} or {@code null}.
     *
     * @param value
     *         value
     * @return this builder
     * @since 5
     */
    public RowValueBuilder setInt(Number value) {
        return set(value != null ? datatypeCoder.encodeInt(value.intValue()) : null);
    }

    /**
     * Sets the field data by encoding the provided {@code int} as a {@code short}.
     *
     * @param value
     *         value
     * @return this builder
     * @see #setShort(short)
     * @since 5
     */
    public RowValueBuilder setShort(int value) {
        return set(datatypeCoder.encodeShort(value));
    }

    /**
     * Sets the field data by encoding the provided {@code Number} as a {@code short} or {@code null}.
     *
     * @param value
     *         value
     * @return this builder
     * @see #setShort(short)
     * @since 5
     */
    public RowValueBuilder setShort(Number value) {
        return set(value != null ? datatypeCoder.encodeShort(value.intValue()) : null);
    }

    /**
     * Sets the field data by encoding the provided {@code short}.
     *
     * @param value
     *         value
     * @return this builder
     * @see #setShort(int)
     * @since 5
     */
    public RowValueBuilder setShort(short value) {
        return set(datatypeCoder.encodeShort(value));
    }

    /**
     * Sets the field data by encoding the provided {@code String}.
     *
     * @param value
     *         value
     * @return this builder
     * @since 5
     */
    public RowValueBuilder setString(String value) {
        return set(value != null ? datatypeCoder.encodeString(value) : null);
    }

    /**
     * Retrieves the field data set at the specified index.
     *
     * @param index
     *         Index
     * @return The field data
     * @throws java.lang.IndexOutOfBoundsException
     *         When @{code index} is not between 0 (inclusive) and {@link #getSize()} (exclusive)
     */
    public byte[] get(int index) {
        checkBounds(index);
        return rowValue.getFieldData(index);
    }

    /**
     * Resets this builder to a new RowValue. All previous values set are cleared.
     *
     * @return this builder.
     */
    public RowValueBuilder reset() {
        rowValue = rowDescriptor.createDefaultFieldValues();
        return this;
    }

    /**
     * @return Number of fields in the row
     */
    public int getSize() {
        return rowDescriptor.getCount();
    }

    /**
     * Returns the populated {@link RowValue} and resets the RowValueBuilder.
     *
     * @param initialize
     *         {@code true} set field data to null for all uninitialized fields, {@code false} leaves fields
     *         uninitialized. In most cases you want to use {@code true}.
     * @return The row value object
     * @see #reset()
     */
    public RowValue toRowValue(boolean initialize) {
        try {
            if (initialize) {
                rowValue.initializeFields();
            }
            return rowValue;
        } finally {
            reset();
        }
    }

    private void checkBounds(int index) {
        if (index < 0 || index >= rowDescriptor.getCount()) {
            throw new IndexOutOfBoundsException(String.format(
                    "The index '%d' exceeds the expected size (%d) of this RowDescriptorBuilder",
                    index, rowDescriptor.getCount()));
        }
    }

}

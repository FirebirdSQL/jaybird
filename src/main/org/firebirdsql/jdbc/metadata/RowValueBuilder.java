// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * @author Mark Rotteveel
 * @see org.firebirdsql.gds.ng.fields.RowValue#of(RowDescriptor, byte[][])
 */
final class RowValueBuilder {

    private final RowDescriptor rowDescriptor;
    private final DatatypeCoder datatypeCoder;
    // The number of 50 for number of encoded strings to cache is pretty much an arbitrary choice
    private final LruEncodedStringCache stringCache = new LruEncodedStringCache(50);
    private RowValue rowValue;

    private int currentIndex;

    /**
     * Creates instance of RowValueBuilder.
     *
     * @param rowDescriptor
     *         The RowDescriptor for the row(s) to be created
     */
    RowValueBuilder(RowDescriptor rowDescriptor) {
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
    RowValueBuilder setFieldIndex(int index) {
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
    RowValueBuilder at(int index) {
        return setFieldIndex(index);
    }

    /**
     * Sets the field data of the current field.
     *
     * @param fieldData
     *         Data
     * @return this builder
     */
    RowValueBuilder set(byte @Nullable [] fieldData) {
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
    RowValueBuilder setInt(int value) {
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
    RowValueBuilder setInt(@Nullable Number value) {
        if (value != null) {
            return setInt(value.intValue());
        } else {
            return set(null);
        }
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
    RowValueBuilder setShort(int value) {
        return setShort((short) value);
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
    RowValueBuilder setShort(@Nullable Number value) {
        if (value != null) {
            return setShort(value.shortValue());
        } else {
            return set(null);
        }
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
    RowValueBuilder setShort(short value) {
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
    RowValueBuilder setString(@Nullable String value) {
        return set(value != null
                ? stringCache.computeIfAbsent(value, datatypeCoder::encodeString)
                : null);
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
    byte @Nullable [] get(int index) {
        checkBounds(index);
        return rowValue.getFieldData(index);
    }

    /**
     * Resets this builder to a new RowValue. All previous values set are cleared.
     *
     * @return this builder.
     */
    RowValueBuilder reset() {
        rowValue = rowDescriptor.createDefaultFieldValues();
        return this;
    }

    /**
     * @return Number of fields in the row
     */
    int getSize() {
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
    RowValue toRowValue(boolean initialize) {
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

    @SuppressWarnings("java:S2160")
    private static final class LruEncodedStringCache extends LinkedHashMap<String, byte[]> {

        @Serial
        private static final long serialVersionUID = -901927526404254328L;

        private final int maxCapacity;

        private LruEncodedStringCache(int maxCapacity) {
            super(maxCapacity, 0.75f, true);
            this.maxCapacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > maxCapacity;
        }
    }
}

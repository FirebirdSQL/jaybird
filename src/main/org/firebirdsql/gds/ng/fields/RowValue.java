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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Collection of {@link FieldValue}. Usually a row or set of parameters.
 * <p>
 * A <code>RowValue</code> itself is unmodifiable, but the {@link FieldValue} elements it contains are modifiable!
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class RowValue implements Iterable<FieldValue> {

    public static final RowValue EMPTY_ROW_VALUE = new RowValue(new FieldValue[0]);

    private final FieldValue[] fieldValues;

    /**
     * Creates a new <code>RowValues</code> object.
     * <p>
     * The array is copied, but the {@link FieldValue} elements in it are not
     * </p>
     * <p>
     * The implementation assumes, but does not check that all elements are not null
     * </p>
     *
     * @param fieldValues
     *         Field value elements
     */
    public RowValue(FieldValue[] fieldValues) {
        this.fieldValues = fieldValues.clone();
    }

    /**
     * @return The number of fields.
     */
    public int getCount() {
        return fieldValues.length;
    }

    /**
     * Gets the {@link FieldValue} at the specified (0-based) index.
     *
     * @param index
     *         0-based index of the field
     * @return FieldValue
     * @throws java.lang.IndexOutOfBoundsException
     *         if index is not <code>0 &lt;= index &lt; getCount</code>
     */
    public FieldValue getFieldValue(int index) {
        return fieldValues[index];
    }

    @Override
    public Iterator<FieldValue> iterator() {
        return new RowValuesIterator();
    }

    /**
     * Convenience method to construct a <code>RowValues</code> object with varargs parameters
     *
     * @param fieldValues
     *         Field value elements
     * @return new <code>RowValues</code> object
     */
    public static RowValue of(final FieldValue... fieldValues) {
        return new RowValue(fieldValues);
    }

    /**
     * Convenience method for populating a row value from a RowDescriptor and byte arrays.
     * <p>
     * Note this method, and the similar {@link org.firebirdsql.gds.ng.fields.RowValueBuilder} are mainly intended for
     * use in {@link org.firebirdsql.jdbc.FBDatabaseMetaData}.
     * </p>
     *
     * @param rowDescriptor
     *         The row descriptor
     * @param rowData
     *         An array of byte arrays with the field data.
     * @return new <code>RowValues</code> object
     * @see org.firebirdsql.gds.ng.fields.RowValueBuilder
     */
    public static RowValue of(RowDescriptor rowDescriptor, byte[]... rowData) {
        if (rowDescriptor.getCount() != rowData.length) {
            throw new IllegalArgumentException("Expected RowDescriptor count and rowData length to be the same");
        }
        final RowValue rowValue = rowDescriptor.createDefaultFieldValues();
        for (int i = 0; i < rowData.length; i++) {
            rowValue.getFieldValue(i).setFieldData(rowData[i]);
        }
        return rowValue;
    }

    /**
     * Copies this <code>RowValue</code> and the {@link FieldValue} elements it contains.
     * <p>
     * The {@link FieldValue} elements are copied by use of {@link FieldValue#clone()}.
     * </p>
     * <p>
     * As <code>FieldValue</code> is mutable, it is important to consider whether you need to be able
     * to see modifications to the field data, or if you need fields with the same original data. If the former,
     * pass the original, if the latter use this method to obtain a copy.
     * </p>
     *
     * @return Copy of this object with cloned field values
     * @see FieldValue#clone() For caveats
     */
    public RowValue deepCopy() {
        // Implementation note: I decided not to override clone here because it didn't "feel right"
        FieldValue[] fieldValueCopy = new FieldValue[fieldValues.length];
        for (int i = 0; i < fieldValues.length; i++) {
            fieldValueCopy[i] = fieldValues[i].clone();
        }
        return new RowValue(fieldValueCopy);
    }

    /**
     * Iterator implementation to iterate over the internal array
     */
    private final class RowValuesIterator implements Iterator<FieldValue> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < fieldValues.length;
        }

        @Override
        public FieldValue next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return fieldValues[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() method is not supported");
        }
    }
}

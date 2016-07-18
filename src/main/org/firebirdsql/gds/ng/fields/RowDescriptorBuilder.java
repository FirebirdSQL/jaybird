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

import org.firebirdsql.gds.ng.DatatypeCoder;

/**
 * Builder to construct an immutable {@link RowDescriptor}.
 * <p>
 * The row descriptor is constructed by defining the fields, and using {@link #addField()} to add the current field
 * definition to the row. The field data is then reset (as if {@link #resetField()} was called,
 * to prepare for the next field to add.
 * </p>
 * <p>
 * This class can also be used to construct individual {@link FieldDescriptor} objects.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class RowDescriptorBuilder {

    private final DatatypeCoder datatypeCoder;
    private int type;
    private int subType;
    private int scale;
    private int length;
    private String fieldName;
    private String tableAlias;
    private String originalName;
    private String originalTableName;
    private String ownerName;
    private final FieldDescriptor[] fieldDescriptors;

    private int currentFieldIndex;

    /**
     * Constructs an instance of RowDescriptorBuilder for <tt>size</tt> fields.
     *
     * @param size
     *         Number of fields
     * @param datatypeCoder
     *         DatatypeCoder for decoding field data
     */
    public RowDescriptorBuilder(final int size, DatatypeCoder datatypeCoder) {
        this.datatypeCoder = datatypeCoder;
        fieldDescriptors = new FieldDescriptor[size];
    }

    /**
     * @return Number of fields the row will hold.
     */
    public int getSize() {
        return fieldDescriptors.length;
    }

    /**
     * Set the Firebird data type of the field.
     *
     * @param type
     *         Data type
     * @return this builder
     */
    public RowDescriptorBuilder setType(final int type) {
        this.type = type;
        return this;
    }

    /**
     * Set the Firebird sub type of the field.
     *
     * @param subType
     *         Sub type
     * @return this builder
     * @see #setType(int)
     */
    public RowDescriptorBuilder setSubType(final int subType) {
        this.subType = subType;
        return this;
    }

    /**
     * Set the scale of the field.
     *
     * @param scale
     *         Scale
     * @return this builder
     * @see #setType(int)
     */
    public RowDescriptorBuilder setScale(final int scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Set the defined length of the field.
     *
     * @param length
     *         Defined (maximum) length of the field
     * @return this builder
     */
    public RowDescriptorBuilder setLength(final int length) {
        this.length = length;
        return this;
    }

    /**
     * Sets the (aliased) field name.
     *
     * @param fieldName
     *         The field name
     * @return this builder
     * @see #setOriginalName(String)
     */
    public RowDescriptorBuilder setFieldName(final String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * Sets the alias of the underlying table.
     *
     * @param tableAlias
     *         The table alias
     * @return this builder
     * @see #setOriginalTableName(String)
     */
    public RowDescriptorBuilder setTableAlias(final String tableAlias) {
        this.tableAlias = tableAlias;
        return this;
    }

    /**
     * Sets the original field name.
     *
     * @param originalName
     *         The original field name
     * @return this builder
     * @see #setFieldName(String)
     */
    public RowDescriptorBuilder setOriginalName(final String originalName) {
        this.originalName = originalName;
        return this;
    }

    /**
     * Sets the original name of the underlying table.
     *
     * @param originalTableName
     *         The table name
     * @return this builder
     * @see #setTableAlias(String)
     */
    public RowDescriptorBuilder setOriginalTableName(final String originalTableName) {
        this.originalTableName = originalTableName;
        return this;
    }

    /**
     * Sets the field index for the current field under construction.
     * <p>
     * Even though {@link #addField()} increments the current field index, it is advisable to always explicitly
     * set the index using this method or {@link #at(int)} as it improves readability.
     * </p>
     *
     * @param index
     *         Index of the field
     * @return this builder
     * @throws IndexOutOfBoundsException
     *         When <code>index</code> is not between 0 (inclusive) and {@link #getSize()} (exclusive)
     * @throws IllegalStateException
     *         When a {@link FieldDescriptor} is already defined on the specified <code>index</code>
     */
    public RowDescriptorBuilder setFieldIndex(final int index) {
        if (index < 0 || index >= fieldDescriptors.length) {
            throw new IndexOutOfBoundsException(String.format("The index '%d' exceeds the expected size (%d) of this RowDescriptorBuilder", index, fieldDescriptors.length));
        }
        if (fieldDescriptors[index] != null) {
            throw new IllegalStateException(String.format("There is already a field at index '%d'", index));
        }
        currentFieldIndex = index;
        return this;
    }

    /**
     * Convenience shortcut for {@link #setFieldIndex(int)}.
     *
     * @param index
     *         Index of the field
     * @return this builder
     * @see #setFieldIndex(int)
     */
    public RowDescriptorBuilder at(final int index) {
        return setFieldIndex(index);
    }

    /**
     * @return The index for the current field
     */
    public int getCurrentFieldIndex() {
        return currentFieldIndex;
    }

    /**
     * Sets the owner (database username) of the field.
     *
     * @param ownerName
     *         Name of the owner
     * @return this builder
     */
    public RowDescriptorBuilder setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    /**
     * Convenience method to populate the basic field information used in metadata result sets (eg for use in
     * {@link org.firebirdsql.jdbc.FBDatabaseMetaData}).
     *
     * @param type
     *         Firebird data type
     * @param length
     *         Defined (maximum) length of the field
     * @param originalName
     *         The original field name
     * @param originalTableName
     *         The table name
     * @return this builder
     * @see #setType(int)
     * @see #setLength(int)
     * @see #setOriginalTableName(String)
     * @see #setOriginalName(String)
     */
    public RowDescriptorBuilder simple(final int type, final int length, final String originalName,
            final String originalTableName) {
        this.type = type;
        this.length = length;
        this.originalName = originalName;
        this.originalTableName = originalTableName;
        return this;
    }

    /**
     * Creates a {@link FieldDescriptor} based on the current field data of this RowDescriptorBuilder.
     *
     * @return FieldDescriptor
     */
    public FieldDescriptor toFieldDescriptor() {
        return new FieldDescriptor(currentFieldIndex, datatypeCoder, type, subType, scale, length, fieldName,
                tableAlias, originalName, originalTableName, ownerName);
    }

    /**
     * Resets the fields of this builder to the Java defaults.
     */
    public RowDescriptorBuilder resetField() {
        type = 0;
        subType = 0;
        scale = 0;
        length = 0;
        fieldName = null;
        tableAlias = null;
        originalName = null;
        originalTableName = null;
        ownerName = null;
        return this;
    }

    /**
     * Set this builder with the values of the source {@link FieldDescriptor} for further modification through this builder.
     *
     * @param sourceFieldDescriptor
     *         Source for the initial values
     * @return this builder
     */
    public RowDescriptorBuilder copyFieldFrom(final FieldDescriptor sourceFieldDescriptor) {
        type = sourceFieldDescriptor.getType();
        subType = sourceFieldDescriptor.getSubType();
        scale = sourceFieldDescriptor.getScale();
        length = sourceFieldDescriptor.getLength();
        fieldName = sourceFieldDescriptor.getFieldName();
        tableAlias = sourceFieldDescriptor.getTableAlias();
        originalName = sourceFieldDescriptor.getOriginalName();
        originalTableName = sourceFieldDescriptor.getOriginalTableName();
        ownerName = sourceFieldDescriptor.getOwnerName();
        return this;
    }

    /**
     * Adds the current field data to the row and prepares this builder for the next field by resetting all values.
     *
     * @return this builder
     * @see #resetField()
     */
    public RowDescriptorBuilder addField() {
        return addField(toFieldDescriptor()).resetField();
    }

    /**
     * Adds the {@link FieldDescriptor} on the current fieldIndex as the next in the row, and increments the current
     * field index by 1.
     * <p>
     * This method does not call {@link #resetField()}, so a partial definition of a field can exist
     * inside this builder after calling this method.
     * </p>
     *
     * @param fieldDescriptor
     *         FieldDescriptor to add
     * @return this builder
     */
    public RowDescriptorBuilder addField(final FieldDescriptor fieldDescriptor) {
        if (currentFieldIndex >= fieldDescriptors.length) {
            throw new IndexOutOfBoundsException(String.format("The index '%d' exceeds the expected size (%d) of this RowDescriptorBuilder", currentFieldIndex, fieldDescriptors.length));
        }
        fieldDescriptors[currentFieldIndex] = fieldDescriptor;
        currentFieldIndex += 1;
        return this;
    }

    /**
     * Constructs the {@link RowDescriptor} with the current content.
     * <p>
     * This method can also return a partially filled {@link RowDescriptor}. Caller can check for completeness by
     * calling {@link #isComplete()}.
     * </p>
     *
     * @return RowDescriptor instance.
     * @see #isComplete()
     */
    public RowDescriptor toRowDescriptor() {
        // NOTE: The correctness of this depends on the fact that RowDescriptor copies the content of the array
        return RowDescriptor.createRowDescriptor(fieldDescriptors, datatypeCoder);
    }

    /**
     * @return <tt>true</tt> when all {@link FieldDescriptor} entries have been defined
     */
    public boolean isComplete() {
        for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
            if (fieldDescriptor == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Returns the index of the first unprocessed FieldDescriptor, or {@link #getSize()} if all fields have been set.
     */
    public int getFirstUnprocessedIndex() {
        for (int idx = 0; idx < fieldDescriptors.length; idx++) {
            if (fieldDescriptors[idx] == null) {
                return idx;
            }
        }
        return getSize();
    }
}

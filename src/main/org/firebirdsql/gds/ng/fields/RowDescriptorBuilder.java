/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.fields;

/**
 * Builder to construct an immutable {@link RowDescriptor}.
 * <p>
 * The row is constructed by defining the fields, and using {@link #addField()} to add the current field
 * definition to the row. The field data is then reset (as if {@link #resetField()} was called,
 * to prepare for the next field to add.
 * </p>
 * <p>
 * This class can also be used to construct individual {@link FieldDescriptor} objects.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class RowDescriptorBuilder {

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
     */
    public RowDescriptorBuilder(int size) {
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

    public RowDescriptorBuilder setFieldIndex(int index) {
        if (index >= fieldDescriptors.length) {
            throw new IndexOutOfBoundsException(String.format("The index '%d' exceeds the expected size (%d) of this RowDescriptorBuilder", index, fieldDescriptors.length));
        }
        if (fieldDescriptors[index] != null) {
            throw new IllegalStateException(String.format("There is already a field at index '%d'", index));
        }
        currentFieldIndex = index;
        return this;
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
     * Creates a {@link FieldDescriptor} based on the current field data of this RowDescriptorBuilder.
     *
     * @return FieldDescriptor
     */
    public FieldDescriptor toFieldDescriptor() {
        return new FieldDescriptor(type, subType, scale, length, fieldName, tableAlias, originalName, originalTableName, ownerName);
    }

    /**
     * Resets the fields of this builder the Java defaults.
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
     * Set this builder with the values of the source {@link FieldDescriptor}.
     *
     * @param sourceFieldDescriptor
     *         Source for the initial values
     * @return this builder
     */
    public RowDescriptorBuilder copyFieldFrom(FieldDescriptor sourceFieldDescriptor) {
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
     */
    public RowDescriptorBuilder addField() {
        return addField(toFieldDescriptor()).resetField();
    }

    /**
     * Adds the {@link FieldDescriptor} as the next in the row
     *
     * @param fieldDescriptor
     *         FieldDescriptor to add
     * @return this builder
     */
    public RowDescriptorBuilder addField(FieldDescriptor fieldDescriptor) {
        if (currentFieldIndex >= fieldDescriptors.length) {
            throw new IndexOutOfBoundsException(String.format("The index '%d' exceeds the expected size (%d) of this RowDescriptorBuilder", currentFieldIndex, fieldDescriptors.length));
        }
        fieldDescriptors[currentFieldIndex] = fieldDescriptor;
        currentFieldIndex += 1;
        return this;
    }

    /**
     * Constructs the {@link RowDescriptor}.
     *
     * @return RowDescriptor instance.
     */
    public RowDescriptor toRowDescriptor() {
        // NOTE: The correctness of this depends on the fact that RowDescriptor copies the content of the list
        return new RowDescriptor(fieldDescriptors);
    }

    /**
     * @return <tt>true</tt> when all {@link FieldDescriptor} entries have been defined
     */
    public boolean isComplete() {
        for (int idx = 0; idx < fieldDescriptors.length; idx++) {
            if (fieldDescriptors[idx] == null) {
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

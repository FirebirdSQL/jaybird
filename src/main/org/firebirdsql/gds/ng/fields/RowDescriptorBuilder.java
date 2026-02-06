// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class RowDescriptorBuilder {

    private final DatatypeCoder datatypeCoder;
    private int type;
    private int subType;
    private int scale;
    private int length;
    private @Nullable String fieldName;
    private @Nullable String tableAlias;
    private @Nullable String originalName;
    private @Nullable String originalSchema;
    private @Nullable String originalTableName;
    private @Nullable String ownerName;
    private final @Nullable FieldDescriptor[] fieldDescriptors;

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
     * Set the Firebird subtype of the field.
     *
     * @param subType
     *         Subtype
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
    public RowDescriptorBuilder setFieldName(@Nullable String fieldName) {
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
    public RowDescriptorBuilder setTableAlias(@Nullable String tableAlias) {
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
    public RowDescriptorBuilder setOriginalName(@Nullable String originalName) {
        this.originalName = originalName;
        return this;
    }

    /**
     * Sets the original schema of the underlying table.
     *
     * @param originalSchema
     *         The schema of the table
     * @return this builder
     */
    public RowDescriptorBuilder setOriginalSchema(@Nullable String originalSchema) {
        this.originalSchema = originalSchema;
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
    public RowDescriptorBuilder setOriginalTableName(@Nullable String originalTableName) {
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
    public RowDescriptorBuilder setOwnerName(@Nullable String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    /**
     * Convenience method to populate the basic field information used in metadata result sets (e.g. for use in
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
                tableAlias, originalName, originalSchema, originalTableName, ownerName);
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
        originalSchema = null;
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
        originalSchema = sourceFieldDescriptor.getOriginalSchema();
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
     * This method throws an {@code IllegalStateException} if one or more fields are not defined. Caller can check for
     * completeness by calling {@link #isComplete()}.
     * </p>
     *
     * @return RowDescriptor instance
     * @throws IllegalStateException
     *         if one or more fields have not been defined
     * @see #isComplete()
     */
    public RowDescriptor toRowDescriptor() {
        int[] missingFields = IntStream.range(0, getSize())
                .filter(idx -> fieldDescriptors[idx] == null)
                .toArray();
        if (missingFields.length == 0) {
            //noinspection NullableProblems : Covered by missingFields check
            return new RowDescriptor(List.of(fieldDescriptors), datatypeCoder);
        } else {
            throw new IllegalStateException(
                    "Fields at indices %s have not been defined".formatted(Arrays.toString(missingFields)));
        }
    }

    /**
     * @return <tt>true</tt> when all {@link FieldDescriptor} entries have been defined
     */
    public boolean isComplete() {
        return Arrays.stream(fieldDescriptors).noneMatch(Objects::isNull);
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

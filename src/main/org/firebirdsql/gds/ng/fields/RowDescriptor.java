// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The class {@code RowDescriptor} is a java mapping of the XSQLDA server data structure used to describe the row
 * metadata of one row for input or output.
 * <p>
 * RowDescriptor is immutable, values of a row are maintained separately in a {@link RowValue}.
 * </p>
 *
 * @author Mark Rotteveel
 * @version 3.0
 */
public final class RowDescriptor implements Iterable<FieldDescriptor> {

    private static final List<FieldDescriptor> NO_DESCRIPTORS = List.of();
    private final List<FieldDescriptor> fieldDescriptors;
    private final DatatypeCoder datatypeCoder;
    private int hash;

    /**
     * Creates an instance of {@code RowDescriptor} with the supplied array of {@link FieldDescriptor} instances.
     *
     * @param fieldDescriptors
     *         field descriptors (list is copied before use)
     * @param datatypeCoder
     *         datatype coder for the connection that uses this row descriptor
     * @since 7
     */
    public RowDescriptor(List<FieldDescriptor> fieldDescriptors, DatatypeCoder datatypeCoder) {
        this.fieldDescriptors = List.copyOf(fieldDescriptors);
        this.datatypeCoder = requireNonNull(datatypeCoder, "dataTypeCoder should not be null");
    }

    /**
     * @return The {@link org.firebirdsql.gds.ng.DatatypeCoder}.
     */
    public DatatypeCoder getDatatypeCoder() {
        return datatypeCoder;
    }

    /**
     * @return The {@link org.firebirdsql.encodings.IEncodingFactory}.
     */
    public IEncodingFactory getEncodingFactory() {
        return datatypeCoder.getEncodingFactory();
    }

    /**
     * @return The number of fields.
     */
    public int getCount() {
        return fieldDescriptors.size();
    }

    /**
     * Gets the {@link FieldDescriptor} at the specified (0-based) index.
     *
     * @param index
     *         0-based index of the field
     * @return FieldDescriptor
     * @throws java.lang.IndexOutOfBoundsException
     *         if index is not {@code 0 <= index < getCount}
     */
    public FieldDescriptor getFieldDescriptor(int index) {
        return fieldDescriptors.get(index);
    }

    /**
     * @return An immutable List of the {@link FieldDescriptor} instances of this row.
     */
    public List<FieldDescriptor> getFieldDescriptors() {
        return fieldDescriptors;
    }

    /**
     * Creates a {@link RowValue} instance with default ({@code null}) values for each field.
     * <p>
     * All fields are marked as uninitialized.
     * </p>
     * <p>
     * The (0-based) index of each field value in the {@link RowValue} corresponds with the (0-based) index of the
     * {@link FieldDescriptor} within this {@code RowDescriptor}.
     * </p>
     *
     * @return Uninitialized and empty {@code RowValue} instance for a row described by this descriptor.
     * @see RowValue#defaultFor(RowDescriptor) 
     */
    public RowValue createDefaultFieldValues() {
        return RowValue.defaultFor(this);
    }

    /**
     * Creates a <em>deleted row marker</em>.
     * <p>
     * A deleted row marker is used in JDBC result sets for deleted rows, and to discern them from (updated) rows that
     * are simply all {@code NULL}. This is for Jaybird internal implementation needs only.
     * </p>
     *
     * @return Deleted row marker with {@code count} number of rows, all set to null
     */
    public RowValue createDeletedRowMarker() {
        return RowValue.deletedRowMarker(getCount());
    }

    @Override
    public Iterator<FieldDescriptor> iterator() {
        return fieldDescriptors.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RowDescriptor: [");
        for (int idx = 0; idx < fieldDescriptors.size(); idx++) {
            sb.append(idx).append('=');
            FieldDescriptor descriptor = fieldDescriptors.get(idx);
            //noinspection ConstantValue : null-check for robustness and debugging purposes
            if (descriptor != null) {
                descriptor.appendFieldDescriptor(sb);
            } else {
                sb.append("[null]");
            }
            sb.append(',');
        }
        if (!fieldDescriptors.isEmpty()) {
            // Remove last ','
            sb.setLength(sb.length() - 1);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RowDescriptor other)) return false;
        return this.fieldDescriptors.equals(other.fieldDescriptors);
    }

    @Override
    public int hashCode() {
        if (hash != 0) return hash;
        int newHash = fieldDescriptors.hashCode();
        return hash = newHash != 0 ? newHash : 1;
    }

    /**
     * Returns an empty row descriptor with the specified datatype coder.
     *
     * @param datatypeCoder
     *         datatype coder for the connection that uses this row descriptor
     * @return Empty row descriptor
     */
    public static RowDescriptor empty(DatatypeCoder datatypeCoder) {
        return new RowDescriptor(NO_DESCRIPTORS, datatypeCoder);
    }

}

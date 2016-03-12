/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;

import java.util.*;

/**
 * The class {@code RowDescriptor} is a java mapping of the XSQLDA server data structure used to describe the row
 * metadata of one row for input or output.
 * <p>
 * RowDescriptor is an immutable, values of a row are maintained separately in a collection of {@link FieldValue}
 * instances.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 3.0
 */
public final class RowDescriptor implements Iterable<FieldDescriptor> {

    private static final FieldDescriptor[] NO_DESCRIPTORS = new FieldDescriptor[0];
    private final FieldDescriptor[] fieldDescriptors;
    private final DatatypeCoder datatypeCoder;
    private int hash;

    /**
     * Creates an instance of {@code RowDescriptor} with the supplied array of
     * {@link FieldDescriptor} instances.
     *
     * @param fieldDescriptors
     *         The field descriptors (array is cloned before use)
     * @param datatypeCoder
     *         The datatype code for the connection that uses this RowDescriptor.
     */
    private RowDescriptor(final FieldDescriptor[] fieldDescriptors, final DatatypeCoder datatypeCoder) {
        assert datatypeCoder != null : "dataTypeCoder should not be null";
        this.fieldDescriptors = fieldDescriptors.clone();
        this.datatypeCoder = datatypeCoder;
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
        return fieldDescriptors.length;
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
        return fieldDescriptors[index];
    }

    /**
     * @return An unmodifiable List of the {@link FieldDescriptor} instances of this row.
     */
    public List<FieldDescriptor> getFieldDescriptors() {
        return Collections.unmodifiableList(Arrays.asList(fieldDescriptors));
    }

    /**
     * Creates a {@link List} with default {@link FieldValue} instances as returned by
     * {@link FieldDescriptor#createDefaultFieldValue()}.
     * <p>
     * The (0-based) index of the FieldValue in the list corresponds with the (0-based) index of the
     * {@link FieldDescriptor} within this {@code RowDescriptor}.
     * </p>
     *
     * @return Default {@code FieldValue} instances for the {@code FieldDescriptor} instance contained in this
     * instance.
     */
    public RowValue createDefaultFieldValues() {
        if (getCount() == 0) return RowValue.EMPTY_ROW_VALUE;
        FieldValue[] fieldValues = new FieldValue[getCount()];
        for (int i = 0; i < fieldDescriptors.length; i++) {
            fieldValues[i] = fieldDescriptors[i].createDefaultFieldValue();
        }
        return new RowValue(fieldValues);
    }

    @Override
    public Iterator<FieldDescriptor> iterator() {
        return new RowDescriptorIterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RowDescriptor: [");
        for (int idx = 0; idx < fieldDescriptors.length; idx++) {
            sb.append(idx).append('=');
            FieldDescriptor descriptor = fieldDescriptors[idx];
            if (descriptor != null) {
                descriptor.appendFieldDescriptor(sb);
            } else {
                sb.append("[null]");
            }
            sb.append(',');
        }
        if (fieldDescriptors.length > 0) {
            // Remove last ','
            sb.setLength(sb.length() - 1);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RowDescriptor)) return false;
        RowDescriptor other = (RowDescriptor) obj;
        return Arrays.equals(this.fieldDescriptors, other.fieldDescriptors);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Arrays.hashCode(this.fieldDescriptors);
        }
        return hash;
    }

    /**
     * Creates an instance of {@code RowDescriptor} with the supplied {@link FieldDescriptor} instances.
     *
     * @param fieldDescriptors
     *         The field descriptors (array is cloned before use)
     * @param datatypeCoder
     *         he datatype code for the connection that uses this RowDescriptor.
     * @return {@code RowDescriptor} instance
     */
    public static RowDescriptor createRowDescriptor(final FieldDescriptor[] fieldDescriptors,
            DatatypeCoder datatypeCoder) {
        if (fieldDescriptors.length == 0) return empty(datatypeCoder);
        return new RowDescriptor(fieldDescriptors, datatypeCoder);
    }

    /**
     * Returns an empty row descriptor with the specified datatype coder.
     *
     * @param datatypeCoder
     *         The datatype code for the connection that uses this RowDescriptor.
     * @return Empty row descriptor
     */
    public static RowDescriptor empty(final DatatypeCoder datatypeCoder) {
        return new RowDescriptor(NO_DESCRIPTORS, datatypeCoder);
    }

    /**
     * Iterator implementation to iterate over the internal array
     */
    private class RowDescriptorIterator implements Iterator<FieldDescriptor> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < fieldDescriptors.length;
        }

        @Override
        public FieldDescriptor next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return fieldDescriptors[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() method is not supported");
        }
    }
}

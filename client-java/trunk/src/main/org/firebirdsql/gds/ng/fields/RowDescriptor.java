/*
 * $Id$
 *
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

import java.util.*;

/**
 * The class <code>RowDescriptor</code> is a java mapping of the XSQLDA server
 * data structure used to describe the row metadata of one row for input or output.
 * <p>
 * RowDescriptor is an immutable, values of a row are maintained separately in a collection of {@link FieldValue} instances. The only exception
 * is the cached value for the calculated blr.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 2.3
 */
public final class RowDescriptor implements Iterable<FieldDescriptor> {

    public static final RowDescriptor EMPTY = new RowDescriptor(new FieldDescriptor[0]);
    public static final List<FieldValue> EMPTY_FIELD_VALUES = Collections.emptyList();

    private final FieldDescriptor[] fieldDescriptors;
    private int hash;

    /**
     * Creates an instance of <code>RowDescriptor</code> with the supplied array of
     * {@link FieldDescriptor} instances.
     *
     * @param fieldDescriptors
     *         The field descriptors (array is cloned before use)
     */
    private RowDescriptor(final FieldDescriptor[] fieldDescriptors) {
        this.fieldDescriptors = fieldDescriptors.clone();
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
     * Creates a {@link List} with default {@link FieldValue} instances as returned by {@link FieldDescriptor#createDefaultFieldValue()}.
     * <p>
     * The (0-based) index of the FieldValue in the list corresponds with the (0-based) index of the {@link FieldDescriptor}
     * within this <code>RowDescriptor</code>.
     * </p>
     *
     * @return Default <code>FieldValue</code> instances for the <code>FieldDescriptor</code> instance contained in this instance.
     */
    public List<FieldValue> createDefaultFieldValues() {
        if (getCount() == 0) return EMPTY_FIELD_VALUES;
        List<FieldValue> fieldValues = new ArrayList<FieldValue>(getCount());
        for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
            fieldValues.add(fieldDescriptor.createDefaultFieldValue());
        }
        return fieldValues;
    }

    @Override
    public Iterator<FieldDescriptor> iterator() {
        return getFieldDescriptors().iterator();
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
     * Creates an instance of <code>RowDescriptor</code> with the supplied {@link FieldDescriptor} instances.
     *
     * @param fieldDescriptors
     *         The field descriptors (array is cloned before use)
     * @return <code>RowDescriptor</code> instance
     */
    public static RowDescriptor createRowDescriptor(final FieldDescriptor[] fieldDescriptors) {
        if (fieldDescriptors.length == 0) return EMPTY;
        return new RowDescriptor(fieldDescriptors);
    }
}

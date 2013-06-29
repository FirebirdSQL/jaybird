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
 * data structure used to represent one row for input or output. RowDescriptor is an immutable type.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 2.3
 */
public class RowDescriptor implements Iterable<FieldDescriptor> {

    public static final RowDescriptor EMPTY = new RowDescriptor(new FieldDescriptor[0]);

    private final FieldDescriptor[] fieldDescriptors;

    public RowDescriptor(FieldDescriptor[] fieldDescriptors) {
        this.fieldDescriptors = fieldDescriptors.clone();
    }

    /**
     * @return The number of columns.
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
}

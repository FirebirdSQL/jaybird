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

import java.sql.SQLException;

/**
 * Interface for calculating the blr (binary language representation) of a row.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface BlrCalculator {

    /**
     * Calculates the blr for the row descriptor.
     *
     * @param rowDescriptor
     *         Row descriptor
     * @return Byte array with the blr
     * @throws SQLException
     *         When the {@link RowDescriptor} contains an unsupported field type.
     */
    byte[] calculateBlr(RowDescriptor rowDescriptor) throws SQLException;

    /**
     * Calculates the blr for a specific row value.
     * <p>
     * This allows to optimize for the actual length of the field.
     * </p>
     *
     * @param rowDescriptor
     *         Row descriptor
     * @param rowValue
     *         Row value
     * @return Byte array with the blr
     * @throws SQLException
     *         When the {@link RowValue} contains an unsupported field type.
     */
    byte[] calculateBlr(RowDescriptor rowDescriptor, RowValue rowValue) throws SQLException;

    /**
     * Calculates the io length for the field descriptor.
     * <p>
     * The return value indicates the length and padding of the type in the buffer
     * <ul>
     * <li>&lt; 0 : Type is of specified length * -1 and not padded</li>
     * <li>== 0 : Type is of dynamic length (which is specified in the buffer as an integer) and padded</li>
     * <li>&gt; 0 : Type is of specified length minus 1 (subtracting 1 is required to avoid 0 for types of zero length) and padded</li>
     * </ul>
     * </p>
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @return The io length
     */
    int calculateIoLength(FieldDescriptor fieldDescriptor) throws SQLException;

    /**
     * Calculates the io length for the field descriptor and actual data.
     * <p>
     * The return value indicates the length and padding of the type in the buffer
     * <ul>
     * <li>&lt; 0 : Type is of specified length * -1 and not padded</li>
     * <li>== 0 : Type is of dynamic length (which is specified in the buffer as an integer) and padded</li>
     * <li>&gt; 0 : Type is of specified length minus 1 (subtracting 1 is required to avoid 0 for types of zero length)
     * nd padded</li>
     * </ul>
     * </p>
     * <p>
     * This allows to optimize for the actual length of the field.
     * </p>
     * <p>
     * For <code>CHAR</code> ({@link org.firebirdsql.gds.ISCConstants#SQL_TEXT} the implementation should be consistent
     * with the lengths as given by {@link #calculateBlr(RowDescriptor, RowValue)}.
     * </p>
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @param fieldData
     *         byte array (can be {@code null}) with field data.
     * @return The io length
     * @since 4.0
     */
    int calculateIoLength(FieldDescriptor fieldDescriptor, byte[] fieldData) throws SQLException;
    
}

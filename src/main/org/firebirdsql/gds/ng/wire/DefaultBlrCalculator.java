/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.*;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import static org.firebirdsql.gds.BlrConstants.*;
import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Default BLR calculator for the wire protocol.
 * <p>
 * Most likely this can be used without change for other protocol versions as well, although we may need to investigate
 * the TODOs specified in {@link #calculateBlr(org.firebirdsql.gds.ng.fields.RowDescriptor)}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class DefaultBlrCalculator implements BlrCalculator {

    /**
     * Cached instance of {@link DefaultBlrCalculator} for dialect 3 databases.
     */
    public static final DefaultBlrCalculator CALCULATOR_DIALECT_3 = new DefaultBlrCalculator((short) 3);

    private final short dialect;

    public DefaultBlrCalculator(short dialect) {
        this.dialect = dialect;
    }

    @Override
    public byte[] calculateBlr(RowDescriptor rowDescriptor) throws SQLException {
        final ByteArrayOutputStream bout = getByteArrayOutputStream(rowDescriptor.getCount());

        for (FieldDescriptor field : rowDescriptor) {
            calculateFieldBlr(bout, field, field.getLength());
        }

        bout.write(blr_end);
        bout.write(blr_eoc);

        return bout.toByteArray();
    }

    @Override
    public byte[] calculateBlr(RowDescriptor rowDescriptor, RowValue rowValue) throws SQLException {
        final ByteArrayOutputStream bout = getByteArrayOutputStream(rowValue.getCount());

        for (int idx = 0; idx < rowDescriptor.getCount(); idx++) {
            final byte[] fieldData = rowValue.getFieldData(idx);
            final FieldDescriptor field = rowDescriptor.getFieldDescriptor(idx);
            final int actualDataLength = fieldData != null ? fieldData.length : 0;
            calculateFieldBlr(bout, field, actualDataLength);
        }

        bout.write(blr_end);
        bout.write(blr_eoc);

        return bout.toByteArray();
    }

    /**
     * Creates a byte array stream and writes the header for the specified number of fields.
     *
     * @param fieldCount Number of fields.
     * @return Byte array OutputStream with header already written
     */
    private ByteArrayOutputStream getByteArrayOutputStream(final int fieldCount) {
        // Approximate size for the blr to allocate an array with a sufficiently large buffer, so no resize is needed
        // 8 bytes: 6 bytes header + 2 bytes tail
        // 7 bytes per field: 5 bytes maximum required + 2 bytes for the null indicator and end of parameter
        final int approximateSize = 8 + 7 * fieldCount;
        final ByteArrayOutputStream bout = new ByteArrayOutputStream(approximateSize);
        final int parameterCount = 2 * fieldCount; // 1 actual field, 1 null descriptor (?)

        bout.write(dialect <= 1 ? blr_version4 : blr_version5);
        bout.write(blr_begin);
        bout.write(blr_message);
        bout.write(0);
        bout.write(parameterCount);
        bout.write(parameterCount >> 8);

        return bout;
    }

    /**
     * Calculates the blr for a single field.
     *
     * @param bout
     *         byte array OutputStream
     * @param field
     *         field descriptor
     * @param len
     *         length to use for SQL_VARYING and SQL_TEXT
     * @throws SQLException
     *         when {@code field} has an unknown type
     */
    private void calculateFieldBlr(final ByteArrayOutputStream bout, final FieldDescriptor field, final int len)
            throws SQLException {
        final int fieldType = field.getType() & ~1;
        switch (fieldType) {
        case SQL_VARYING:
            bout.write(blr_varying2);
            bout.write(field.getSubType());
            // Formally bout.write(field.getSubType() >> 8);, but that would be the collation id, which is not relevant
            bout.write(0);
            bout.write(len);
            bout.write(len >> 8);
            break;
        case SQL_TEXT:
            bout.write(blr_text2);
            bout.write(field.getSubType());
            // Formally bout.write(field.getSubType() >> 8);, but that would be the collation id, which is not relevant
            bout.write(0);
            bout.write(len);
            bout.write(len >> 8);
            break;
        case SQL_NULL:
            bout.write(blr_text);
            bout.write(0);
            bout.write(0);
            break;
        case SQL_DOUBLE:
            bout.write(blr_double);
            break;
        case SQL_FLOAT:
            bout.write(blr_float);
            break;
        case SQL_D_FLOAT:
            bout.write(blr_d_float);
            break;
        case SQL_TYPE_DATE:
            bout.write(blr_sql_date);
            break;
        case SQL_TYPE_TIME:
            bout.write(blr_sql_time);
            break;
        case SQL_TIMESTAMP:
            bout.write(blr_timestamp);
            break;
        case SQL_BLOB:
            // TODO Use blr_blob2 instead; added in 2.5
            bout.write(blr_quad);
            bout.write(0); // scale?
            break;
        case SQL_ARRAY:
            bout.write(blr_quad);
            bout.write(0); // scale?
            break;
        case SQL_LONG:
            bout.write(blr_long);
            bout.write(field.getScale());
            break;
        case SQL_SHORT:
            bout.write(blr_short);
            bout.write(field.getScale());
            break;
        case SQL_INT64:
            bout.write(blr_int64);
            bout.write(field.getScale());
            break;
        case SQL_QUAD:
            bout.write(blr_quad);
            bout.write(field.getScale());
            break;
        case SQL_BOOLEAN:
            bout.write(blr_bool);
            break;
        case SQL_DEC16:
            bout.write(blr_dec64);
            break;
        case SQL_DEC34:
            bout.write(blr_dec128);
            break;
        case SQL_INT128:
            bout.write(blr_int128);
            bout.write(field.getScale());
            break;
        case SQL_TIMESTAMP_TZ:
            bout.write(blr_timestamp_tz);
            break;
        case SQL_TIME_TZ:
            bout.write(blr_sql_time_tz);
            break;
        case SQL_TIMESTAMP_TZ_EX:
            bout.write(blr_ex_timestamp_tz);
            break;
        case SQL_TIME_TZ_EX:
            bout.write(blr_ex_time_tz);
            break;
        default:
            throw new FbExceptionBuilder().exception(isc_dsql_sqlda_value_err).toSQLException();
        }

        bout.write(blr_short); // Null indicator
        bout.write(0); // End of parameter?
    }

    @Override
    public int calculateIoLength(FieldDescriptor fieldDescriptor) throws SQLException {
        switch (fieldDescriptor.getType() & ~1) {
        case SQL_TEXT:
            return fieldDescriptor.getLength() + 1;
        case SQL_VARYING:
            return 0;
        case SQL_SHORT:
        case SQL_LONG:
        case SQL_FLOAT:
        case SQL_TYPE_TIME:
        case SQL_TYPE_DATE:
            return -4;
        case SQL_DOUBLE:
        case SQL_TIMESTAMP:
        case SQL_BLOB:
        case SQL_ARRAY:
        case SQL_QUAD:
        case SQL_INT64:
        case SQL_DEC16:
        case SQL_TIME_TZ:
            return -8;
        case SQL_TIMESTAMP_TZ:
        case SQL_TIME_TZ_EX:
            return -12;
        case SQL_TIMESTAMP_TZ_EX:
        case SQL_DEC34:
        case SQL_INT128:
            return -16;
        case SQL_NULL:
            return 0;
        case SQL_BOOLEAN:
            return 1 + 1;
        default:
            throw new FbExceptionBuilder().exception(isc_dsql_datatype_err).toSQLException();
        }
    }

    @Override
    public int calculateIoLength(FieldDescriptor fieldDescriptor, byte[] fieldData) throws SQLException {
        final int fieldType = fieldDescriptor.getType() & ~1;
        if (fieldType == SQL_TEXT) {
            // Use actual data length for SQL_TEXT
            return (fieldData != null ? fieldData.length : 0) + 1;
        }
        return calculateIoLength(fieldDescriptor);
    }

    // See src/remote/client/BlrFromMessage.cpp method buildBlr
    @Override
    public int calculateBatchMessageLength(RowDescriptor rowDescriptor) throws SQLException {
        int length = 0;
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            int fieldLength = fieldDescriptor.getLength();

            int align;
            switch (fieldDescriptor.getType() & ~1) {
            case SQL_TEXT:
            case SQL_NULL:
            case SQL_BOOLEAN:
                // no align
                align = 1;
                break;
            case SQL_SHORT:
                align = 2;
                break;
            case SQL_VARYING:
                align = 2;
                // varchar length bytes
                fieldLength += 2;
                break;
            case SQL_FLOAT:
            case SQL_LONG:
            case SQL_TYPE_DATE:
            case SQL_TYPE_TIME:
            case SQL_TIMESTAMP:
            case SQL_TIME_TZ_EX:
            case SQL_BLOB:
            case SQL_ARRAY:
            case SQL_QUAD:
            case SQL_TIMESTAMP_TZ_EX:
            case SQL_TIMESTAMP_TZ:
            case SQL_TIME_TZ:
                align = 4;
                break;
            case SQL_DOUBLE:
            case SQL_D_FLOAT:
            case SQL_INT64:
            case SQL_DEC16:
            case SQL_DEC34:
            case SQL_INT128:
                align = 8;
                break;
            default:
                throw new FbExceptionBuilder().exception(isc_dsql_datatype_err).toSQLException();
            }
            if (align > 1) {
                length = blrAlign(length, align);
            }
            length += fieldLength;
            // null-indicator
            length = blrAlign(length, 2) + 2;
        }
        return length;
    }

    /**
     * Length alignment.
     * <p>
     * See {@code FB_ALIGN} in {@code src/include/fb_types.h}.
     * </p>
     *
     * @param length
     *         Current length
     * @param alignment
     *         Alignment
     * @return aligned length
     */
    private int blrAlign(int length, int alignment) {
        return (length + alignment - 1) & -alignment;
    }
}

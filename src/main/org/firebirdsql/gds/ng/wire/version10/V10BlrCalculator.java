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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlrConstants;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

/**
 * BLR Calculator for the version 10 protocol.
 * <p>
 * Most likely this can be used without change for other protocol versions as well, although we may need to investigate
 * the TODOs specified in {@link #calculateBlr(org.firebirdsql.gds.ng.fields.RowDescriptor)}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class V10BlrCalculator implements BlrCalculator {

    /**
     * Cached instance of {@link V10BlrCalculator} for dialect 3 databases.
     */
    public static final V10BlrCalculator CALCULATOR_DIALECT_3 = new V10BlrCalculator((short) 3);

    private final short dialect;

    public V10BlrCalculator(short dialect) {
        this.dialect = dialect;
    }

    @Override
    public byte[] calculateBlr(RowDescriptor rowDescriptor) throws SQLException {
        // TODO Return null for input null or 0 rows?
        // Approximate size for the blr so we can allocate a ByteArray with a sufficiently large buffer, so no resize is needed
        // 8 bytes: 6 bytes header + 2 bytes tail
        // 5 bytes per field: 3 bytes maximum required + 2 bytes for the null indicator and end of parameter
        final int approximateSize = 8 + 5 * rowDescriptor.getCount();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream(approximateSize);
        final int parCount = 2 * rowDescriptor.getCount();

        // TODO Previous Jaybird implementation always wrote blr_version5
        bout.write(dialect <= 1 ? BlrConstants.blr_version4 : BlrConstants.blr_version5);
        bout.write(BlrConstants.blr_begin);
        bout.write(BlrConstants.blr_message);
        bout.write(0);
        bout.write(parCount);
        bout.write(parCount >> 8);

        for (FieldDescriptor field : rowDescriptor) {
            final int dtype = field.getType() & ~1;
            final int len = field.getLength();
            switch (dtype) {
            case ISCConstants.SQL_VARYING:
                // TODO Use blr_varying2 instead, find out which Firebird version added that
                bout.write(BlrConstants.blr_varying);
                bout.write(len);
                bout.write(len >> 8);
                break;
            case ISCConstants.SQL_TEXT:
                // TODO Use blr_text2 instead, find out which Firebird version added that
                bout.write(BlrConstants.blr_text);
                bout.write(len);
                bout.write(len >> 8);
                break;
            case ISCConstants.SQL_NULL:
                bout.write(BlrConstants.blr_text);
                bout.write(0);
                bout.write(0);
                break;
            case ISCConstants.SQL_DOUBLE:
                bout.write(BlrConstants.blr_double);
                break;
            case ISCConstants.SQL_FLOAT:
                bout.write(BlrConstants.blr_float);
                break;
            case ISCConstants.SQL_D_FLOAT:
                bout.write(BlrConstants.blr_d_float);
                break;
            case ISCConstants.SQL_TYPE_DATE:
                bout.write(BlrConstants.blr_sql_date);
                break;
            case ISCConstants.SQL_TYPE_TIME:
                bout.write(BlrConstants.blr_sql_time);
                break;
            case ISCConstants.SQL_TIMESTAMP:
                bout.write(BlrConstants.blr_timestamp);
                break;
            case ISCConstants.SQL_BLOB:
                // TODO Use blr_blob2 instead, find out which Firebird version added that
                bout.write(BlrConstants.blr_quad);
                bout.write(0); // scale?
                break;
            case ISCConstants.SQL_ARRAY:
                bout.write(BlrConstants.blr_quad);
                bout.write(0); // scale?
                break;
            case ISCConstants.SQL_LONG:
                bout.write(BlrConstants.blr_long);
                bout.write(field.getScale());
                break;
            case ISCConstants.SQL_SHORT:
                bout.write(BlrConstants.blr_short);
                bout.write(field.getScale());
                break;
            case ISCConstants.SQL_INT64:
                bout.write(BlrConstants.blr_int64);
                bout.write(field.getScale());
                break;
            case ISCConstants.SQL_QUAD:
                bout.write(BlrConstants.blr_quad);
                bout.write(field.getScale());
                break;
            case ISCConstants.SQL_BOOLEAN:
                bout.write(BlrConstants.blr_bool);
                break;
            default:
                throw new FbExceptionBuilder().exception(ISCConstants.isc_dsql_sqlda_value_err).toSQLException();
            }

            bout.write(BlrConstants.blr_short); // Null indicator
            bout.write(0); // End of parameter?
        }

        bout.write(BlrConstants.blr_end);
        bout.write(BlrConstants.blr_eoc);

        return bout.toByteArray();
    }

    @Override
    public int calculateIoLength(FieldDescriptor fieldDescriptor) throws SQLException {
        int ioLength;
        switch (fieldDescriptor.getType() & ~1) {
        case ISCConstants.SQL_TEXT:
            ioLength = fieldDescriptor.getLength() + 1;
            break;
        case ISCConstants.SQL_VARYING:
            ioLength = 0;
            break;
        case ISCConstants.SQL_SHORT:
        case ISCConstants.SQL_LONG:
        case ISCConstants.SQL_FLOAT:
        case ISCConstants.SQL_TYPE_TIME:
        case ISCConstants.SQL_TYPE_DATE:
            ioLength = -4;
            break;
        // case SQL_D_FLOAT:
        // break;
        case ISCConstants.SQL_DOUBLE:
        case ISCConstants.SQL_TIMESTAMP:
        case ISCConstants.SQL_BLOB:
        case ISCConstants.SQL_ARRAY:
        case ISCConstants.SQL_QUAD:
        case ISCConstants.SQL_INT64:
            ioLength = -8;
            break;
        case ISCConstants.SQL_NULL:
            ioLength = 0;
            break;
        case ISCConstants.SQL_BOOLEAN:
            ioLength = 1 + 1;
            break;
        default:
            throw new FbExceptionBuilder().exception(ISCConstants.isc_dsql_datatype_err).toSQLException();
        }
        return ioLength;
    }
}

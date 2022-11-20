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
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;

/**
 * Provides the implementation for {@link DatabaseMetaData#getTypeInfo()}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class GetTypeInfo {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(18, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, 31, "TYPE_NAME", "TYPEINFO").addField()
            .at(1).simple(SQL_LONG, 0, "DATA_TYPE", "TYPEINFO").addField()
            .at(2).simple(SQL_LONG, 0, "PRECISION", "TYPEINFO").addField()
            .at(3).simple(SQL_VARYING | 1, 1, "LITERAL_PREFIX", "TYPEINFO").addField()
            .at(4).simple(SQL_VARYING | 1, 1, "LITERAL_SUFFIX", "TYPEINFO").addField()
            .at(5).simple(SQL_VARYING | 1, 31, "CREATE_PARAMS", "TYPEINFO").addField()
            .at(6).simple(SQL_SHORT, 0, "NULLABLE", "TYPEINFO").addField()
            .at(7).simple(SQL_TEXT, 1, "CASE_SENSITIVE", "TYPEINFO").addField()
            .at(8).simple(SQL_SHORT, 0, "SEARCHABLE", "TYPEINFO").addField()
            .at(9).simple(SQL_TEXT, 1, "UNSIGNED_ATTRIBUTE", "TYPEINFO").addField()
            .at(10).simple(SQL_TEXT, 1, "FIXED_PREC_SCALE", "TYPEINFO").addField()
            .at(11).simple(SQL_TEXT, 1, "AUTO_INCREMENT", "TYPEINFO").addField()
            .at(12).simple(SQL_VARYING | 1, 31, "LOCAL_TYPE_NAME", "TYPEINFO").addField()
            .at(13).simple(SQL_SHORT, 0, "MINIMUM_SCALE", "TYPEINFO").addField()
            .at(14).simple(SQL_SHORT, 0, "MAXIMUM_SCALE", "TYPEINFO").addField()
            .at(15).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "TYPEINFO").addField()
            .at(16).simple(SQL_LONG | 1, 0, "SQL_DATETIME_SUB", "TYPEINFO").addField()
            .at(17).simple(SQL_LONG, 0, "NUM_PREC_RADIX", "TYPEINFO").addField()
            .toRowDescriptor();

    private final DbMetadataMediator mediator;

    private GetTypeInfo(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    public ResultSet getTypeInfo() throws SQLException {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();

        int blobTypePred = firebirdSupportInfo.supportsFullSearchableBlobs() ? DatabaseMetaData.typeSearchable
                : DatabaseMetaData.typePredBasic;

        //dialect 3 only
        final List<RowValue> rows = new ArrayList<>(23);

        // DECFLOAT=-6001 (TODO Change when standardized)
        if (firebirdSupportInfo.supportsDecfloat()) {
            rows.add(row("DECFLOAT", JaybirdTypeCodes.DECFLOAT, DECFLOAT_34_PRECISION, null, null, "precision", false,
                    DatabaseMetaData.typeSearchable, false, false, 0, SQL_DEC34, 10));
        }

        //BIGINT=-5
        rows.add(row("BIGINT", Types.BIGINT, BIGINT_PRECISION, null, null, null, false, DatabaseMetaData.typeSearchable,
                false, true, 0, SQL_INT64, 10));

        //LONGVARBINARY=-4
        rows.add(row("BLOB SUB_TYPE BINARY", Types.LONGVARBINARY, 0, "x'", "'", null, true, blobTypePred, true, true, 0,
                SQL_BLOB, 10));

        //VARBINARY=-3
        rows.add(row("VARCHAR", Types.VARBINARY, 32765, "x'", "'", "length", true, DatabaseMetaData.typeSearchable,
                true, true, 0, SQL_VARYING, 10));

        //BINARY=-2
        rows.add(row("CHAR", Types.BINARY, 32767, "x'", "'", "length", true, DatabaseMetaData.typeSearchable, true,
                true, 0, SQL_TEXT, 10));

        //LONGVARCHAR=-1
        rows.add(row("BLOB SUB_TYPE TEXT", Types.LONGVARCHAR, 0, "'", "'", null, true, blobTypePred, true, true, 0,
                SQL_BLOB, 10));

        //CHAR=1
        rows.add(row("CHAR", Types.CHAR, 32767, "'", "'", "length", true, DatabaseMetaData.typeSearchable, true, true,
                0, SQL_TEXT, 10));

        // also for numeric
        int maxDecimalPrecision = firebirdSupportInfo.maxDecimalPrecision();
        //NUMERIC=2
        rows.add(row("NUMERIC", Types.NUMERIC, maxDecimalPrecision, null, null, "precision,scale", false,
                DatabaseMetaData.typeSearchable, false, true, maxDecimalPrecision, SQL_INT64, 10));

        // Handle INT128 as a JDBC type NUMERIC
        if (firebirdSupportInfo.supportsInt128()) {
            rows.add(row("INT128", Types.NUMERIC, NUMERIC_INT128_PRECISION, null, null, null, false,
                    DatabaseMetaData.typeSearchable, false, true, 0, SQL_INT128, 10));
        }

        //DECIMAL=3
        rows.add(row("DECIMAL", Types.DECIMAL, maxDecimalPrecision, null, null, "precision,scale", false,
                DatabaseMetaData.typeSearchable, false, true, maxDecimalPrecision, SQL_INT64, 10));

        //INTEGER=4
        rows.add(row("INTEGER", Types.INTEGER, INTEGER_PRECISION, null, null, null, false,
                DatabaseMetaData.typeSearchable, false, true, 0, SQL_LONG, 10));

        //SMALLINT=5
        rows.add(row("SMALLINT", Types.SMALLINT, SMALLINT_PRECISION, null, null, null, false,
                DatabaseMetaData.typeSearchable, false, true, 0, SQL_SHORT, 10));

        boolean supportsFloatBinaryPrecision = firebirdSupportInfo.supportsFloatBinaryPrecision();

        //FLOAT=6
        // Technically this describes REAL, but historically FLOAT == REAL in Firebird, and Jaybird has only used FLOAT
        int floatPrecision = supportsFloatBinaryPrecision ? 24 : 7;
        // We're intentionally not communicating the max FLOAT precision of 53 (which is a synonym of DOUBLE PRECISION)
        // nor are we reporting "precision" for column CREATE_PARAMS
        rows.add(row("FLOAT", Types.FLOAT, floatPrecision, null, null, null, false, DatabaseMetaData.typeSearchable,
                false, false, 0, SQL_FLOAT,
                supportsFloatBinaryPrecision ? 2 : 10));

        //DOUBLE=8
        int doublePrecision = supportsFloatBinaryPrecision ? 53 : 15;
        rows.add(row("DOUBLE PRECISION", Types.DOUBLE, doublePrecision, null, null, null, false,
                DatabaseMetaData.typeSearchable, false, false, 0, SQL_DOUBLE,
                supportsFloatBinaryPrecision ? 2 : 10));

        //VARCHAR=12
        rows.add(row("VARCHAR", Types.VARCHAR, 32765, "'", "'", "length", true, DatabaseMetaData.typeSearchable, true,
                true, 0, SQL_VARYING, 10));

        //BOOLEAN=16
        if (firebirdSupportInfo.supportsBoolean()) {
            rows.add(row("BOOLEAN", Types.BOOLEAN, BOOLEAN_BINARY_PRECISION, null, null, null, false,
                    DatabaseMetaData.typePredBasic, true, true, 0, SQL_BOOLEAN, 2));
        }

        //DATE=91
        rows.add(row("DATE", Types.DATE, DATE_PRECISION, "date'", "'", null, false, DatabaseMetaData.typeSearchable,
                true, true, 0, SQL_TYPE_DATE, 10));

        //TIME=92
        rows.add(row("TIME", Types.TIME, TIME_PRECISION, "time'", "'", null, false, DatabaseMetaData.typeSearchable,
                true, true, 0, SQL_TYPE_TIME, 10));

        //TIMESTAMP=93
        rows.add(row("TIMESTAMP", Types.TIMESTAMP, TIMESTAMP_PRECISION, "timestamp'", "'", null, false,
                DatabaseMetaData.typeSearchable, true, true, 0, SQL_TIMESTAMP, 10));

        //OTHER=1111
        rows.add(row("ARRAY", Types.OTHER, 0, null, null, null, true, DatabaseMetaData.typePredNone, true, true, 0,
                SQL_ARRAY, 10));

        //BLOB=2004
        // Should we split this into all negative blob types currently known in the DB?
        // Blob is potentially searchable with like, etc, acting as if it isn't.
        rows.add(row("BLOB SUB_TYPE <0", Types.BLOB, 0, null, null, null, true, DatabaseMetaData.typePredNone, true,
                true, 0, SQL_BLOB, 10));

        if (firebirdSupportInfo.supportsTimeZones()) {
            //TIME_WITH_TIMEZONE=2013
            rows.add(row("TIME WITH TIME ZONE", Types.TIME_WITH_TIMEZONE, TIME_WITH_TIMEZONE_PRECISION, "time'", "'",
                    null, false, DatabaseMetaData.typeSearchable, true, true, 0, SQL_TIME_TZ, 10));

            //TIMESTAMP_WITH_TIMEZONE=2014
            rows.add(row("TIMESTAMP WITH TIME ZONE", Types.TIMESTAMP_WITH_TIMEZONE, TIMESTAMP_WITH_TIMEZONE_PRECISION,
                    "timestamp'", "'", null, false, DatabaseMetaData.typeSearchable, true, true, 0, SQL_TIMESTAMP_TZ,
                    10));
        }

        return new FBResultSet(ROW_DESCRIPTOR, rows);
    }

    // NOTE: No parameters for NULLABLE (always true), AUTO_INCREMENT (always false), MINIMUM_SCALE (always 0), LOCAL_TYPE_NAME (always null) and SQL_DATETIME_SUB (unused)
    private static RowValue row(String typeName, int jdbcType, int precision, String literalPrefix,
            String literalSuffix, String createParams, boolean caseSensitive, int searchable, boolean unsigned,
            boolean fixedPrecScale, int maxScale, int sqlDataType, int numPrecRadix) {
        DatatypeCoder coder = DbMetadataMediator.datatypeCoder;
        return RowValue.of(ROW_DESCRIPTOR, getBytes(typeName), coder.encodeInt(jdbcType), coder.encodeInt(precision),
                getBytes(literalPrefix), getBytes(literalSuffix), getBytes(createParams),
                coder.encodeShort(DatabaseMetaData.typeNullable), getCharBoolean(caseSensitive),
                coder.encodeShort(searchable), getCharBoolean(unsigned), getCharBoolean(fixedPrecScale),
                getCharBoolean(false), null, coder.encodeShort(0), coder.encodeShort(maxScale),
                coder.encodeInt(sqlDataType), null, coder.encodeInt(numPrecRadix));
    }

    private static byte[] getBytes(String value) {
        return value != null ? DbMetadataMediator.datatypeCoder.encodeString(value) : null;
    }

    private static byte[] getCharBoolean(boolean booleanValue) {
        return booleanValue ? new byte[] { 'T' } : new byte[] { 'F' };
    }

    public static GetTypeInfo create(DbMetadataMediator mediator) {
        return new GetTypeInfo(mediator);
    }
}

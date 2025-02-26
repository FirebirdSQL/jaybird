// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.util.InternalApi;

/**
 * Constants for metadata as used in the Firebird metadata tables {@link java.sql.DatabaseMetaData} implementation.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
@SuppressWarnings("java:S115")
public final class FbMetadataConstants {

    public static final int SUBTYPE_NUMERIC = 1;
    public static final int SUBTYPE_DECIMAL = 2;
    
    public static final int FLOAT_BINARY_PRECISION = 24;
    public static final int FLOAT_DECIMAL_PRECISION = 7;
    public static final int DOUBLE_BINARY_PRECISION = 53;
    public static final int DOUBLE_DECIMAL_PRECISION = 15;
    public static final int BIGINT_PRECISION = 19;
    public static final int INTEGER_PRECISION = 10;
    public static final int SMALLINT_PRECISION = 5;
    public static final int BOOLEAN_BINARY_PRECISION = 1;
    public static final int NUMERIC_BIGINT_PRECISION = 18;
    public static final int NUMERIC_INTEGER_PRECISION = 9;
    public static final int NUMERIC_SMALLINT_PRECISION = 4;
    // NOTE: We also use this for INT128, which is actually 39, but for compatibility with JDBC type NUMERIC and tools
    // generating casts like NUMERIC(n) for what is actually an INT128, which would fail for 39, we use the same
    // (max) precision we apply for the real NUMERIC with precision > 18.
    public static final int NUMERIC_INT128_PRECISION = 38;
    public static final int DECFLOAT_16_PRECISION = 16;
    public static final int DECFLOAT_34_PRECISION = 34;
    // Precision: For datetime datatypes, this is the length in characters of the String representation (assuming the
    // maximum allowed precision of the fractional seconds component)
    public static final int DATE_PRECISION = 10; // yyyy-MM-dd
    public static final int TIME_PRECISION = 13; // HH:mm:ss.ffff
    public static final int TIMESTAMP_PRECISION = 24; // yyyy-MM-dd HH:mm:ss.ffff
    public static final int TIME_WITH_TIMEZONE_PRECISION = 19; // HH:mm:ss.ffff+XX:yy
    public static final int TIMESTAMP_WITH_TIMEZONE_PRECISION = 30; // yyyy-MM-dd HH:mm:ss.ffff+XX:yy

    public static final int RADIX_BINARY = 2;
    public static final int RADIX_DECIMAL = 10;

    public static final int OBJECT_NAME_LENGTH_BEFORE_V4_0 = 31;
    public static final int OBJECT_NAME_LENGTH_V4_0 = 63;
    public static final int OBJECT_NAME_LENGTH = OBJECT_NAME_LENGTH_V4_0;

    public static final int smallint_type = 7;
    public static final int integer_type = 8;
    public static final int quad_type = 9;
    public static final int float_type = 10;
    public static final int d_float_type = 11;
    public static final int date_type = 12;
    public static final int time_type = 13;
    public static final int char_type = 14;
    public static final int int64_type = 16;
    public static final int boolean_type = 23;
    public static final int dec16_type = 24;
    public static final int dec34_type = 25;
    public static final int int128_type = 26;
    public static final int double_type = 27;
    public static final int time_tz_type = 28;
    public static final int timestamp_tz_type = 29;
    public static final int ex_time_tz_type = 30;
    public static final int ex_timestamp_tz_type = 31;
    public static final int timestamp_type = 35;
    public static final int varchar_type = 37;
    public static final int cstring_type = 40;
    public static final int blob_type = 261;

    private FbMetadataConstants() {
        // no instances
    }
}

/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.util.InternalApi;

/**
 * Constants for metadata as used in the Firebird metadata tables {@link java.sql.DatabaseMetaData} implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
@InternalApi
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
    public static final int NUMERIC_DEC_FIXED_PRECISION = 34;
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

    // TODO Double check if these are the same as the blr constants or not.
    // TODO And if they are the same, check missing types (like text2 = 15, varying2 = 38)
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
    // Replaced by int128_type in 4.0.0.1604
    @Deprecated
    public static final int dec_fixed_type = 26;
    public static final int double_type = 27;
    public static final int time_tz_type = 28;
    public static final int timestamp_tz_type = 29;
    public static final int timestamp_type = 35;
    public static final int varchar_type = 37;
    public static final int cstring_type = 40;
    public static final int blob_type = 261;

    private FbMetadataConstants() {
        // no instances
    }
}

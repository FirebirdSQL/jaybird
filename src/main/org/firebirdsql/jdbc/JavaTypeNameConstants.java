// SPDX-FileCopyrightText: Copyright 2018-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;

/**
 * String constants of the Java type names used in metadata and field access.
 *
 * @author Mark Rotteveel
 */
@InternalApi
public final class JavaTypeNameConstants {

    public static final String OBJECT_CLASS_NAME = "java.lang.Object";
    public static final String BOOLEAN_CLASS_NAME = "java.lang.Boolean";
    public static final String BYTE_CLASS_NAME = "java.lang.Byte";
    public static final String SHORT_CLASS_NAME = "java.lang.Short";
    public static final String INTEGER_CLASS_NAME = "java.lang.Integer";
    public static final String LONG_CLASS_NAME = "java.lang.Long";
    public static final String BIG_INTEGER_CLASS_NAME = "java.math.BigInteger";
    public static final String FLOAT_CLASS_NAME = "java.lang.Float";
    public static final String DOUBLE_CLASS_NAME = "java.lang.Double";
    public static final String BIG_DECIMAL_CLASS_NAME = "java.math.BigDecimal";
    public static final String DECIMAL_CLASS_NAME = "org.firebirdsql.extern.decimal.Decimal";
    public static final String DECIMAL32_CLASS_NAME = "org.firebirdsql.extern.decimal.Decimal32";
    public static final String DECIMAL64_CLASS_NAME = "org.firebirdsql.extern.decimal.Decimal64";
    public static final String DECIMAL128_CLASS_NAME = "org.firebirdsql.extern.decimal.Decimal128";
    public static final String STRING_CLASS_NAME = "java.lang.String";
    public static final String CLOB_CLASS_NAME = "java.sql.Clob";
    public static final String NCLOB_CLASS_NAME = "java.sql.NClob";
    public static final String READER_CLASS_NAME = "java.io.Reader";
    public static final String BLOB_CLASS_NAME = "java.sql.Blob";
    public static final String FIREBIRD_BLOB_CLASS_NAME = "org.firebirdsql.jdbc.FirebirdBlob";
    public static final String FB_BLOB_CLASS_NAME = "org.firebirdsql.jdbc.FBBlob";
    public static final String FB_CLOB_CLASS_NAME = "org.firebirdsql.jdbc.FBClob";
    public static final String BYTE_ARRAY_CLASS_NAME = "[B"; // byte[]
    public static final String INPUT_STREAM_CLASS_NAME = "java.io.InputStream";
    public static final String TIMESTAMP_CLASS_NAME = "java.sql.Timestamp";
    public static final String UTIL_DATE_CLASS_NAME = "java.util.Date";
    public static final String CALENDAR_CLASS_NAME = "java.util.Calendar";
    public static final String LOCAL_DATE_TIME_CLASS_NAME = "java.time.LocalDateTime";
    public static final String OFFSET_DATE_TIME_CLASS_NAME = "java.time.OffsetDateTime";
    public static final String ZONED_DATE_TIME_CLASS_NAME = "java.time.ZonedDateTime";
    public static final String TIME_CLASS_NAME = "java.sql.Time";
    public static final String LOCAL_TIME_CLASS_NAME = "java.time.LocalTime";
    public static final String OFFSET_TIME_CLASS_NAME = "java.time.OffsetTime";
    public static final String SQL_DATE_CLASS_NAME = "java.sql.Date";
    public static final String LOCAL_DATE_CLASS_NAME = "java.time.LocalDate";
    public static final String ARRAY_CLASS_NAME = "java.sql.Array";
    public static final String ROW_ID_CLASS_NAME = "java.sql.RowId";
    public static final String FB_ROW_ID_CLASS_NAME = "org.firebirdsql.jdbc.FBRowId";

    private JavaTypeNameConstants() {
        // no instances
    }

}

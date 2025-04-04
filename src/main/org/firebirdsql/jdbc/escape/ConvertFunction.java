// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the {@code CONVERT} JDBC escape with some caveats.
 * <p>
 * Most important caveats:
 * </p>
 * <ul>
 *  <li>Contrary to the specification, we allow explicit length or precision and scale parameters</li>
 *  <li>{@code VARCHAR}, {@code NVARCHAR} (and value not a parameter ({@code ?})) without explicit length is converted
 *  using {@code TRIM(TRAILING FROM value)}, which means the result is {@code VARCHAR} except for blobs where this will
 *  result in a blob; national character set will be lost. If value is a parameter ({@code ?}), and no length is
 *  specified, then a length of 50 will be applied.</li>
 *  <li>{@code CHAR}, {@code NCHAR} without explicit length will be cast to {@code (N)CHAR(50)}</li>
 *  <li>{@code BINARY}, and {@code VARBINARY} without explicit length will be cast to
 *  {@code (VAR)CHAR(50) CHARACTER SET OCTETS}, with explicit length, {@code  CHARACTER SET OCTETS} is appended</li>
 *  <li>{@code LONGVARCHAR}, {@code LONGNVARCHAR}, {@code CLOB}, {@code NCLOB} will be cast to
 *  {@code BLOB SUB_TYPE TEXT}, national character set will be lost</li>
 *  <li>{@code LONGVARBINARY}, {@code BLOB} will be cast to {@code BLOB SUB_TYPE BINARY}</li>
 *  <li>{@code TINYINT} is mapped to {@code SMALLINT}</li>
 *  <li>{@code ROWID} is not supported as length of {@code DB_KEY} values depend on the context
 *  <!-- TODO: consider cast to CHAR(8) character set binary or maybe multiples of 8? --></li>
 *  <li>{@code (SQL_)DECIMAL} and {@code (SQL_)NUMERIC} without precision and scale are passed as is, in current
 *  Firebird versions, this means the value will be equivalent to {@code DECIMAL(9,0)} (which is equivalent to
 *  {@code INTEGER})</li>
 *  <li>Extension not defined in JDBC: {@code TIME_WITH_TIMEZONE/TIME_WITH_TIME_ZONE} and
 *  {@code TIMESTAMP_WITH_TIMEZONE/TIMESTAMP_WITH_TIME_ZONE} for Firebird 4 time zone types</li>
 *  <li>Unsupported/unknown datatypes (or invalid length or precision and scale) are passed as is to cast, resulting in
 *  an error from the Firebird engine if the resulting cast is invalid</li>
 * </ul>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class ConvertFunction implements SQLFunction {

    private static final Pattern TYPE_PATTERN =
            Pattern.compile("(?:SQL_)?(\\w+)(?:\\s*(\\([^)]*\\)))?", Pattern.CASE_INSENSITIVE);
    
    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        if (parameters.length != 2) {
            throw new FBSQLParseException("Expected 2 parameters for CONVERT, received " + parameters.length);
        }
        final String value = parameters[0];
        final String sqlType = parameters[1];
        final Matcher typeMatcher = TYPE_PATTERN.matcher(sqlType);
        if (!typeMatcher.matches()) {
            return renderCast(value, sqlType);
        }

        return renderCast(value, typeMatcher);
    }

    private String renderCast(final String value, final String sqlType) {
        return "CAST(" + value + " AS " + sqlType + ")";
    }

    private String renderCast(final String value, final Matcher typeMatcher) {
        String dataType = typeMatcher.group(1).toUpperCase(Locale.ROOT);
        String parameters = typeMatcher.group(2);
        switch (dataType) {
        case "TINYINT" -> dataType = "SMALLINT";
        case "DOUBLE" -> dataType = "DOUBLE PRECISION";
        case "CHAR", "NCHAR" -> {
            // Caveat: without parameters, size fixed at 50 (seems a reasonable trade off)
            if (parameters == null) {
                parameters = "(50)";
            }
        }
        case "VARCHAR", "NVARCHAR" -> {
            // Caveat: for blob use of TRIM results in a blob, not VARCHAR
            // Caveat: for NVARCHAR without parameters, this results in a VARCHAR
            // Caveat: if value is a parameter, size fixed at 50 (seems a reasonable trade off)
            if (parameters == null) {
                if ("?".equals(value)) {
                    parameters = "(50)";
                } else {
                    return "TRIM(TRAILING FROM " + value + ")";
                }
            }
        }
        case "BINARY" -> {
            // Caveat: without parameters, size fixed at 50 (seems a reasonable trade-off)
            // For maximum backwards compatibility, we use CHAR, not BINARY (introduced in Firebird 4.0)
            dataType = "CHAR";
            if (parameters == null) {
                parameters = "(50) CHARACTER SET OCTETS";
            } else {
                parameters += " CHARACTER SET OCTETS";
            }
        }
        case "VARBINARY" -> {
            // Caveat: without parameters, size fixed at 50 (seems a reasonable trade-off)
            dataType = "VARCHAR";
            if (parameters == null) {
                parameters = "(50) CHARACTER SET OCTETS";
            } else {
                parameters += " CHARACTER SET OCTETS";
            }
        }
        case "LONGVARCHAR", "LONGNVARCHAR", "CLOB", "NCLOB" -> {
            // Caveat: LONGNVARCHAR / NCLOB doesn't apply Firebird N(VAR)CHAR semantics of ISO-8859-1 charset
            dataType = "BLOB SUB_TYPE TEXT";
            parameters = null;
        }
        case "LONGVARBINARY", "BLOB" -> {
            dataType = "BLOB SUB_TYPE BINARY";
            parameters = null;
        }
        // WITH_TIMEZONE / WITH_TIME_ZONE support for convert is not defined in JDBC or ODBC
        case "TIME_WITH_TIMEZONE", "TIME_WITH_TIME_ZONE" -> {
            dataType = "TIME WITH TIME ZONE";
            parameters = null;
        }
        case "TIMESTAMP_WITH_TIMEZONE", "TIMESTAMP_WITH_TIME_ZONE" -> {
            dataType = "TIMESTAMP WITH TIME ZONE";
            parameters = null;
        }
        default -> {
            // do nothing
        }
        }
        return renderCast(value, parameters == null ? dataType : dataType + parameters);
    }

}

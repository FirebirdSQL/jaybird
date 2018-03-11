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
package org.firebirdsql.jdbc.escape;

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
 *  TODO: consider cast to CHAR(8) character set binary or maybe multiples of 8?</li>
 *  <li>{@code `(SQL_)DECIMAL`} and {@code `(SQL_)NUMERIC`} without precision and scale are passed as is, in current
 *  Firebird versions, this means the value will be equivalent to {@code DECIMAL(9,0)} (which is equivalent to
 *  {@code INTEGER})</li>
 *  <li>Unsupported/unknown datatypes (or invalid length or precision and scale) are passed as is to cast, resulting in
 *  an error from the Firebird engine if the resulting cast is invalid</li>
 * </ul>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
        String dataType = typeMatcher.group(1).toUpperCase();
        String parameters = typeMatcher.group(2);
        switch (dataType) {
        case "TINYINT":
            dataType = "SMALLINT";
            break;
        case "DOUBLE":
            dataType = "DOUBLE PRECISION";
            break;
        case "CHAR":
        case "NCHAR":
            // Caveat: without parameters, size fixed at 50 (seems a reasonable trade off)
            if (parameters == null) {
                parameters = "(50)";
            }
            break;
        case "VARCHAR":
        case "NVARCHAR":
            // Caveat: for blob use of TRIM results in a blob, not VARCHAR
            // Caveat: for NVARCHAR without parameters, this results in a VARCHAR
            // Caveat: if value is a parameter, size fixed at 50 (seems a reasonable trade off)
            if (parameters == null) {
                if (!"?".equals(value)) {
                    return "TRIM(TRAILING FROM " + value + ")";
                } else {
                    parameters = "(50)";
                }
            }
            break;
        case "BINARY":
            // Caveat: without parameters, size fixed at 50 (seems a reasonable trade off)
            if (parameters == null) {
                dataType = "CHAR";
                parameters = "(50) CHARACTER SET OCTETS";
            } else {
                dataType = "CHAR";
                parameters += " CHARACTER SET OCTETS";
            }
            break;
        case "VARBINARY":
            // Caveat: without parameters, size fixed at 50 (seems a reasonable trade off)
            if (parameters == null) {
                dataType = "VARCHAR";
                parameters = "(50) CHARACTER SET OCTETS";
            } else {
                dataType = "VARCHAR";
                parameters += " CHARACTER SET OCTETS";
            }
            break;
        case "LONGVARCHAR":
        case "LONGNVARCHAR":
        case "CLOB":
        case "NCLOB":
            // Caveat: LONGNVARCHAR / NCLOB doesn't apply Firebird N(VAR)CHAR semantics of ISO-8859-1 charset
            dataType = "BLOB SUB_TYPE TEXT";
            parameters = null;
            break;
        case "LONGVARBINARY":
        case "BLOB":
            dataType = "BLOB SUB_TYPE BINARY";
            parameters = null;
            break;
        }
        return renderCast(value, parameters == null ? dataType : dataType + parameters);
    }

}

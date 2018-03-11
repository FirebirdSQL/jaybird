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

/**
 * Implements the {@code TIMESTAMPDIFF} JDBC escape.
 * <p>
 * Value {@code SQL_TSI_FRAC_SECOND} unit is nanoseconds and will be simulated by using {@code MILLISECOND} and
 * the result multiplied by {@code 1.0e6} to convert the value to nanoseconds.
 * </p>
 * <p>
 * Value {@code SQL_TSI_QUARTER} will be simulated by using {@code MONTH} and the result divided by {@code 3}.
 * </p>
 * <p>
 * Contrary to specified in the JDBC specification, the resulting value will be {@code BIGINT}, not {@code INTEGER}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
final class TimestampDiffFunction implements SQLFunction {

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        if (parameters.length != 3) {
            throw new FBSQLParseException("Expected 3 parameters for TIMESTAMPDIFF, received " + parameters.length);
        }
        String jdbcIntervalName = parameters[0].trim().toUpperCase();
        if ("SQL_TSI_QUARTER".equals(jdbcIntervalName)) {
            return "(DATEDIFF(MONTH," + parameters[1] + "," + parameters[2] + ")/3)";
        } else if ("SQL_TSI_FRAC_SECOND".equals(jdbcIntervalName)) {
            // See ODBC spec: "where fractional seconds are expressed in billionths of a second."
            return "CAST(DATEDIFF(MILLISECOND," + parameters[1] + "," + parameters[2] + ")*1.0e6 AS BIGINT)";
        }
        String fbIntervalName = IntervalMapping.getFirebirdInterval(jdbcIntervalName);
        return "DATEDIFF(" + fbIntervalName + "," + parameters[1] + "," + parameters[2] + ")";
    }
}

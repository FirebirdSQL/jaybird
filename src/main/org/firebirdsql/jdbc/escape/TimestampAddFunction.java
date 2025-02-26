// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import java.util.Locale;

/**
 * Implements the {@code TIMESTAMPADD} JDBC escape.
 * <p>
 * Value {@code SQL_TSI_FRAC_SECOND} unit is nanoseconds and will be simulated by using {@code MILLISECOND} and
 * the count multiplied by {@code 1.0e-6} to convert the value to milliseconds.
 * </p>
 * <p>
 * Value {@code SQL_TSI_QUARTER} will be simulated by using {@code MONTH} and the count multiplied by {@code 3}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class TimestampAddFunction implements SQLFunction {

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        if (parameters.length != 3) {
            throw new FBSQLParseException("Expected 3 parameters for TIMESTAMPADD, received " + parameters.length);
        }
        String jdbcIntervalName = parameters[0].trim().toUpperCase(Locale.ROOT);
        if ("SQL_TSI_QUARTER".equals(jdbcIntervalName)) {
            return "DATEADD(MONTH,3*(" + parameters[1] + ")," + parameters[2] + ")";
        } else if ("SQL_TSI_FRAC_SECOND".equals(jdbcIntervalName)) {
            // See ODBC spec: "where fractional seconds are expressed in billionths of a second."
            return "DATEADD(MILLISECOND,1.0e-6*(" + parameters[1] + ")," + parameters[2] + ")";
        }
        String fbIntervalName = IntervalMapping.getFirebirdInterval(jdbcIntervalName);
        return "DATEADD(" + fbIntervalName + "," + parameters[1] + "," + parameters[2] + ")";
    }

}

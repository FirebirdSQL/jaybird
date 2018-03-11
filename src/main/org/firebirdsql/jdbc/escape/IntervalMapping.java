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
 * Helper class to map JDBC interval names to Firebird interval names.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
final class IntervalMapping {

    private IntervalMapping() {
        // no instances
    }

    /**
     * Maps the JDBC interval name to a Firebird interval name, unknown values are returned as is.
     * <p>
     * This function will map {@code SQL_TSI_QUARTER} to {@code QUARTER}, but current Firebird versions do not
     * support {@code QUARTER}. Users of this function should explicitly handle this.
     * </p>
     * <p>
     * Value {@code SQL_TSI_FRAC_SECOND} is not mapped and will be returned as is, users of this function should
     * explicitly handle this. The unit of {@code SQL_TSI_FRAC_SECOND} is one billionth of a second (or in other words:
     * nanoseconds). See <a href="https://docs.microsoft.com/en-us/sql/odbc/reference/appendixes/time-date-and-interval-functions">
     * ODBC: Time, Date, and Interval Functions</a>.
     * </p>
     *
     * @param jdbcIntervalName
     *         JDBC interval name, value must already be trimmed and upper-cased; {@code null} not supported.
     * @return Firebird interval name, or original value if unsupported or unknown
     */
    static String getFirebirdInterval(String jdbcIntervalName) {
        switch (jdbcIntervalName) {
        case "SQL_TSI_SECOND":
            return "SECOND";
        case "SQL_TSI_MINUTE":
            return "MINUTE";
        case "SQL_TSI_HOUR":
            return "HOUR";
        case "SQL_TSI_DAY":
            return "DAY";
        case "SQL_TSI_WEEK":
            return "WEEK";
        case "SQL_TSI_MONTH":
            return "MONTH";
        case "SQL_TSI_QUARTER":
            // NOTE QUARTER not supported by Firebird
            return "QUARTER";
        case "SQL_TSI_YEAR":
            return "YEAR";
        case "SQL_TSI_FRAC_SECOND":
            // explicitly not supported, passing as-is
        default:
            return jdbcIntervalName;
        }
    }
}

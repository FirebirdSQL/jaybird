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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for support of the scalar time and date function escapes as defined in
 * appendix D.3 of the JDBC 4.1 specification.
 *
 * @author Mark Rotteveel
 */
class ScalarTimeDateFunctionsTest {
    
    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection con;
    private static Statement stmt;

    @BeforeAll
    static void setupAll() throws Exception {
        // We create a connection and statement for all tests executed for performance reasons
        Properties props = getDefaultPropertiesForConnection();
        if (getDefaultSupportInfo().supportsTimeZones()) {
            // Test uses java.sql.Time/java.sql.Timestamp, avoid complications with CURRENT_TIME(STAMP)
            props.setProperty("dataTypeBind", "time with time zone to legacy;timestamp with time zone to legacy");
        }
        con = DriverManager.getConnection(getUrl(), props);
        stmt = con.createStatement();
    }

    @AfterAll
    static void tearDownAll() {
        try {
            closeQuietly(stmt, con);
        } finally {
            stmt = null;
            con = null;
        }
    }

    static Stream<Arguments> timeDateFunctionTestcases() {
        return Stream.of(
//@formatter:off
        /* 0*/ testcase("CURRENT_DATE", new CurrentDateValidator()),
        /* 1*/ testcase("CURRENT_DATE()", new CurrentDateValidator()),
        /* 2*/ testcase("CURRENT_TIME", new CurrentTimeValidator()),
        /* 3*/ testcase("CURRENT_TIME()", new CurrentTimeValidator()),
        /* 4*/ testcase("CURRENT_TIMESTAMP", new CurrentTimestampValidator()),
        /* 5*/ testcase("CURRENT_TIMESTAMP()", new CurrentTimestampValidator()),
        /* 6*/ testcase("CURDATE()", new CurrentDateValidator()),
        /* 7*/ testcase("CURTIME()", new CurrentTimeValidator()),
        // See test cases 55-60 for other week days
        /* 8*/ testcase("DAYNAME(DATE '2020-01-12')", new SimpleValidator("Sunday")),
        /* 9*/ testcase("DAYOFMONTH(DATE '2012-12-22')", new SimpleValidator(22)),
        /*10*/ testcase("DAYOFWEEK(DATE '2012-12-22')", new SimpleValidator(7)),
        /*11*/ testcase("DAYOFWEEK(DATE '2012-12-23')", new SimpleValidator(1)),
        /*12*/ testcase("DAYOFYEAR(DATE '2012-12-22')", new SimpleValidator(357)),
        /*13*/ testcase("EXTRACT(YEAR FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(2012)),
        /*14*/ testcase("EXTRACT(MONTH FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(12)),
        /*15*/ testcase("EXTRACT(DAY FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(22)),
        /*16*/ testcase("EXTRACT(HOUR FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(19)),
        /*17*/ testcase("EXTRACT(MINUTE FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(13)),
        /*18*/ testcase("EXTRACT(SECOND FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(5)),
        /*19*/ testcase("HOUR(TIME '19:13:05')", new SimpleValidator(19)),
        /*20*/ testcase("MINUTE(TIME '19:13:05')", new SimpleValidator(13)),
        /*21*/ testcase("MONTH(DATE '2012-12-22')", new SimpleValidator(12)),
        // See test cases 61-71 for other month names
        /*22*/ testcase("MONTHNAME(DATE '2020-01-12')", new SimpleValidator("January")),
        /*23*/ testcase("NOW()", new CurrentTimestampValidator()),
        /*24*/ testcase("QUARTER(DATE '2012-01-01')", new SimpleValidator(1)),
        /*25*/ testcase("QUARTER(DATE '2012-03-31')", new SimpleValidator(1)),
        /*26*/ testcase("QUARTER(DATE '2012-04-01')", new SimpleValidator(2)),
        /*27*/ testcase("QUARTER(DATE '2012-06-30')", new SimpleValidator(2)),
        /*28*/ testcase("QUARTER(DATE '2012-07-01')", new SimpleValidator(3)),
        /*29*/ testcase("QUARTER(DATE '2012-09-30')", new SimpleValidator(3)),
        /*20*/ testcase("QUARTER(DATE '2012-10-01')", new SimpleValidator(4)),
        /*31*/ testcase("QUARTER(DATE '2012-12-31')", new SimpleValidator(4)),
        /*32*/ testcase("SECOND(TIME '19:13:05')", new SimpleValidator(5)),
        /*33*/ testcase("TIMESTAMPADD(SQL_TSI_FRAC_SECOND, 5000000, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-23 12:00:00.005"))),
        /*34*/ testcase("TIMESTAMPADD(SQL_TSI_SECOND, 13, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-23 12:00:13"))),
        /*35*/ testcase("TIMESTAMPADD(SQL_TSI_MINUTE, 4, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-23 12:04:00"))),
        /*36*/ testcase("TIMESTAMPADD(SQL_TSI_HOUR, 1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-23 13:00:00"))),
        /*37*/ testcase("TIMESTAMPADD(SQL_TSI_DAY, 1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-24 12:00:00"))),
        /*38*/ testcase("TIMESTAMPADD(SQL_TSI_WEEK, 1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-30 12:00:00"))),
        /*39*/ testcase("TIMESTAMPADD(SQL_TSI_MONTH, 1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-12-23 12:00:00"))),
        /*40*/ testcase("TIMESTAMPADD(SQL_TSI_QUARTER, 1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2018-02-23 12:00:00"))),
        /*41*/ testcase("TIMESTAMPADD(SQL_TSI_YEAR, 1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2018-11-23 12:00:00"))),
        // Check if expressions work correctly for FRAC_SECOND (check correct evaluation order)
        /*42*/ testcase("TIMESTAMPADD(SQL_TSI_FRAC_SECOND, 2500000+2500000, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2017-11-23 12:00:00.005"))),
        // Check if expressions work correctly for QUARTER (check correct evaluation order)
        /*43*/ testcase("TIMESTAMPADD(SQL_TSI_QUARTER, 1+1, TIMESTAMP'2017-11-23 12:00:00')",
                    new SimpleValidator(Timestamp.valueOf("2018-05-23 12:00:00"))),
        /*44*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_FRAC_SECOND,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2017-11-23 12:00:00.999')",
                    new SimpleValidator(999 * 1_000_000L)),
        /*45*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_SECOND,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2017-11-23 12:00:03')",
                    new SimpleValidator(3L)),
        /*46*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_MINUTE,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2017-11-23 13:01:03')",
                    new SimpleValidator(61L)),
        /*47*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_HOUR,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2017-11-23 13:59:59')",
                    new SimpleValidator(1L)),
        /*48*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_DAY,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2017-11-25 11:00:03')",
                    new SimpleValidator(2L)),
        /*49*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_WEEK,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2017-11-30 12:00:03')",
                    new SimpleValidator(1L)),
        /*50*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_MONTH,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2018-02-01 12:00:03')",
                    new SimpleValidator(3L)),
        /*51*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_QUARTER,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2018-02-01 12:00:03')",
                    new SimpleValidator(1L)),
        /*52*/ testcase(
                    "TIMESTAMPDIFF(SQL_TSI_YEAR,TIMESTAMP'2017-11-23 12:00:00',TIMESTAMP'2019-01-01 12:00:03')",
                    new SimpleValidator(2L)),
        /*53*/ testcase("WEEK(DATE '2012-12-22')", new SimpleValidator(51)),
        /*54*/ testcase("YEAR(DATE '2012-12-22')", new SimpleValidator(2012)),
        // See test case 8 for Sunday
        /*55*/ testcase("DAYNAME(DATE '2020-01-13')", new SimpleValidator("Monday")),
        /*56*/ testcase("DAYNAME(DATE '2020-01-14')", new SimpleValidator("Tuesday")),
        /*57*/ testcase("DAYNAME(DATE '2020-01-15')", new SimpleValidator("Wednesday")),
        /*58*/ testcase("DAYNAME(DATE '2020-01-16')", new SimpleValidator("Thursday")),
        /*59*/ testcase("DAYNAME(DATE '2020-01-17')", new SimpleValidator("Friday")),
        /*60*/ testcase("DAYNAME(DATE '2020-01-18')", new SimpleValidator("Saturday")),
        // See test case 22 for January
        /*61*/ testcase("MONTHNAME(DATE '2020-02-12')", new SimpleValidator("February")),
        /*62*/ testcase("MONTHNAME(DATE '2020-03-12')", new SimpleValidator("March")),
        /*63*/ testcase("MONTHNAME(DATE '2020-04-12')", new SimpleValidator("April")),
        /*64*/ testcase("MONTHNAME(DATE '2020-05-12')", new SimpleValidator("May")),
        /*65*/ testcase("MONTHNAME(DATE '2020-06-12')", new SimpleValidator("June")),
        /*66*/ testcase("MONTHNAME(DATE '2020-07-12')", new SimpleValidator("July")),
        /*67*/ testcase("MONTHNAME(DATE '2020-08-12')", new SimpleValidator("August")),
        /*68*/ testcase("MONTHNAME(DATE '2020-09-12')", new SimpleValidator("September")),
        /*69*/ testcase("MONTHNAME(DATE '2020-10-12')", new SimpleValidator("October")),
        /*70*/ testcase("MONTHNAME(DATE '2020-11-12')", new SimpleValidator("November")),
        /*71*/ testcase("MONTHNAME(DATE '2020-12-12')", new SimpleValidator("December"))
//@formatter:on
        );
    }

    /**
     * Testcase
     *
     * @param functionCall
     *         JDBC function call (without {fn .. })
     * @param validator
     *         {@link Validator} to test the result of using the function against the database
     * @param supported
     *         {@code true} function is supported, {@code false} when not supported
     */
    @ParameterizedTest(name = "{index}: value {0}")
    @MethodSource("timeDateFunctionTestcases")
    void testScalarFunction(String functionCall, Validator validator, Boolean supported) throws Exception {
        try (ResultSet rs = stmt.executeQuery(createQuery(functionCall))) {
            assertTrue(supported, () -> format("Expected function call %s to be unsupported", functionCall));
            assertTrue(rs.next(), "Expected at least one row");
            String validationResult = validator.validate(rs.getObject(1), functionCall);
            assertNull(validationResult, validationResult);
        } catch (SQLException ex) {
            if (supported) {
                throw ex;
            }
            // TODO validate exception?
        }
    }

    private String createQuery(String functionCall) {
        return format("SELECT {fn %s} FROM RDB$DATABASE", functionCall);
    }

    /**
     * Convenience method to create object array for testcase (ensures correct types).
     *
     * @param functionCall
     *         JDBC function call (with out {fn .. })
     * @param validator
     *         {@link Validator} to test the result of using the function against the database
     * @return Object[] testcase
     */
    private static Arguments testcase(final String functionCall, final Validator validator) {
        return Arguments.of(functionCall, validator, true);
    }

    @SuppressWarnings("unused")
    private static Arguments unsupported(final String functionCall) {
        return Arguments.of(functionCall,
                (Validator) (objectToValidate, fn) -> format("Escape function %s not supported", fn), false);
    }

    @FunctionalInterface
    private interface Validator {

        /**
         * Validates object
         *
         * @param objectToValidate
         *         Object to validate
         * @param functionCall
         *         Text of function call (for use in validation message)
         * @return null if validated, validation message if not validated
         */
        String validate(Object objectToValidate, String functionCall);
    }

    private static class CurrentDateValidator implements Validator {
        @Override
        public String validate(Object objectToValidate, String functionCall) {
            if (objectToValidate instanceof java.sql.Date) {
                java.sql.Date date = (java.sql.Date) objectToValidate;
                java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
                String dateAsString = date.toString();
                String currentDateAsString = currentDate.toString();
                if (dateAsString.equals(currentDateAsString)) {
                    return null;
                } else {
                    return format("Expected current date %s, received %s", currentDateAsString, dateAsString);
                }
            } else {
                return "Expected result of type java.sql.Date";
            }
        }
    }

    private static class CurrentTimeValidator implements Validator {
        @Override
        public String validate(Object objectToValidate, String functionCall) {
            if (objectToValidate instanceof java.sql.Time) {
                java.sql.Time time = (java.sql.Time) objectToValidate;
                java.sql.Time currentTime = new java.sql.Time(System.currentTimeMillis());
                String timeAsString = time.toString();
                String currentTimeAsString = currentTime.toString();
                if (timeAsString.equals(currentTimeAsString)) {
                    return null;
                } else {
                    return format("Expected current time %s, received %s", currentTimeAsString, timeAsString);
                }
            } else {
                return "Expected result of type java.sql.Time";
            }
        }
    }

    private static class CurrentTimestampValidator implements Validator {
        @Override
        public String validate(Object objectToValidate, String functionCall) {
            if (objectToValidate instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) objectToValidate;
                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String timestampAsString = df.format(timestamp);
                String currentTimestampAsString = df.format(currentTimestamp);
                if (timestampAsString.equals(currentTimestampAsString)) {
                    return null;
                }
                return format("Expected current timestamp %s, received %s", currentTimestampAsString,
                        timestampAsString);
            }
            return "Expected result of type java.sql.Timestamp";
        }
    }

    private static class SimpleValidator implements Validator {

        private final Object expectedValue;

        private SimpleValidator(Object expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public String validate(Object objectToValidate, String functionCall) {
            if (equals(expectedValue, objectToValidate)) {
                return null;
            }
            return format("Unexpected value %s, expected %s for function call %s", objectToValidate,
                    expectedValue, functionCall);
        }

        private boolean equals(Object o1, Object o2) {
            if (o1 == o2) {
                return true;
            }
            if (o1 == null) {
                return false;
            }
            if (o1 instanceof Number && o2 instanceof Number) {
                double d1 = ((Number) o1).doubleValue();
                double d2 = ((Number) o2).doubleValue();
                return Math.abs(d1 - d2) <= 0.00001;
            }
            return o1.equals(o2);
        }
    }
}

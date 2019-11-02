/*
 * $Id$
 *
 * Firebird Open Source JavaEE connector - JDBC driver
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

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;

import org.firebirdsql.management.FBManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for support of the scalar time and date function escapes as defined in
 * appendix D.3 of the JDBC 4.1 specification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(Parameterized.class)
public class TestScalarTimeDateFunctions {
    private static FBManager fbManager;
    private static Connection con;
    private static Statement stmt;

    private final String functionCall;
    private final Validator validator;
    private final boolean supported;

    @BeforeClass
    public static void setUp() throws Exception {
        fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        // We create a connection and statement for all tests executed for performance reasons
        con = getConnectionViaDriverManager();
        stmt = con.createStatement();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        closeQuietly(stmt);
        closeQuietly(con);
        defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }

    /**
     * Testcase
     *
     * @param functionCall
     *            JDBC function call (without {fn .. })
     * @param validator
     *            {@link Validator} to test the result of using the function against the
     *            database
     * @param supported
     *            <code>true</code> function is supported, <code>false</code> when not supported
     */
    public TestScalarTimeDateFunctions(String functionCall, Validator validator, Boolean supported) {
        this.functionCall = functionCall;
        this.validator = validator;
        this.supported = supported;
    }

    @Parameters(name="{index}: {0}")
    public static Collection<Object[]> timeDateFunctionTestcases() {
        return Arrays.asList(new Object[][] {
//@formatter:off
        /* 0*/  testcase("CURRENT_DATE", new CurrentDateValidator()),
        /* 1*/  testcase("CURRENT_DATE()", new CurrentDateValidator()),
        /* 2*/  testcase("CURRENT_TIME", new CurrentTimeValidator()),
        /* 3*/  testcase("CURRENT_TIME()", new CurrentTimeValidator()),
        /* 4*/  testcase("CURRENT_TIMESTAMP", new CurrentTimestampValidator()),
        /* 5*/  testcase("CURRENT_TIMESTAMP()", new CurrentTimestampValidator()),
        /* 6*/  testcase("CURDATE()", new CurrentDateValidator()),
        /* 7*/  testcase("CURTIME()", new CurrentTimeValidator()),
        /* 8*/  unsupported("DAYNAME(CURRENT_DATE)"),
        /* 9*/  testcase("DAYOFMONTH(DATE '2012-12-22')", new SimpleValidator(22)),
        /*10*/  testcase("DAYOFWEEK(DATE '2012-12-22')", new SimpleValidator(7)),
        /*11*/  testcase("DAYOFWEEK(DATE '2012-12-23')", new SimpleValidator(1)),
        /*12*/  testcase("DAYOFYEAR(DATE '2012-12-22')", new SimpleValidator(357)),
        /*13*/  testcase("EXTRACT(YEAR FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(2012)),
        /*14*/  testcase("EXTRACT(MONTH FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(12)),
        /*15*/  testcase("EXTRACT(DAY FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(22)),
        /*16*/  testcase("EXTRACT(HOUR FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(19)),
        /*17*/  testcase("EXTRACT(MINUTE FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(13)),
        /*18*/  testcase("EXTRACT(SECOND FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(5)),
        /*19*/  testcase("HOUR(TIME '19:13:05')", new SimpleValidator(19)),
        /*20*/  testcase("MINUTE(TIME '19:13:05')", new SimpleValidator(13)),
        /*21*/  testcase("MONTH(DATE '2012-12-22')", new SimpleValidator(12)),
        /*22*/  unsupported("MONTHNAME(CURRENT_DATE)"),
        /*23*/  testcase("NOW()", new CurrentTimestampValidator()),
        /*24*/  unsupported("QUARTER(DATE '2012-12-22')"),          // TODO Can be implemented as 1+(EXTRACT(MONTH FROM ...)-1)/3
        /*25*/  testcase("SECOND(TIME '19:13:05')", new SimpleValidator(5)),
                // TODO tests for TIMESTAMPADD and TIMESTAMPDIFF (maybe as separate test class)
        /*26*/  testcase("WEEK(DATE '2012-12-22')", new SimpleValidator(51)),
        /*27*/  testcase("YEAR(DATE '2012-12-22')", new SimpleValidator(2012)),
//@formatter:off
        });
    }

    @Before
    public void skipUnsupportedFirebird4Types() {
        assumeFalse("CURRENT_TIME(STAMP) related functions on Firebird return time zone types",
                getDefaultSupportInfo().isVersionEqualOrAbove(4, 0) &&
                        (validator instanceof CurrentTimeValidator || validator instanceof CurrentTimestampValidator));
    }
    
    @Test
    public void testScalarFunction() throws Exception {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(createQuery());
            if (!supported) {
                fail(String.format("Expected function call %s to be unsupported", functionCall));
            } else {
                assertTrue("Expected at least one row", rs.next());
                String validationResult = validator.validate(rs.getObject(1), functionCall);
                if (validationResult != null) {
                    fail(validationResult);
                }
            }
        } catch (SQLException ex) {
            if (supported) {
                throw ex;
            } else {
                // TODO validate exception?
                //fail("Validation of unsupported functions not yet implemented");
            }
        } finally {
            closeQuietly(rs);
        }
    }
    
    private String createQuery() {
        return String.format("SELECT {fn %s} FROM RDB$DATABASE", functionCall);
    }
    
    /**
     * Convenience method to create object array for testcase (ensures correct
     * types).
     * 
     * @param functionCall
     *            JDBC function call (with out {fn .. })
     * @param validator
     *            {@link Validator} to test the result of using the function against the
     *            database
     * @return Object[] testcase
     */
    private static Object[] testcase(final String functionCall, final Validator validator) {
        return new Object[] { functionCall, validator, true };
    }
    
    private static Object[] unsupported(final String functionCall) {
        return new Object[] { functionCall, new Validator() {
            @Override
            public String validate(Object objectToValidate, String functionCall) {
                return String.format("Escape function %s not supported", functionCall);
            }
        }, false };
    }
    
    private interface Validator {
        
        /**
         * Validates object
         * 
         * @param objectToValidate
         *            Object to validate
         * @param functionCall
         *            Text of function call (for use in validation message)
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
                    return String.format("Expected current date %s, received %s", currentDateAsString, dateAsString);
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
                    return String.format("Expected current time %s, received %s", currentTimeAsString, timeAsString);
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
                return String.format("Expected current timestamp %s, received %s", currentTimestampAsString,
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
            return String.format("Unexpected value %s, expected %s for function call %s", objectToValidate,
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

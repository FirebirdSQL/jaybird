/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.escape;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.firebirdsql.management.FBManager;
import org.junit.AfterClass;
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
        fbManager = FBTestProperties.defaultDatabaseSetUp();
        // We create a connection and statement for all tests executed for performance reasons
        con = FBTestProperties.getConnectionViaDriverManager();
        stmt = con.createStatement();
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        JdbcResourceHelper.closeQuietly(stmt);
        JdbcResourceHelper.closeQuietly(con);
        FBTestProperties.defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }
    
    /**
     * Testcase
     * 
     * @param functionCall
     *            JDBC function call (without {fn .. })
     * @param expectedResult
     *            Expected value as result of using the function against the
     *            database
     */
    public TestScalarTimeDateFunctions(String functionCall, Validator validator, Boolean supported) {
        this.functionCall = functionCall;
        this.validator = validator;
        this.supported = supported;
    }
    
    @Parameters
    public static Collection<Object[]> timeDateFunctionTestcases() {
        return Arrays.asList(new Object[][] {
                testcase("CURRENT_DATE", new CurrentDateValidator()),   // TODO Currently fails, support calling without ()
                testcase("CURRENT_DATE()", new CurrentDateValidator()), // TODO Currently fails, implement using CURRENT_DATE
                testcase("CURRENT_TIME", new CurrentTimeValidator()),   // TODO Currently fails, support calling without ()
                testcase("CURRENT_TIME()", new CurrentTimeValidator()), // TODO Currently fails, implement using CURRENT_TIME
                testcase("CURRENT_TIMESTAMP", new CurrentTimestampValidator()),     // TODO Currently fails, support calling without ()
                testcase("CURRENT_TIMESTAMP()", new CurrentTimestampValidator()),   // TODO Currently fails, implement using CURRENT_TIMESTAMP
                testcase("CURDATE()", new CurrentDateValidator()),
                testcase("CURTIME()", new CurrentTimeValidator()),
                unsupported("DAYNAME(CURRENT_DATE)"),
                testcase("DAYOFMONTH(DATE '2012-12-22')", new SimpleValidator(22)),
                testcase("DAYOFWEEK(DATE '2012-12-22')", new SimpleValidator(7)),   // TODO Implement (NOTE Sunday = 1, Saturday = 7)
                testcase("DAYOFWEEK(DATE '2012-12-23')", new SimpleValidator(1)),   // TODO Implement
                testcase("DAYOFYEAR(DATE '2012-12-22')", new SimpleValidator(357)), // TODO Implement (NOTE: 2012-01-01 should be 1, not 0)
                testcase("EXTRACT(YEAR FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(2012)),
                testcase("EXTRACT(MONTH FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(12)),
                testcase("EXTRACT(DAY FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(22)),
                testcase("EXTRACT(HOUR FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(19)),
                testcase("EXTRACT(MINUTE FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(13)),
                testcase("EXTRACT(SECOND FROM TIMESTAMP '2012-12-22 19:13:05')", new SimpleValidator(5)),
                testcase("HOUR(TIME '19:13:05')", new SimpleValidator(19)),
                testcase("MINUTE(TIME '19:13:05')", new SimpleValidator(13)),
                testcase("MONTH(DATE '2012-12-22')", new SimpleValidator(12)),
                unsupported("MONTHNAME(CURRENT_DATE)"),
                testcase("NOW()", new CurrentTimestampValidator()),   // TODO Currently fails, implement using CURRENT_TIMESTAMP
                unsupported("QUARTER(DATE '2012-12-22')"),  // TODO Can be implemented as 1+(EXTRACT(MONTH FROM ...)-1)/3
                testcase("SECOND(TIME '19:13:05')", new SimpleValidator(5)),
                // TODO tests for TIMESTAMPADD and TIMESTAMPDIFF (maybe as separate test class)
                testcase("WEEK(DATE '2012-12-22')", new SimpleValidator(52)),   // TODO Currently fails, implement using EXTRACT(WEEK FROM ..)
                testcase("YEAR(DATE '2012-12-22')", new SimpleValidator(2012)),
        });
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
                // TODO validate exception instead of throwing
                fail("Validation of unsupported functions not yet implemented");
            }
        } finally {
            JdbcResourceHelper.closeQuietly(rs);
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
     * @param expectedResult
     *            Expected value as result of using the function against the
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
                java.sql.Date date = (java.sql.Date)objectToValidate;
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
                java.sql.Time time = (java.sql.Time)objectToValidate;
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
            if (objectToValidate instanceof java.sql.Time) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp)objectToValidate;
                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String timestampAsString = df.format(timestamp);
                String currentTimestampAsString = df.format(currentTimestamp);
                if (timestampAsString.equals(currentTimestampAsString)) {
                    return null;
                }
                return String.format("Expected current timestamp %s, received %s", currentTimestampAsString, timestampAsString);
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
            return String.format("Unexpected value %s, expected %s for function call %s", objectToValidate, expectedValue, functionCall);
        }
        
        private boolean equals(Object o1, Object o2) {
            if (o1 == o2) {
                return true;
            }
            if (o1 == null && o2 != null) {
                return false;
            }
            if (o1 instanceof Number && o2 instanceof Number) {
                double d1 = ((Number)o1).doubleValue();
                double d2 = ((Number)o2).doubleValue();
                return Math.abs(d1 - d2) <= 0.00001;
            }
            return o1.equals(o2);
        }
    }
}

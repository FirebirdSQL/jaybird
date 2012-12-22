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
 * Tests for support of the scalar numeric function escapes as defined in appendix
 * D.1 of the JDBC 4.1 specification.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(Parameterized.class)
public class TestScalarNumericFunctions {
    
    private static FBManager fbManager;
    private static Connection con;
    private static Statement stmt;
    
    private final String functionCall;
    private final double expectedResult;
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
    public TestScalarNumericFunctions(String functionCall, Double expectedResult, Boolean supported) {
        this.functionCall = functionCall;
        this.expectedResult = expectedResult;
        this.supported = supported;
    }
    
    @Parameters
    public static Collection<Object[]> numericFunctionTestcases() {
        return Arrays.asList(new Object[][] {
//@formatter:off
                testcase("ABS(-513)", 513),
                testcase("ACOS(-1)", Math.PI),
                testcase("ASIN(1)", Math.PI/2),
                testcase("ATAN(-1)", -1*Math.PI/4),
                testcase("ATAN2(1,0)", Math.PI/2),
                testcase("CEILING(2.13)", 3),
                testcase("COS(3.141592654)", -1),
                testcase("COT(0.5)", 1.830488),
                unsupported("DEGREES(3.1414)"),  // TODO Add support using number * 180.0 / PI()?
                testcase("EXP(2)", Math.exp(2)),
                testcase("FLOOR(2.13)", 2),
                testcase("LOG(7.389056099)", 2), // TODO Currently fails, add support using LN(param)
                testcase("LOG10(31.1)", 1.492760),
                testcase("MOD(513, 132)", 117),
                testcase("PI()", Math.PI),
                testcase("POWER(3, 4)", 81),
                unsupported("RADIANS(90)"),      // TODO Add support using number * PI() / 180.0?
                unsupported("RAND(14232)"),      // Firebird only supports RAND() without parameter
                testcase("ROUND(1.123456, 3)", 1.123),
                testcase("SIGN(-3487)", -1),
                testcase("SIGN(0)", 0),
                testcase("SIGN(24737443)", 1),
                testcase("SIN(2.322)", Math.sin(2.322)),
                testcase("SQRT(25.25)", 5.024938),
                testcase("TAN(1.2333)", Math.tan(1.2333)),
                testcase("TRUNCATE(2345.12556, 2)", 2345.12), //TODO Currently fails, add support using TRUNC (verify if identical)
                unsupported("LN(7.389056099)"),   // Not listed in appendix D, TODO: change JDBC escape to not forward all functions
//@formatter:on
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
                assertEquals(failureMessage(), expectedResult, rs.getDouble(1), 0.00001);
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
    
    private String failureMessage() {
        return String.format("Unexpected result for function escape %s", functionCall);
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
    private static Object[] testcase(String functionCall, double expectedResult) {
        return new Object[] { functionCall, expectedResult, true };
    }
    
    private static Object[] unsupported(String functionCall) {
        return new Object[] { functionCall, Double.NaN, false };
    }
}

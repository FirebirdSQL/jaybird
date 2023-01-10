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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for support of the scalar numeric function escapes as defined in
 * appendix D.1 of the JDBC 4.1 specification.
 *
 * @author Mark Rotteveel
 */
class ScalarNumericFunctionsTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection con;
    private static Statement stmt;

    @BeforeAll
    static void setupAll() throws Exception {
        // We create a connection and statement for all tests executed for performance reasons
        con = getConnectionViaDriverManager();
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

    static Stream<Arguments> numericFunctionTestcases() {
        return Stream.of(
//@formatter:off
        /* 0*/  testcase("ABS(-513)", 513),
        /* 1*/  testcase("ACOS(-1)", Math.PI),
        /* 2*/  testcase("ASIN(1)", Math.PI/2),
        /* 3*/  testcase("ATAN(-1)", -1*Math.PI/4),
        /* 4*/  testcase("ATAN2(1,0)", Math.PI/2),
        /* 5*/  testcase("CEILING(2.13)", 3),
        /* 6*/  testcase("COS(3.141592654)", -1),
        /* 7*/  testcase("COT(0.5)", 1.830488),
        /* 8*/  testcase("DEGREES(PI())", 180),
        /* 9*/  testcase("EXP(2)", Math.exp(2)),
        /*10*/  testcase("FLOOR(2.13)", 2),
        /*11*/  testcase("LOG(7.389056099)", 2),
        /*12*/  testcase("LOG10(31.1)", 1.492760),
        /*13*/  testcase("MOD(513, 132)", 117),
        /*14*/  testcase("PI()", Math.PI),
        /*15*/  testcase("POWER(3, 4)", 81),
        /*16*/  testcase("RADIANS(90)", Math.PI / 2),
        /*17*/  unsupported("RAND(14232)"),      // Firebird only supports RAND() without parameter
        /*18*/  testcase("ROUND(1.123456, 3)", 1.123),
        /*19*/  testcase("SIGN(-3487)", -1),
        /*20*/  testcase("SIGN(0)", 0),
        /*21*/  testcase("SIGN(24737443)", 1),
        /*22*/  testcase("SIN(2.322)", Math.sin(2.322)),
        /*23*/  testcase("SQRT(25.25)", 5.024938),
        /*24*/  testcase("TAN(1.2333)", Math.tan(1.2333)),
        /*25*/  testcase("TRUNCATE(2345.12556, 2)", 2345.12)
//@formatter:on
        );
    }

    /**
     * Testcase
     *
     * @param functionCall
     *         JDBC function call (without {fn .. })
     * @param expectedResult
     *         Expected value as result of using the function against the database
     * @param supported
     *         {@code true} function is supported, {@code false} when not supported
     */
    @ParameterizedTest(name = "{index}: value {0} ({1})")
    @MethodSource("numericFunctionTestcases")
    void testScalarFunction(String functionCall, Double expectedResult, Boolean supported) throws Exception {
        try (ResultSet rs = stmt.executeQuery(createQuery(functionCall))) {
            assertTrue(supported, () -> format("Expected function call %s to be unsupported", functionCall));
            assertTrue(rs.next(), "Expected at least one row");
            assertEquals(expectedResult, rs.getDouble(1), 0.00001, failureMessage(functionCall));
        } catch (SQLException ex) {
            if (supported) {
                throw ex;
            }
            // TODO validate exception?
        }
    }

    private String failureMessage(String functionCall) {
        return format("Unexpected result for function escape %s", functionCall);
    }

    private String createQuery(String functionCall) {
        return format("SELECT {fn %s} FROM RDB$DATABASE", functionCall);
    }

    /**
     * Convenience method to create object array for testcase (ensures correct types).
     *
     * @param functionCall
     *         JDBC function call (with out {fn .. })
     * @param expectedResult
     *         Expected value as result of using the function against the database
     * @return testcase
     */
    private static Arguments testcase(String functionCall, double expectedResult) {
        return Arguments.of(functionCall, expectedResult, true);
    }

    @SuppressWarnings("SameParameterValue")
    private static Arguments unsupported(String functionCall) {
        return Arguments.of(functionCall, Double.NaN, false);
    }
}

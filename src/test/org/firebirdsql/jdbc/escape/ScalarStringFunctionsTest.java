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
 * Tests for support of the scalar string function escapes as defined in
 * appendix D.2 of the JDBC 4.1 specification.
 *
 * @author Mark Rotteveel
 */
class ScalarStringFunctionsTest {

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

    static Stream<Arguments> stringFunctionTestcases() {
        return Stream.of(
//@formatter:off
        /* 0*/  testcase("ASCII('A')", "65"),
        /* 1*/  testcase("CHAR(65)", "A"),
        /* 2*/  testcase("CHAR_LENGTH('123456')", "6"),
        /* 3*/  testcase("CHAR_LENGTH('123456', CHARACTERS)", "6"),
        /* 4*/  testcase("CHAR_LENGTH('123456', OCTETS)", "6"),
        /* 5*/  testcase("CHARACTER_LENGTH('123456')", "6"),
        /* 6*/  testcase("CHARACTER_LENGTH('123456', CHARACTERS)", "6"),
        /* 7*/  testcase("CHARACTER_LENGTH('123456', OCTETS)", "6"),
        /* 8*/  testcase("CONCAT('abc', 'def')", "abcdef"),
        /* 9*/  unsupported("DIFFERENCE('Robert', 'Rubin')"),
        /*10*/  testcase("INSERT('Goodbye', 2, 3, 'Hello')", "GHellobye"),
        /*11*/  testcase("LCASE('ABCDEF')", "abcdef"),
        /*12*/  testcase("LEFT('1234567890', 4)", "1234"),
        /*13*/  testcase("LENGTH(' 234   ')", "4"),
        /*14*/  testcase("LENGTH(' 234   ', CHARACTERS)", "4"),
        /*15*/  testcase("LENGTH(' 234   ', OCTETS)", "4"),
        /*16*/  testcase("LOCATE('def', 'abcdefabcdef')", "4"),
        /*17*/  testcase("LOCATE('def', 'abcdefabcdef', 5)", "10"),
        /*18*/  testcase("LTRIM('  abc  ')", "abc  "),
        /*19*/  testcase("POSITION('def' IN 'abcdefabcdef')", "4"),
        /*20*/  testcase("POSITION('def' IN 'abcdefabcdef', CHARACTERS)", "4"),
        /*21*/  unsupported("POSITION('def' IN 'abcdefabcdef', OCTETS)"),   // TODO Unclear what semantics are, so don't support, or as normal POSITION?
        /*22*/  testcase("REPEAT('a', 4)", "aaaa"),
        /*23*/  testcase("REPLACE('abcdefabcdef', 'def', 'xyz')", "abcxyzabcxyz"),
        /*24*/  testcase("RIGHT('0987654321', 6)", "654321"),
        /*25*/  testcase("RTRIM('  abc  ')", "  abc"),
        /*26*/  unsupported("SOUNDEX('Robert')"),
        /*27*/  testcase("SPACE(5)", "     "),
        /*28*/  testcase("SUBSTRING('1234567890', 3, 3)", "345"),
        /*29*/  testcase("SUBSTRING('1234567890', 3, 3, CHARACTERS)", "345"),
        /*30*/  testcase("SUBSTRING('1234567890', 3, 3, OCTETS)", "345"),   // TODO Verify behaviour with multi-byte characters
        /*31*/  testcase("UCASE('abcdef')", "ABCDEF")
//@formatter:on
        );
    }

    /**
     * Testcase
     *
     * @param functionCall
     *         JDBC function call (without {fn .. })
     * @param expectedResult
     *         Expected value as result of using the function against the
     *         database
     * @param supported
     *         {@code true} function is supported, {@code false} when not supported
     */
    @ParameterizedTest(name = "{index}: value {0} ({1})")
    @MethodSource("stringFunctionTestcases")
    void testScalarFunction(String functionCall, String expectedResult, Boolean supported) throws Exception {
        try (ResultSet rs = stmt.executeQuery(createQuery(functionCall))) {
            assertTrue(supported, () -> format("Expected function call %s to be unsupported", functionCall));
            assertTrue(rs.next(), "Expected at least one row");
            assertEquals(expectedResult, rs.getString(1), failureMessage(functionCall));
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
     * Convenience method to create object array for testcase (ensures correct
     * types).
     *
     * @param functionCall
     *         JDBC function call (with out {fn .. })
     * @param expectedResult
     *         Expected value as result of using the function against the
     *         database
     * @return Object[] testcase
     */
    private static Arguments testcase(String functionCall, String expectedResult) {
        return Arguments.of(functionCall, expectedResult, true);
    }

    private static Arguments unsupported(String functionCall) {
        return Arguments.of(functionCall, "", false);
    }
}

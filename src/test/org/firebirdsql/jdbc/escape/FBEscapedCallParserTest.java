/*
 SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2011-2025 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.jaybird.util.BasicVersion;
import org.firebirdsql.jdbc.FBProcedureCall;
import org.firebirdsql.jdbc.FBProcedureParam;
import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.Types;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FBEscapedCallParser}.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
class FBEscapedCallParserTest {

    //TODO Why are CALL_TEST_1 - CALL_TEST_4 unused? Sign of missing test coverage?
    private static final String CALL_TEST_1 = "{call my_proc(?, {d 01-12-11})}";
    private static final String CALL_TEST_2 = "{?= call my_proc ?, {d 01-12-11}}";
    private static final String CALL_TEST_3 = "EXECUTE PROCEDURE my_proc(?, {d 01-12-11})";
    private static final String CALL_TEST_4 = "EXECUTE PROCEDURE my_proc(?, '11-dec-2001');";
    private static final String CALL_TEST_5 =
            "{? = call my_proc(UPPER(?), '11-dec-2001',out 'test string, with comma')}";
    private static final String CALL_TEST_5_1 =
            "{?=call my_proc(UPPER(?), '11-dec-2001',out 'test string, with comma')}";
    private static final String CALL_TEST_6 =
            "{call my_proc(?, {fn ucase(?)}, '11-dec-2001',out 'test string, with comma')}";
    private static final String CALL_TEST_7 = "EXECUTE PROCEDURE my_proc(UPPER(?), '11-dec-2001')";
    private static final String CALL_TEST_8 = "EXECUTE PROCEDURE my_proc (UPPER(?), '11-dec-2001')";
    private static final String CALL_TEST_9 = "   \t  EXECUTE\nPROCEDURE  my_proc   (    UPPER(?), '11-dec-2001')  \t";

    private final FBEscapedCallParser parser = new FBEscapedCallParser(
            FBEscapedParser.of(FBTestProperties.maximumVersionSupported(), QuoteStrategy.DIALECT_3));
    private FBProcedureCall testProcedureCall;
    private final FBProcedureParam param1 = new FBProcedureParam(0, "?");
    private final FBProcedureParam param2 = new FBProcedureParam(1, "UPPER(?)");
    private final FBProcedureParam param3 = new FBProcedureParam(2, "'11-dec-2001'");
    private final FBProcedureParam param4 = new FBProcedureParam(3, "'test string, with comma'");

    @BeforeEach
    public void setUp() throws SQLException {
        testProcedureCall = new FBProcedureCall();
        // We're testing with the maximum supported version, and this procedure is not schema qualified in the statement
        testProcedureCall.setSchema(null);
        testProcedureCall.setPackage(FBProcedureCall.NO_PACKAGE);
        testProcedureCall.setName("MY_PROC");
        testProcedureCall.addOutputParam(param1);
        testProcedureCall.addInputParam(param2);
        testProcedureCall.addInputParam(param3);
        testProcedureCall.addOutputParam(param4);

        param1.setIndex(1);
        param2.setIndex(2);

        testProcedureCall.registerOutParam(1, Types.INTEGER);
        testProcedureCall.getInputParam(2).setValue("test value");
    }

    // TODO Split into multiple tests

    @Test
    void testProcessEscapedCall() throws Exception {
        FBProcedureCall procedureCall1 = parser.parseCall(CALL_TEST_5);
        procedureCall1.registerOutParam(1, Types.INTEGER);
        procedureCall1.getInputParam(2).setValue("test value");
        assertThrows(SQLException.class, () -> procedureCall1.registerOutParam(3, Types.CHAR),
                "Should not allow registering param 3 as output, since it does not exist");
        // Index 1 corresponds to the first mapped OUT parameter, so it returns 1
        assertEquals(1, procedureCall1.mapOutParamIndexToPosition(1), "Should return mapped parameter");
        // Index 2 does not correspond to a mapped OUT parameter, this returns the original value (2)
        assertEquals(2, procedureCall1.mapOutParamIndexToPosition(2), "Should return unmapped parameter");

        FBProcedureCall procedureCall2 = parser.parseCall(CALL_TEST_5_1);
        procedureCall2.registerOutParam(1, Types.INTEGER);
        procedureCall2.getInputParam(2).setValue("test value");
        assertThrows(SQLException.class, () -> procedureCall2.registerOutParam(3, Types.CHAR),
                "Should not allow registering param 3 as output, since it does not exist");
        assertEquals(testProcedureCall, procedureCall2,
                "Should correctly parse call: " + procedureCall2.getSQL(QuoteStrategy.DIALECT_3));

        FBProcedureCall procedureCall3 = parser.parseCall(CALL_TEST_6);
        procedureCall3.registerOutParam(1, Types.INTEGER);
        procedureCall3.getInputParam(2).setValue("test value");
        assertEquals(testProcedureCall, procedureCall3,
                "Should correctly parse call: " + procedureCall3.getSQL(QuoteStrategy.DIALECT_3));

        FBProcedureCall procedureCall4 = parser.parseCall(CALL_TEST_7);
        verifyParseSql(procedureCall4);

        FBProcedureCall procedureCall5 = parser.parseCall(CALL_TEST_8);
        verifyParseSql(procedureCall5);

        FBProcedureCall procedureCall6 = parser.parseCall(CALL_TEST_9);
        verifyParseSql(procedureCall6);
    }

    @Test
    void testOutParameterMapping() throws Exception {
        FBProcedureCall procedureCall = parser.parseCall(CALL_TEST_5);
        procedureCall.getInputParam(1).setValue("test value");
        procedureCall.registerOutParam(2, Types.INTEGER);
        // Index 1 does not correspond to a mapped OUT parameter, this returns the original value (1)
        assertEquals(1, procedureCall.mapOutParamIndexToPosition(1), "Should return unmapped parameter");
        // Index 2 corresponds to the first mapped OUT parameter, so it returns 1 as well
        assertEquals(1, procedureCall.mapOutParamIndexToPosition(2), "Should return mapped parameter");
        // Index 3 does not correspond to a mapped OUT parameter (though it does correspond to a literal marked as OUT),
        // this returns the original value (3)
        assertEquals(3, procedureCall.mapOutParamIndexToPosition(3), "Should return unmapped parameter");
    }

    @ParameterizedTest
    @MethodSource
    void parseCalls(BasicVersion version, String in, boolean ambiguousScope, String schema, String pkg, String name,
            String sql) throws Exception {
        var parser = new FBEscapedCallParser(FBEscapedParser.of(version, QuoteStrategy.DIALECT_3));
        FBProcedureCall procedureCall = parser.parseCall(in);
        assertEquals(ambiguousScope, procedureCall.isAmbiguousScope(), "ambiguousScope");
        assertEquals(schema, procedureCall.getSchema(), "schema");
        assertEquals(pkg, procedureCall.getPackage(), "package");
        assertEquals(name, procedureCall.getName(), "name");
        assertEquals(sql, procedureCall.getSQL(QuoteStrategy.DIALECT_3), "converted SQL");
    }

    static Stream<Arguments> parseCalls() {
        return Stream.of(
                parseCallTestCase(2, "{call SOME_PROCEDURE(?)}", false, "", "", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(2, "EXECUTE PROCEDURE SOME_PROCEDURE(?)", false, "", "", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(3, "{call SOME_PROCEDURE(?)}", false, "", "", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(3, "EXECUTE PROCEDURE SOME_PROCEDURE(?)", false, "", "", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(3, "{call SOME_PACKAGE.SOME_PROCEDURE(?)}", false, "", "SOME_PACKAGE",
                        "SOME_PROCEDURE", "EXECUTE PROCEDURE \"SOME_PACKAGE\".\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "{call SOME_PROCEDURE(?)}", false, null, "", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "EXECUTE PROCEDURE SOME_PROCEDURE(?)", false, null, "", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "{call SOME_PACKAGE.SOME_PROCEDURE(?)}", true, "SOME_PACKAGE", "",
                        "SOME_PROCEDURE", "EXECUTE PROCEDURE \"SOME_PACKAGE\".\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "{call SOME_PACKAGE%PACKAGE.SOME_PROCEDURE(?)}", false, null, "SOME_PACKAGE",
                        "SOME_PROCEDURE", "EXECUTE PROCEDURE \"SOME_PACKAGE\"%PACKAGE.\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "{call SOME_SCHEMA.SOME_PROCEDURE(?)}", true, "SOME_SCHEMA", "",
                        "SOME_PROCEDURE", "EXECUTE PROCEDURE \"SOME_SCHEMA\".\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "{call SOME_SCHEMA%SCHEMA.SOME_PROCEDURE(?)}", false, "SOME_SCHEMA", "",
                        "SOME_PROCEDURE", "EXECUTE PROCEDURE \"SOME_SCHEMA\"%SCHEMA.\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "{call SOME_SCHEMA.SOME_PACKAGE.SOME_PROCEDURE(?)}", false, "SOME_SCHEMA",
                        "SOME_PACKAGE", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_SCHEMA\".\"SOME_PACKAGE\".\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(6, "EXECUTE PROCEDURE SOME_SCHEMA.SOME_PACKAGE.SOME_PROCEDURE(?)", false,
                        "SOME_SCHEMA", "SOME_PACKAGE", "SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_SCHEMA\".\"SOME_PACKAGE\".\"SOME_PROCEDURE\"(?)"),

                // NOTE These are cases of invalid syntax for the version, and although consistently parsed, is
                // undefined behaviour and will fail one execute
                parseCallTestCase(2, "{call SOME_PACKAGE.SOME_PROCEDURE(?)}", false, "", "",
                        "SOME_PACKAGE.SOME_PROCEDURE", "EXECUTE PROCEDURE \"SOME_PACKAGE\".\"SOME_PROCEDURE\"(?)"),
                parseCallTestCase(3, "{call SOME_SCHEMA.SOME_PACKAGE.SOME_PROCEDURE(?)}", false, "", null,
                        "SOME_SCHEMA.SOME_PACKAGE.SOME_PROCEDURE",
                        "EXECUTE PROCEDURE \"SOME_SCHEMA\".\"SOME_PACKAGE\".\"SOME_PROCEDURE\"(?)")
        );
    }

    private static Arguments parseCallTestCase(int major, String in, boolean ambiguous, @Nullable String schema,
            @Nullable String pkg, String name, String sql) {
        return Arguments.of(BasicVersion.of(major),in, ambiguous, schema, pkg, name, sql);
    }

    private void verifyParseSql(FBProcedureCall procedureCall) throws SQLException {
        String testProcedureCallSQL = testProcedureCall.getSQL(QuoteStrategy.DIALECT_3);
        String procedureCallSQL = procedureCall.getSQL(QuoteStrategy.DIALECT_3);
        assertEquals(testProcedureCallSQL, procedureCallSQL,
                String.format("Should correctly parse call.\n[%s] \n[%s]", procedureCallSQL, testProcedureCallSQL));
    }

}

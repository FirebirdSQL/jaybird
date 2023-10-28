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

import org.firebirdsql.jdbc.FBProcedureCall;
import org.firebirdsql.jdbc.FBProcedureParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;

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

    private final FBEscapedCallParser parser = new FBEscapedCallParser();
    private FBProcedureCall testProcedureCall;
    private final FBProcedureParam param1 = new FBProcedureParam(0, "?");
    private final FBProcedureParam param2 = new FBProcedureParam(1, "UPPER(?)");
    private final FBProcedureParam param3 = new FBProcedureParam(2, "'11-dec-2001'");
    private final FBProcedureParam param4 = new FBProcedureParam(3, "'test string, with comma'");

    @BeforeEach
    public void setUp() throws SQLException {
        testProcedureCall = new FBProcedureCall();
        testProcedureCall.setName("my_proc");
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
        assertEquals(testProcedureCall, procedureCall2, "Should correctly parse call " + procedureCall2.getSQL(false));

        FBProcedureCall procedureCall3 = parser.parseCall(CALL_TEST_6);
        procedureCall3.registerOutParam(1, Types.INTEGER);
        procedureCall3.getInputParam(2).setValue("test value");
        assertEquals(testProcedureCall, procedureCall3, "Should correctly parse call. " + procedureCall3.getSQL(false));

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

    private void verifyParseSql(FBProcedureCall procedureCall) throws SQLException {
        assertEquals(testProcedureCall.getSQL(false), procedureCall.getSQL(false),
                String.format("Should correctly parse call.\n[%s] \n[%s]",
                        procedureCall.getSQL(false), testProcedureCall.getSQL(false)));
    }

}

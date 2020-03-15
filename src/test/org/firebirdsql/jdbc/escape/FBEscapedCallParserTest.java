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

import org.firebirdsql.jdbc.FBProcedureCall;
import org.firebirdsql.jdbc.FBProcedureParam;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link FBEscapedCallParser}.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBEscapedCallParserTest {

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

    private FBProcedureCall testProcedureCall;
    private FBProcedureParam param1 = new FBProcedureParam(0, "?");
    private FBProcedureParam param2 = new FBProcedureParam(1, "UPPER(?)");
    private FBProcedureParam param3 = new FBProcedureParam(2, "'11-dec-2001'");
    private FBProcedureParam param4 = new FBProcedureParam(3, "'test string, with comma'");

    @Before
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
    public void testProcessEscapedCall() throws Exception {
        FBEscapedCallParser parser = new FBEscapedCallParser();

        FBProcedureCall procedureCall = parser.parseCall(CALL_TEST_5);
        procedureCall.registerOutParam(1, Types.INTEGER);
        procedureCall.getInputParam(2).setValue("test value");
        try {
            procedureCall.registerOutParam(3, Types.CHAR);
            fail("Should not allow registering param 3 as output, since it does not exist.");
        } catch (SQLException ex) {
            // everything is ok
        }
        assertEquals(1, procedureCall.mapOutParamIndexToPosition(1, false));
        try {
            procedureCall.mapOutParamIndexToPosition(2, false);
            fail("Should not allow to obtain position when no compatibility mode is specified.");
        } catch (SQLException ex) {
            // everything is ok
        }

        procedureCall = parser.parseCall(CALL_TEST_5_1);
        procedureCall.registerOutParam(1, Types.INTEGER);
        procedureCall.getInputParam(2).setValue("test value");
        try {
            procedureCall.registerOutParam(3, Types.CHAR);
            fail("Should not allow registering param 3 as output, since it does not exist.");
        } catch (SQLException ex) {
            // everything is ok
        }
        assertEquals("Should correctly parse call. " + procedureCall.getSQL(false),
                testProcedureCall, procedureCall);

        procedureCall = parser.parseCall(CALL_TEST_6);
        procedureCall.registerOutParam(1, Types.INTEGER);
        procedureCall.getInputParam(2).setValue("test value");
        assertEquals("Should correctly parse call. " + procedureCall.getSQL(false),
                testProcedureCall, procedureCall);

        procedureCall = parser.parseCall(CALL_TEST_7);
        verifyParseSql(procedureCall);

        procedureCall = parser.parseCall(CALL_TEST_8);
        verifyParseSql(procedureCall);

        procedureCall = parser.parseCall(CALL_TEST_9);
        verifyParseSql(procedureCall);
    }

    private void verifyParseSql(FBProcedureCall procedureCall) throws SQLException {
        assertEquals("Should correctly parse call.\n[" + procedureCall.getSQL(false) + "] \n["
                        + testProcedureCall.getSQL(false) + "]",
                testProcedureCall.getSQL(false), procedureCall.getSQL(false));
    }

}

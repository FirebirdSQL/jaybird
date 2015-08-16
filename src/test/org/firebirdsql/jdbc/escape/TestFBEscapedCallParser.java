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

import java.sql.SQLException;
import java.sql.Types;

import org.firebirdsql.jdbc.FBProcedureCall;
import org.firebirdsql.jdbc.FBProcedureParam;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;

import junit.framework.TestCase;

/**
 * Describe class <code>TestFBEscapedCallParser</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBEscapedCallParser extends TestCase {
    public static final String CALL_TEST_1 =
        "{call my_proc(?, {d 01-12-11})}";

    public static final String CALL_TEST_2 =
        "{?= call my_proc ?, {d 01-12-11}}";

    public static final String CALL_TEST_3 =
        "EXECUTE PROCEDURE my_proc(?, {d 01-12-11})";

    public static final String CALL_TEST_4 =
        "EXECUTE PROCEDURE my_proc(?, '11-dec-2001');";
    
    public static final String CALL_TEST_5 =
        "{? = call my_proc(UPPER(?), '11-dec-2001',out 'test string, with comma')}";
    
    public static final String CALL_TEST_5_1 =
        "{?=call my_proc(UPPER(?), '11-dec-2001',out 'test string, with comma')}";
    
    public static final String CALL_TEST_6 =
        "{call my_proc(?, {fn ucase(?)}, '11-dec-2001',out 'test string, with comma')}";
    
    public static final String CALL_TEST_7 =
        "EXECUTE PROCEDURE my_proc(UPPER(?), '11-dec-2001')";
    
    public static final String CALL_TEST_8 =
        "EXECUTE PROCEDURE my_proc (UPPER(?), '11-dec-2001')";

    public static final String CALL_TEST_9 =
        "   \t  EXECUTE\nPROCEDURE  my_proc   (    UPPER(?), '11-dec-2001')";

    public TestFBEscapedCallParser(String testName) {
        super(testName);
    }
    
    protected FBProcedureCall testProcedureCall; 
    protected FBProcedureParam param1 = new FBProcedureParam(0, "?");
    protected FBProcedureParam param2 = new FBProcedureParam(1, "UPPER(?)");
    protected FBProcedureParam param3 = new FBProcedureParam(2, "'11-dec-2001'");
    protected FBProcedureParam param4 = new FBProcedureParam(3, "'test string, with comma'");
    
    protected void setUp() throws SQLException {
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
    
    protected void tearDown() {
    }
    
    public void testProcessEscapedCall() throws Exception {
        FBEscapedCallParser parser = new FBEscapedCallParser(EscapeParserMode.USE_BUILT_IN);
        
        FBProcedureCall procedureCall = parser.parseCall(CALL_TEST_5);
        procedureCall.registerOutParam(1, Types.INTEGER);
        procedureCall.getInputParam(2).setValue("test value");
        try {
        	procedureCall.registerOutParam(3, Types.CHAR);
            fail("Should not allow registering param 3 as output, " +
                    "since it does not exist.");
            
        } catch(SQLException ex) {
        	// everything is ok
        }
        assertEquals(1, procedureCall.mapOutParamIndexToPosition(1, false));
        try {
            procedureCall.mapOutParamIndexToPosition(2, false);
            fail("Should not allow to obtain position when no compatibility " +
                    "mode is specified.");
        } catch(SQLException ex) {
            // everything is ok
        }

        procedureCall = parser.parseCall(CALL_TEST_5_1);
        procedureCall.registerOutParam(1, Types.INTEGER);
        procedureCall.getInputParam(2).setValue("test value");
        try {
            procedureCall.registerOutParam(3, Types.CHAR);
            assertTrue("Should not allow registering param 3 as output, " +
                    "since it does not exist.", false);
            
        } catch(SQLException ex) {
            // everything is ok
        }
        assertTrue("Should correctly parse call. " + procedureCall.getSQL(false), 
                testProcedureCall.equals(procedureCall));
        
        procedureCall = parser.parseCall(CALL_TEST_6);
        procedureCall.registerOutParam(1, Types.INTEGER);
        procedureCall.getInputParam(2).setValue("test value");
        assertTrue("Should correctly parse call. " + procedureCall.getSQL(false), 
                testProcedureCall.equals(procedureCall));

        procedureCall = parser.parseCall(CALL_TEST_7);
        verifyParseSql(procedureCall);

        procedureCall = parser.parseCall(CALL_TEST_8);
        verifyParseSql(procedureCall);
        
        procedureCall = parser.parseCall(CALL_TEST_9);
        verifyParseSql(procedureCall);
    }

    private void verifyParseSql(FBProcedureCall procedureCall) throws SQLException {
        assertTrue("Should correctly parse call.\n[" + procedureCall.getSQL(false) + "] \n[" + testProcedureCall.getSQL(false) + "]", 
                testProcedureCall.getSQL(false).equals(procedureCall.getSQL(false)));
    }

}

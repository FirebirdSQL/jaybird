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

package org.firebirdsql.jdbc;

import java.sql.SQLException;
import java.sql.Types;

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
    
    public static final String CALL_TEST_6 =
        "{call my_proc(?, {fn ucase(?)}, '11-dec-2001',out 'test string, with comma')}";
    
    public static final String CALL_TEST_7 =
        "EXECUTE PROCEDURE my_proc(UPPER(?), '11-dec-2001')";
    

    public TestFBEscapedCallParser(String testName) {
        super(testName);
    }
    
    protected FBProcedureCall testProcedureCall; 
    
    protected void setUp() {
        testProcedureCall = new FBProcedureCall();
        testProcedureCall.setName("my_proc");
        testProcedureCall.addOutputParam(
                new FBProcedureParam(0, "?"));
        testProcedureCall.addInputParam(
                new FBProcedureParam(1, "UPPER(?)"));
        testProcedureCall.addInputParam(
                new FBProcedureParam(2, "'11-dec-2001'"));
        testProcedureCall.addOutputParam(
                new FBProcedureParam(3, "'test string, with comma'"));
    }
    
    protected void tearDown() {
    }
    
    public void testProcessEscapedCall() throws Exception {
        FBEscapedCallParser parser = new FBEscapedCallParser();
        
        FBProcedureCall procedureCall = parser.parseCall(CALL_TEST_5);
        procedureCall.registerOutParam(1, Types.INTEGER);
        try {
        	procedureCall.registerOutParam(3, Types.CHAR);
            assertTrue("Should not allow registering param 3 as output, " +
                    "since it does not exist.", false);
            
        } catch(SQLException ex) {
        	// everything is ok
        }
        
        assertTrue("Should correctly parse call. " + procedureCall.getSQL(), 
                testProcedureCall.equals(procedureCall));
        
        procedureCall = parser.parseCall(CALL_TEST_6);
        procedureCall.registerOutParam(1, Types.INTEGER);
        assertTrue("Should correctly parse call. " + procedureCall.getSQL(), 
                testProcedureCall.equals(procedureCall));
        
        procedureCall = parser.parseCall(CALL_TEST_7);
        assertTrue("Should correctly parse call. " + procedureCall.getSQL(), 
                testProcedureCall.getSQL().equals(procedureCall.getSQL()));
    }

}

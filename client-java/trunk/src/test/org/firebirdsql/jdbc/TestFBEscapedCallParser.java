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
        "my_proc in ?, in '11-dec-2001', out 'test'";

    public TestFBEscapedCallParser(String testName) {
        super(testName);
    }
    
    protected FBEscapedCallParser.FBProcedureCall testProcedureCall; 
    
    protected void setUp() {
        testProcedureCall = new FBEscapedCallParser.FBProcedureCall();
        testProcedureCall.setName("my_proc");
        testProcedureCall.addInputParam(
                new FBEscapedCallParser.FBProcedureCallParam(1, "?"));
        testProcedureCall.addInputParam(
                new FBEscapedCallParser.FBProcedureCallParam(2, "'11-dec-2001'"));
        testProcedureCall.addOutputParam(
                new FBEscapedCallParser.FBProcedureCallParam(3, "'test'"));
    }
    
    protected void tearDown() {
    }
    
    public void testProcessEscapedCall() throws Exception {
        FBEscapedCallParser parser = new FBEscapedCallParser();
        
        FBEscapedCallParser.FBProcedureCall procedureCall = 
            parser.parseCall(CALL_TEST_5);
        
        assertTrue("Should correctly parse call", 
                testProcedureCall.equals(procedureCall));
    }

}

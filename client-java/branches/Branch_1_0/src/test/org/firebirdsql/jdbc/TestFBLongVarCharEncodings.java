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

/**
 * This test case tests encodings in text blobs.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBLongVarCharEncodings extends TestFBEncodings {
    
    public static final String CREATE_TABLE = 
        "CREATE TABLE test_encodings (" + 
        "  id INTEGER, " +
        "  win1250_field BLOB SUB_TYPE 1 CHARACTER SET WIN1250, " +
        "  win1251_field BLOB SUB_TYPE 1 CHARACTER SET WIN1251, " +
        "  win1252_field BLOB SUB_TYPE 1 CHARACTER SET WIN1252, " +
        "  win1253_field BLOB SUB_TYPE 1 CHARACTER SET WIN1253, " +
        "  win1254_field BLOB SUB_TYPE 1 CHARACTER SET WIN1254, " +
        "  unicode_field BLOB SUB_TYPE 1 CHARACTER SET UNICODE_FSS, " +
        "  ascii_field BLOB SUB_TYPE 1 CHARACTER SET ASCII, " +
        "  none_field BLOB SUB_TYPE 1 CHARACTER SET NONE " +
        ")"
        ;

    public TestFBLongVarCharEncodings(String testName) {
        super(testName);
    }

    protected String getCreateTableStatement() {
        return CREATE_TABLE;
    }

    public void testGerman() throws Exception {
        super.testGerman();
    }

    public void testHungarian() throws Exception {
        super.testHungarian();
    }

    public void testUkrainian() throws Exception {
        super.testUkrainian();
    }
    
    
}
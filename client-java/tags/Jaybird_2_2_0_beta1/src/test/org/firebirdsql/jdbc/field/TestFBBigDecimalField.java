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
package org.firebirdsql.jdbc.field;

import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;


public class TestFBBigDecimalField extends BaseTestFBField {

    public TestFBBigDecimalField(String testName) {
        super(testName);
    }

    protected void setUp() throws SQLException{
        final XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = createXSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_INT64;
        
        field = FBField.createField(xsqlvars[0], createDataProvider(xsqlvars), null, false);
    }
    protected void tearDown() {
    }

    
    public void testDouble() throws SQLException {
    }

    public void testFloat() throws SQLException {
    }

    public void testBigDecimal() throws SQLException {
    }

    public void testObject() throws SQLException {
    }

    public void testString() throws SQLException {
    }

    public void testAsciiStream() throws SQLException {
    }

    public void testBinaryStream() throws SQLException {
    }

    public void testBoolean() throws SQLException {
    }

    public void testBytes() throws SQLException {
    }

    public void testDate() throws SQLException {
    }

    public void testTime() throws SQLException {
    }

    public void testTimestamp() throws SQLException {
    }

    public void testUnicodeStream() throws SQLException {
    }
    
}

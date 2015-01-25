/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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

import org.firebirdsql.common.FBTestBase;

public class TestFBConnection4_0 extends FBTestBase {

    public TestFBConnection4_0(String name) {
        super(name);
    }

    public void testClientInfo() throws Exception {
        AbstractConnection connection = (AbstractConnection)getConnectionViaDriverManager();
        try {
            
            connection.setClientInfo("TestProperty", "testValue");
            String checkValue = connection.getClientInfo("TestProperty");
            assertEquals("testValue", checkValue);
            
        } finally {
            connection.close();
        }
    }
}

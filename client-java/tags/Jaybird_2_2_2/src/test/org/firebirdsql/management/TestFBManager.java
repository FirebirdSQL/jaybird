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
package org.firebirdsql.management;


import org.firebirdsql.common.FBTestBase;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Describe class <code>TestFBManager</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBManager extends FBTestBase {

    public TestFBManager(String name) {
        super(name);
    }

    public static Test suite() {

        return new TestSuite(TestFBManager.class);
    }

    public void testStart() throws Exception {
        FBManager m = createFBManager();
        m.setServer(DB_SERVER_URL);
        m.setPort(DB_SERVER_PORT);
        m.start();
        m.stop();
    }

    public void testCreateDrop() throws Exception {
        FBManager m = createFBManager();
        m.setServer(DB_SERVER_URL);
        m.setPort(DB_SERVER_PORT);
        m.start();
        // check create
        // m.createDatabase(getdbpath(DB_NAME + ".fdb"), DB_USER, DB_PASSWORD);
        m.createDatabase(DB_NAME + ".fdb", DB_USER, DB_PASSWORD);
        
        // check create with set forceCreate
        m.setForceCreate(true);
        // m.createDatabase(getdbpath(DB_NAME + ".fdb"), DB_USER, DB_PASSWORD);
        m.createDatabase(DB_NAME + ".fdb", DB_USER, DB_PASSWORD);
        
        assertTrue("Must report that database exists", m.isDatabaseExists(
            DB_NAME + ".fdb", DB_USER, DB_PASSWORD));
        
        // check drop
        m.dropDatabase(DB_NAME + ".fdb", DB_USER, DB_PASSWORD);
        
        assertTrue("Must report that database exists", !m.isDatabaseExists(
            DB_NAME + ".fdb", DB_USER, DB_PASSWORD));
        
        m.stop();
    }



}

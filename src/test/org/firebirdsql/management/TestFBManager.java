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

    @Override
    public void setUp() {
        // We don't want the setup in FBTestBase
    }

    @Override
    public void tearDown() {
        // We don't want the teardown in FBTestBase
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
        // Adding .fdb suffix to prevent conflicts with other tests if drop fails
        final String databasePath = getDatabasePath() + ".fdb";
        // check create
        m.createDatabase(databasePath, DB_USER, DB_PASSWORD);
        
        // check create with set forceCreate
        m.setForceCreate(true);
        m.createDatabase(databasePath, DB_USER, DB_PASSWORD);
        
        assertTrue("Must report that database exists", m.isDatabaseExists(
                databasePath, DB_USER, DB_PASSWORD));
        
        // check drop
        m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
        
        assertTrue("Must report that database does not exist", !m.isDatabaseExists(
                databasePath, DB_USER, DB_PASSWORD));
        
        m.stop();
    }
}

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

import org.firebirdsql.jdbc.BaseFBTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Describe class <code>TestFBManager</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBManager extends BaseFBTest {

   // private static String dbPath = System.getProperty("test.db.dir");

   // public static String DBNAME = dbPath + "/fbmtest.gdb";

    public TestFBManager(String name) {
        super(name);
    }

    public static Test suite() {

        return new TestSuite(TestFBManager.class);
    }

    public void testStart() throws Exception {
        FBManager m = new FBManager();
        m.setServer("localhost");
        m.setPort(3050);
        m.start();
        m.stop();
    }

    public void testCreateDrop() throws Exception {
        FBManager m = new FBManager();
        m.setServer("localhost");
        m.setPort(3050);
        m.start();
        m.createDatabase(DB_NAME, DB_USER, DB_PASSWORD);
        m.dropDatabase(DB_NAME, DB_USER, DB_PASSWORD);
        m.stop();
    }



}

/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */
package org.firebirdsql.management;


//import org.firebirdsql.management.FBManager;
import java.io.*;
import java.util.Properties;
import java.sql.*;


import junit.framework.*;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 *This is a class that hands out connections.  Initial implementation uses DriverManager.getConnection,
 *future enhancements will use datasources/ managed stuff.
 */
public class TestFBManager extends TestCase {

    public static String DBNAME = "/usr/local/firebird/dev/client-java/db/fbmtest.gdb";

    public TestFBManager(String name) {
        super(name);
    }

    public static Test suite() {

        return new TestSuite(TestFBManager.class);
    }

    public void testStart() throws Exception {
        FBManager m = new FBManager();
        m.setURL("localhost");
        m.setPort(3050);
        m.start();
        m.stop();
    }

    public void testCreateDrop() throws Exception {
        FBManager m = new FBManager();
        m.setURL("localhost");
        m.setPort(3050);
        m.start();
        m.createDatabase(DBNAME);
        m.dropDatabase(DBNAME);
        m.stop();
    }



}

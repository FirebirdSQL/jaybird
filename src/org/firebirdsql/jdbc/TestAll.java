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

/*
 * CVS modification log:
 * $Log$
 * Revision 1.2  2001/07/18 05:12:46  d_jencks
 * Completely implemented DatabaseMetaData.getTables
 *
 * Revision 1.1  2001/07/13 18:16:15  d_jencks
 * Implementation of jdbc 1.0 Driver contributed by Roman Rokytskyy
 *
 * Revision 1.1  2001/07/09 09:09:51  rrokytskyy
 * Initial revision
 *
 */

package org.firebirdsql.jdbc;

import junit.framework.*;

/**
 * Test suite for the org.firebirdsql.jdbc package.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */
public class TestAll {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(org.firebirdsql.jdbc.TestFBDriver.suite());
        suite.addTest(org.firebirdsql.jdbc.TestFBUnmanagedConnection.suite());
        suite.addTest(org.firebirdsql.jdbc.TestFBCallableStatement.suite());
        return suite;
    }
}

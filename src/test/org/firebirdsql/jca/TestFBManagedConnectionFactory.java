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
package org.firebirdsql.jca;

import javax.resource.spi.ManagedConnection;

/**
 * Describe class <code>TestFBManagedConnectionFactory</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBManagedConnectionFactory extends TestXABase {

    public TestFBManagedConnectionFactory(String name) {
        super(name);
    }

    public void testCreateMcf() throws Exception {
        // TODO Test doesn't assert anything
        initMcf();
    }

    public void testCreateMc() throws Exception {
        // TODO Test doesn't assert anything
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        mc.destroy();
    }
}


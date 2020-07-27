/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jaybird.xca;

import org.junit.Test;

import javax.sql.DataSource;
import java.rmi.MarshalledObject;
import java.sql.Connection;

import static org.junit.Assert.assertNotNull;

/**
 * TestDataSourceSerialization.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class TestDataSourceSerialization extends TestXABase {

    @Test
    public void testDataSourceSerialization() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        assertNotNull("Could not get DataSource", ds);

        Connection c = ds.getConnection();
        assertNotNull("Could not get Connection", c);

        c.close();
        MarshalledObject<DataSource> mo = new MarshalledObject<DataSource>(ds);
        ds = mo.get();
        c = ds.getConnection();
        c.close();
    }
}

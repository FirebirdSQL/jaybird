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



import java.rmi.MarshalledObject;
import java.sql.Connection;
import javax.sql.DataSource;


/**
 * TestDataSourceSerialization.java
 *
 *
 * Created: Fri Sep  6 11:00:48 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class TestDataSourceSerialization extends TestXABase 
{
    public TestDataSourceSerialization(String name) 
    {
        super(name);
    }
    
    public void testDataSourceSerialization() throws Exception
    {
        if (log != null) log.info("testDataSourceSerialization");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        assertTrue("Could not get DataSource", ds != null);
        Connection c = ds.getConnection();
        assertTrue("Could not get Connection", c != null);
        c.close();
        MarshalledObject mo = new MarshalledObject(ds);
        ds = (DataSource)mo.get();
        c = ds.getConnection();
        c.close();
    }


}// TestDataSourceSerialization

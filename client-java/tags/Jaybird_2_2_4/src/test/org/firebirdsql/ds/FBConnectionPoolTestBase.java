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
package org.firebirdsql.ds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.PooledConnection;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.common.SimpleFBTestBase;
import org.firebirdsql.gds.impl.GDSType;

/**
 * Common testbase for tests using {@link FBConnectionPoolDataSource}
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class FBConnectionPoolTestBase extends FBTestBase {

    private List connections = new ArrayList();
    protected FBConnectionPoolDataSource ds;

    public FBConnectionPoolTestBase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    
        FBConnectionPoolDataSource newDs = new FBConnectionPoolDataSource();
        newDs.setType(SimpleFBTestBase.getProperty("test.gds_type", null));
        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")) {
            newDs.setServerName(DB_SERVER_URL);
            newDs.setPortNumber(DB_SERVER_PORT);
        }
        newDs.setDatabaseName(getDatabasePath());
        newDs.setUser(DB_USER);
        newDs.setPassword(DB_PASSWORD);
    
        ds = newDs;
    }

    public void tearDown() throws Exception {
        Iterator iter = connections.iterator();
        while (iter.hasNext()) {
            PooledConnection pc = (PooledConnection) iter.next();
            closeQuietly(pc);
        }
        super.tearDown();
    }

    protected PooledConnection getPooledConnection() throws SQLException {
        PooledConnection pc = ds.getPooledConnection();
        connections.add(pc);
        return pc;
    }

}
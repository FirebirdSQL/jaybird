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
package org.firebirdsql.ds;

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

import javax.sql.PooledConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;

/**
 * Common testbase for tests using {@link FBConnectionPoolDataSource}
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class FBConnectionPoolTestBase {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private List<PooledConnection> connections = new ArrayList<>();
    protected FBConnectionPoolDataSource ds;

    @Before
    public void setUp() throws Exception {
        FBConnectionPoolDataSource newDs = new FBConnectionPoolDataSource();
        newDs.setType(getProperty("test.gds_type", null));
        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")) {
            newDs.setServerName(DB_SERVER_URL);
            newDs.setPortNumber(DB_SERVER_PORT);
        }
        newDs.setDatabaseName(getDatabasePath());
        newDs.setUser(DB_USER);
        newDs.setPassword(DB_PASSWORD);
        newDs.setEncoding(DB_LC_CTYPE);
    
        ds = newDs;
    }

    @After
    public void tearDown() throws Exception {
        for (PooledConnection pc : connections) {
            closeQuietly(pc);
        }
        connections.clear();
    }

    protected PooledConnection getPooledConnection() throws SQLException {
        PooledConnection pc = ds.getPooledConnection();
        connections.add(pc);
        return pc;
    }
}
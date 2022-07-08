/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.PooledConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;

/**
 * Common testbase for tests using {@link FBConnectionPoolDataSource}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
abstract class FBConnectionPoolTestBase {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private final List<PooledConnection> connections = new ArrayList<>();
    protected FBConnectionPoolDataSource ds;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() {
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
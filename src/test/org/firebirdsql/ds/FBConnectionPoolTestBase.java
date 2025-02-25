// SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.PooledConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.configureDefaultDbProperties;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;

/**
 * Common testbase for tests using {@link FBConnectionPoolDataSource}.
 *
 * @author Mark Rotteveel
 */
abstract class FBConnectionPoolTestBase {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private final List<PooledConnection> connections = new ArrayList<>();
    protected final FBConnectionPoolDataSource ds = configureDefaultDbProperties(new FBConnectionPoolDataSource());

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
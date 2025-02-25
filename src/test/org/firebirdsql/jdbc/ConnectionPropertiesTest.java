// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for connection properties (does not cover all properties for now)
 *
 * @author Mark Rotteveel
 */
class ConnectionPropertiesTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @Test
    void testProperty_defaultIsolation_onDataSource() throws Exception {
        FBSimpleDataSource ds = createDataSource();

        ds.setDefaultIsolation("TRANSACTION_SERIALIZABLE");

        try (Connection connection = ds.getConnection()) {
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation(),
                    "Unexpected isolation level");
        }
    }

    @Test
    void testProperty_defaultIsolation_onDriverManager() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("defaultIsolation", "TRANSACTION_SERIALIZABLE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation(),
                    "Unexpected isolation level");
        }
    }

    @Test
    void testProperty_isolation_onDriverManager() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // alias for defaultIsolation
        props.setProperty("isolation", "TRANSACTION_SERIALIZABLE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation(),
                    "Unexpected isolation level");
        }
    }

    private FBSimpleDataSource createDataSource() {
        return configureDefaultDbProperties(new FBSimpleDataSource());
    }
}

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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests for connection properties (does not cover all properties for now)
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class ConnectionPropertiesTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    @Test
    public void testProperty_defaultIsolation_onDataSource() throws Exception {
        FBSimpleDataSource ds = createDataSource();

        ds.setDefaultIsolation("TRANSACTION_SERIALIZABLE");

        try (Connection connection = ds.getConnection()) {
            assertEquals("Unexpected isolation level",
                    Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());
        }
    }

    @Test
    public void testProperty_defaultIsolation_onDriverManager() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("defaultIsolation", "TRANSACTION_SERIALIZABLE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertEquals("Unexpected isolation level",
                    Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());
        }
    }

    @Test
    public void testProperty_isolation_onDriverManager() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // alias for defaultIsolation
        props.setProperty("isolation", "TRANSACTION_SERIALIZABLE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertEquals("Unexpected isolation level",
                    Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());
        }
    }

    private FBSimpleDataSource createDataSource() {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabase(getUrl().replace("jdbc:firebirdsql:", ""));
        ds.setType(GDS_TYPE);
        ds.setUserName(DB_USER);
        ds.setPassword(DB_PASSWORD);
        return ds;
    }
}

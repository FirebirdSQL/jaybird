// SPDX-FileCopyrightText: Copyright 2002 David Jencks
// SPDX-FileCopyrightText: Copyright 2012-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.DataSource;
import java.rmi.MarshalledObject;
import java.sql.Connection;

import static org.firebirdsql.common.FBTestProperties.createDefaultMcf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TestDataSourceSerialization.java
 *
 * @author David Jencks
 */
class DataSourceSerializationTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    @Test
    void testDataSourceSerialization() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        DataSource ds = mcf.createConnectionFactory();
        assertNotNull(ds, "Could not get DataSource");

        try (Connection c = ds.getConnection()) {
            assertNotNull(c, "Could not get Connection");
        }
        
        MarshalledObject<DataSource> mo = new MarshalledObject<>(ds);
        ds = mo.get();
        try (Connection c = ds.getConnection()) {
            assertNotNull(c, "Could not get Connection");
        }
    }
}

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
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
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

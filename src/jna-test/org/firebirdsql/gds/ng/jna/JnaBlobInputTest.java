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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.BaseTestBlob;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;

/**
 * Test for input blobs in the JNA implementation.
 *
 * @author Mark Rotteveel
 */
class JnaBlobInputTest extends BaseTestBlob {

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected JnaDatabase createFbDatabase(FbConnectionProperties connectionInfo) throws SQLException {
        final JnaDatabase db = factory.connect(connectionInfo);
        db.attach();
        return db;
    }

    @Override
    protected JnaDatabase createDatabaseConnection() throws SQLException {
        return (JnaDatabase) super.createDatabaseConnection();
    }

}

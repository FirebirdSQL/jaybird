// SPDX-FileCopyrightText: Copyright 2023-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.BaseTestInputBlob;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;

/**
 * Test for input blobs in the JNA implementation.
 *
 * @author Mark Rotteveel
 */
class JnaBlobInputTest extends BaseTestInputBlob {

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

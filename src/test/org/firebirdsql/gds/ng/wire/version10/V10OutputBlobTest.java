// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.BaseTestOutputBlob;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10OutputBlobTest extends BaseTestOutputBlob {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(10);

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.excludesNativeOnly();

    private final V10CommonConnectionInfo commonConnectionInfo = commonConnectionInfo();

    protected V10CommonConnectionInfo commonConnectionInfo() {
        return new V10CommonConnectionInfo();
    }

    protected final ProtocolCollection getProtocolCollection() {
        return commonConnectionInfo.getProtocolCollection();
    }

    @SuppressWarnings("resource")
    @Override
    protected final FbDatabase createFbDatabase(FbConnectionProperties connectionInfo) throws SQLException {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.getPlatformDefault(), getProtocolCollection());
        gdsConnection.socketConnect();
        FbWireDatabase db = gdsConnection.identify();
        db.attach();
        return db;
    }

    @Override
    protected final FbWireDatabase createDatabaseConnection() throws SQLException {
        return (FbWireDatabase) super.createDatabaseConnection();
    }

}

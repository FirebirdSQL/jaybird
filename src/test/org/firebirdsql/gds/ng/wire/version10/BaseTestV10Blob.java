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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.BaseTestBlob;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;

import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class BaseTestV10Blob extends BaseTestBlob {

    private final V10CommonConnectionInfo commonConnectionInfo = commonConnectionInfo();

    protected abstract V10CommonConnectionInfo commonConnectionInfo();

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

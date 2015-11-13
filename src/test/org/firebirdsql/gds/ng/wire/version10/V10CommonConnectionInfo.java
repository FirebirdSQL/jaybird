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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.*;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;

/**
 * Class to contain common connection information shared by the V10 tests.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10CommonConnectionInfo {

    private final ProtocolDescriptor protocolDescriptor;
    private final Class<? extends FbWireDatabase> expectedDatabaseType;
    private final Class<? extends FbWireService> expectedServiceType;

    V10CommonConnectionInfo() {
        this(new Version10Descriptor(), V10Database.class, V10Service.class);
    }

    protected V10CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        this.protocolDescriptor = protocolDescriptor;
        this.expectedDatabaseType = expectedDatabaseType;
        this.expectedServiceType = expectedServiceType;
    }

    public final IConnectionProperties getDatabaseConnectionInfo() {
        IConnectionProperties connectionInfo = new FbConnectionProperties();
        setAttachProperties(connectionInfo);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        return connectionInfo;
    }

    public final IServiceProperties getServiceConnectionInfo() {
        IServiceProperties connectionInfo = new FbServiceProperties();
        setAttachProperties(connectionInfo);
        return connectionInfo;
    }

    private void setAttachProperties(IAttachProperties<?> attachProperties) {
        attachProperties.setServerName(FBTestProperties.DB_SERVER_URL);
        attachProperties.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        attachProperties.setUser(DB_USER);
        attachProperties.setPassword(DB_PASSWORD);
        attachProperties.setEncoding("NONE");
    }

    public final ProtocolDescriptor getProtocolDescriptor() {
        return protocolDescriptor;
    }

    public final ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(getProtocolDescriptor());
    }

    public final WireDatabaseConnection getDummyDatabaseConnection() throws SQLException {
        FbConnectionProperties connectionInfo = new FbConnectionProperties();
        connectionInfo.setEncoding("NONE");
        return new WireDatabaseConnection(connectionInfo);
    }

    public final AbstractFbWireDatabase createDummyDatabase() throws SQLException {
        return (AbstractFbWireDatabase) getProtocolDescriptor().createDatabase(getDummyDatabaseConnection());
    }

    public final WireServiceConnection getDummyServiceConnection() throws SQLException {
        FbServiceProperties connectionInfo = new FbServiceProperties();
        connectionInfo.setEncoding("NONE");
        return new WireServiceConnection(connectionInfo);
    }

    public final AbstractFbWireService createDummyService() throws SQLException {
        return (AbstractFbWireService) getProtocolDescriptor().createService(getDummyServiceConnection());
    }

    public final AbstractWireOperations createDummyWireOperations(WarningMessageCallback warningMessageCallback)
            throws SQLException {
        return (AbstractWireOperations) getProtocolDescriptor().createWireOperations(getDummyDatabaseConnection(),
                warningMessageCallback, new Object());
    }

    public final Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return expectedDatabaseType;
    }

    public final Class<? extends FbWireService> getExpectedServiceType() {
        return expectedServiceType;
    }
}

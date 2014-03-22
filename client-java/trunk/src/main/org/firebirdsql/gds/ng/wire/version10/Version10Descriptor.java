/*
 * $Id$
 * 
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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 10 protocol. This version applies to Firebird 1.x and 2.0,
 * but also works with newer Firebird versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class Version10Descriptor extends AbstractProtocolDescriptor implements ProtocolDescriptor {

    public Version10Descriptor() {
        super(
                WireProtocolConstants.PROTOCOL_VERSION10,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc,
                WireProtocolConstants.ptype_batch_send,
                1);
    }

    @Override
    public FbWireDatabase createDatabase(final WireConnection connection) {
        return new V10Database(connection, this);
    }

    @Override
    public FbWireTransaction createTransaction(final FbWireDatabase database, final int transactionHandle) {
        return new V10Transaction(database, transactionHandle);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V10Statement(database);
    }

    @Override
    public DatabaseParameterBuffer createDatabaseParameterBuffer(final WireConnection connection) {
        final IConnectionProperties connectionProperties = connection.getConnectionProperties();
        final DatabaseParameterBuffer dpb = new DatabaseParameterBufferImp();

        // Map standard properties
        dpb.addArgument(ISCConstants.isc_dpb_lc_ctype, connection.getEncodingDefinition().getFirebirdEncodingName());
        if (connectionProperties.getPageCacheSize() != IConnectionProperties.DEFAULT_BUFFERS_NUMBER) {
            dpb.addArgument(ISCConstants.isc_dpb_num_buffers, connectionProperties.getPageCacheSize());
        }
        if (connectionProperties.getUser() != null) {
            dpb.addArgument(ISCConstants.isc_dpb_user_name, connectionProperties.getUser());
        }
        if (connectionProperties.getPassword() != null) {
            dpb.addArgument(ISCConstants.isc_dpb_password, connectionProperties.getPassword());
        }
        if (connectionProperties.getRoleName() != null) {
            dpb.addArgument(ISCConstants.isc_dpb_sql_role_name, connectionProperties.getRoleName());
        }
        dpb.addArgument(ISCConstants.isc_dpb_sql_dialect, connectionProperties.getConnectionDialect());
        if (connectionProperties.getConnectTimeout() != IConnectionProperties.DEFAULT_CONNECT_TIMEOUT) {
            dpb.addArgument(ISCConstants.isc_dpb_connect_timeout, connectionProperties.getConnectTimeout());
        }
        // TODO Include ProcessID and ProcessName here or elsewhere?

        // Map non-standard properties
        // TODO Implement support for non-standard properties

        return dpb;
    }

    @Override
    public BlrCalculator createBlrCalculator(final FbWireDatabase database) {
        final short connectionDialect = database.getConnectionDialect();
        return connectionDialect == ISCConstants.SQL_DIALECT_V6 ? V10BlrCalculator.CALCULATOR_DIALECT_3 : new V10BlrCalculator(connectionDialect);
    }

    @Override
    public FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction) {
        return new V10OutputBlob(database, transaction);
    }

    @Override
    public FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction, long blobId) {
        return new V10InputBlob(database, transaction, blobId);
    }
}

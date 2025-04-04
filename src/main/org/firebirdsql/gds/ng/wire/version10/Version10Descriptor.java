// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceRequestBufferImp;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.wire.*;

import java.sql.SQLException;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 10 protocol. This version applies to Firebird 1.x and 2.0,
 * but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class Version10Descriptor extends AbstractProtocolDescriptor implements ProtocolDescriptor {

    public Version10Descriptor() {
        this(
                WireProtocolConstants.PROTOCOL_VERSION10,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc,
                WireProtocolConstants.ptype_batch_send,
                false,
                1);
    }

    protected Version10Descriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        super(version, architecture, minimumType, maximumType, supportsWireCompression, weight);
    }

    @Override
    public FbWireDatabase createDatabase(final WireDatabaseConnection connection) {
        return new V10Database(connection, this);
    }

    @Override
    public FbWireService createService(final WireServiceConnection connection) {
        return new V10Service(connection, this);
    }

    @Override
    public ServiceParameterBuffer createServiceParameterBuffer(final WireServiceConnection connection) {
        return new ServiceParameterBufferImp(ServiceParameterBufferImp.SpbMetaData.SPB_VERSION_2,
                connection.getEncoding());
    }

    @Override
    public ServiceRequestBuffer createServiceRequestBuffer(final WireServiceConnection connection) {
        return new ServiceRequestBufferImp(ServiceRequestBufferImp.SrbMetaData.SRB_VERSION_2, connection.getEncoding());
    }

    @Override
    public FbWireTransaction createTransaction(final FbWireDatabase database, final int transactionHandle,
            final TransactionState initialState) {
        return new V10Transaction(database, transactionHandle, initialState);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V10Statement(database);
    }

    @Override
    public FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) throws SQLException {
        return new V10OutputBlob(database, transaction, blobParameterBuffer);
    }

    @Override
    public FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer, long blobId) throws SQLException {
        return new V10InputBlob(database, transaction, blobParameterBuffer, blobId);
    }

    @Override
    public FbWireAsynchronousChannel createAsynchronousChannel(FbWireDatabase database) {
        return new V10AsynchronousChannel(database);
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V10WireOperations(connection, defaultWarningMessageCallback);
    }

    @Override
    protected ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter() {
        return new V10ParameterConverter();
    }
}

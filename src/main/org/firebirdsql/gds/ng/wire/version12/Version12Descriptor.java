// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceRequestBufferImp;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version10.*;
import org.firebirdsql.gds.ng.wire.version11.V11InputBlob;
import org.firebirdsql.gds.ng.wire.version11.V11OutputBlob;
import org.firebirdsql.gds.ng.wire.version11.V11WireOperations;

import java.sql.SQLException;

/**
 * The {@link org.firebirdsql.gds.ng.wire.ProtocolDescriptor} for the Firebird version 12 protocol. This version
 * applies to Firebird 2.5, but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class Version12Descriptor extends AbstractProtocolDescriptor implements ProtocolDescriptor {

    public Version12Descriptor() {
        super(
                WireProtocolConstants.PROTOCOL_VERSION12,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                false,
                3);
    }

    @Override
    public FbWireDatabase createDatabase(final WireDatabaseConnection connection) {
        return new V12Database(connection, this);
    }

    @Override
    public FbWireService createService(WireServiceConnection connection) {
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
        return new V12Statement(database);
    }

    @Override
    public FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) throws SQLException {
        return new V11OutputBlob(database, transaction, blobParameterBuffer);
    }

    @Override
    public FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer, long blobId) throws SQLException {
        return new V11InputBlob(database, transaction, blobParameterBuffer, blobId);
    }

    @Override
    public FbWireAsynchronousChannel createAsynchronousChannel(FbWireDatabase database) {
        return new V10AsynchronousChannel(database);
    }

    @Override
    protected ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter() {
        return new V12ParameterConverter();
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V11WireOperations(connection, defaultWarningMessageCallback);
    }
}

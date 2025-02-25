// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version15;

import org.firebirdsql.encodings.Encoding;
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
import org.firebirdsql.gds.ng.wire.version13.V13ParameterConverter;
import org.firebirdsql.gds.ng.wire.version13.V13Statement;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 15 protocol. This version
 * applies to Firebird 3.0.2, but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class Version15Descriptor extends AbstractProtocolDescriptor implements ProtocolDescriptor {

    public Version15Descriptor() {
        super(
                WireProtocolConstants.PROTOCOL_VERSION15,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                true,
                5);
    }

    @Override
    public FbWireDatabase createDatabase(final WireDatabaseConnection connection) {
        return new V15Database(connection, this);
    }

    @Override
    public FbWireService createService(WireServiceConnection connection) {
        return new V10Service(connection, this);
    }

    @Override
    public ServiceParameterBuffer createServiceParameterBuffer(final WireServiceConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        return new ServiceParameterBufferImp(ServiceParameterBufferImp.SpbMetaData.SPB_VERSION_2,
                stringEncoding);
    }

    @Override
    public ServiceRequestBuffer createServiceRequestBuffer(final WireServiceConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        return new ServiceRequestBufferImp(ServiceRequestBufferImp.SrbMetaData.SRB_VERSION_2, stringEncoding);
    }

    @Override
    public FbWireTransaction createTransaction(final FbWireDatabase database, final int transactionHandle,
            final TransactionState initialState) {
        return new V10Transaction(database, transactionHandle, initialState);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V13Statement(database);
    }

    @Override
    public FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        return new V10OutputBlob(database, transaction, blobParameterBuffer);
    }

    @Override
    public FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId) {
        return new V10InputBlob(database, transaction, blobParameterBuffer, blobId);
    }

    @Override
    public FbWireAsynchronousChannel createAsynchronousChannel(FbWireDatabase database) {
        return new V10AsynchronousChannel(database);
    }

    @Override
    protected ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter() {
        return new V13ParameterConverter();
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V15WireOperations(connection, defaultWarningMessageCallback);
    }
}

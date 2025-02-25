// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.wire.ProtocolDescriptor} that returns null for the
 * <code>createXXX</code> methods.
 * <p>
 * For testing purposes only
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class EmptyProtocolDescriptor extends AbstractProtocolDescriptor {

    public EmptyProtocolDescriptor(final int version, final int architecture, final int minimumType,
            final int maximumType, final int weight) {
        super(version, architecture, minimumType, maximumType, false, weight);
    }

    @Override
    public FbWireDatabase createDatabase(WireDatabaseConnection connection) {
        return null;
    }

    @Override
    public FbWireService createService(WireServiceConnection connection) {
        return null;
    }

    @Override
    public ServiceParameterBuffer createServiceParameterBuffer(final WireServiceConnection connection) {
        return null;
    }

    @Override
    public ServiceRequestBuffer createServiceRequestBuffer(final WireServiceConnection connection) {
        return null;
    }

    @Override
    public FbWireTransaction createTransaction(FbWireDatabase database, int transactionHandle,
            final TransactionState initialState) {
        return null;
    }

    @Override
    public FbWireStatement createStatement(FbWireDatabase database) {
        return null;
    }

    @Override
    public BlrCalculator createBlrCalculator(FbWireDatabase database) {
        return null;
    }

    @Override
    public FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) {
        return null;
    }

    @Override
    public FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer, long blobId) {
        return null;
    }

    @Override
    public FbWireAsynchronousChannel createAsynchronousChannel(FbWireDatabase database) {
        return null;
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return null;
    }

    @Override
    protected ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter() {
        return new ParameterConverter<WireDatabaseConnection, WireServiceConnection>() {
            @Override
            public DatabaseParameterBuffer toDatabaseParameterBuffer(WireDatabaseConnection connection) {
                return null;
            }

            @Override
            public ServiceParameterBuffer toServiceParameterBuffer(WireServiceConnection connection) {
                return null;
            }
        };
    }
}

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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.wire.ProtocolDescriptor} that returns null for the
 * <code>createXXX</code> methods.
 * <p>
 * For testing purposes only
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class EmptyProtocolDescriptor extends AbstractProtocolDescriptor {

    public EmptyProtocolDescriptor(final int version, final int architecture, final int minimumType,
            final int maximumType, final int weight) {
        super(version, architecture, minimumType, maximumType, weight);
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
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
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

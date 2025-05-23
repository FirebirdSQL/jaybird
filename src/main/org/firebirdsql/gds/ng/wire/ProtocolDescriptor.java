// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.fields.BlrCalculator;

import java.sql.SQLException;

/**
 * Descriptor of protocol information.
 * <p>
 * The driver maintains a list of default protocol descriptors that are loaded using a {@link java.util.ServiceLoader}
 * from the file {@code META-INF/services/org.firebirdsql.gds.ng.wire.ProtocolDescriptor}
 * </p>
 * <p>
 * Protocol descriptors loaded this way are required to adhere to the following rules:
 * <ul>
 * <li>They provide a no-arg constructor</li>
 * <li>All instances of a specific implementation class created with the no-arg constructor have the
 * same {@link Object#hashCode()}.</li>
 * <li>All instances of a specific implementation class created with the no-arg constructor are considered equal to
 * each other by the {@link Object#equals(Object)} implementation</li>
 * </ul>
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface ProtocolDescriptor {

    /**
     * @return The protocol version
     */
    int getVersion();

    /**
     * @return Protocol architecture
     */
    int getArchitecture();

    /**
     * @return Minimum supported protocol type
     */
    int getMinimumType();

    /**
     * @return Maximum supported protocol type
     */
    int getMaximumType();

    /**
     * @return {@code true} when zlib wire compression is supported
     */
    boolean supportsWireCompression();

    /**
     * @return Preference weight
     */
    int getWeight();

    /**
     * Create {@link FbWireDatabase} implementation for this protocol.
     *
     * @param connection
     *         WireDatabaseConnection to this database
     * @return FbWireDatabase implementation
     */
    FbWireDatabase createDatabase(WireDatabaseConnection connection);

    /**
     * Create {@link FbWireService} implementation for this protocol.
     *
     * @param connection
     *         WireServiceConnection to this service
     * @return FbWireService implementation
     */
    FbWireService createService(WireServiceConnection connection);

    /**
     * Create {@link FbTransaction} implementation for this protocol.
     *
     * @param database
     *         FbWireDatabase of the current database
     * @param transactionHandle
     *         Transaction handle
     * @param initialState
     *         Initial transaction state (either {@link org.firebirdsql.gds.ng.TransactionState#ACTIVE}
     *         or {@link org.firebirdsql.gds.ng.TransactionState#PREPARED}).
     * @return FbTransaction implementation
     */
    FbWireTransaction createTransaction(FbWireDatabase database, int transactionHandle, TransactionState initialState);

    /**
     * Create {@link org.firebirdsql.gds.ng.FbStatement} implementation for this protocol.
     *
     * @param database
     *         FbWireDatabase of the current database
     * @return FbStatement implementation
     */
    FbWireStatement createStatement(FbWireDatabase database);

    /**
     * Create {@link DatabaseParameterBuffer} implementation and populate it with supported
     * properties for this protocol version.
     *
     * @param connection
     *         Connection
     * @return DatabaseParameterBuffer implementation
     */
    DatabaseParameterBuffer createDatabaseParameterBuffer(WireDatabaseConnection connection) throws SQLException;

    /**
     * Create an attach {@link ServiceParameterBuffer} implementation and populate it with supported properties for
     * this protocol version.
     *
     * @param connection
     *         Connection
     * @return ServiceParameterBuffer implementation for attach
     */
    ServiceParameterBuffer createAttachServiceParameterBuffer(WireServiceConnection connection) throws SQLException;

    /**
     * Creates a normal {@link ServiceParameterBuffer}.
     *
     * @param connection
     *         Connection
     * @return ServiceParameterBuffer implementation
     */
    ServiceParameterBuffer createServiceParameterBuffer(WireServiceConnection connection);

    /**
     * @return An empty service request buffer
     */
    ServiceRequestBuffer createServiceRequestBuffer(WireServiceConnection connection);

    /**
     * Create {@link BlrCalculator} implementation for this protocol version.
     *
     * @param database
     *         FbWireDatabase of the current database
     * @return BlrCalculator implementation
     */
    BlrCalculator createBlrCalculator(FbWireDatabase database);

    /**
     * Create an output {@link FbWireBlob} implementation for this protocol version.
     *
     * @param database
     *         FbWireDatabase of the current database
     * @param transaction
     *         FbWireTransaction to associate with the blob
     * @param blobParameterBuffer
     *         Blob Parameter Buffer
     * @return FbWireBlob implementation
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     */
    FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) throws SQLException;

    /**
     * Create an input {@link FbWireBlob} implementation for this protocol version.
     *
     * @param database
     *         FbWireDatabase of the current database
     * @param transaction
     *         FbWireTransaction to associate with the blob
     * @param blobParameterBuffer
     *         Blob Parameter Buffer
     * @param blobId
     *         Blob Id (must be non-zero for input blob)
     * @return FbWireBlob implementation
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     */
    FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer, long blobId) throws SQLException;

    /**
     * Create a disconnected asynchronous channel.
     *
     * @param database
     *         The parent database handle.
     * @return Asynchronous channel implementation
     */
    FbWireAsynchronousChannel createAsynchronousChannel(FbWireDatabase database);

    /**
     * Create an {@link FbWireOperations} implementation for this protocol version.
     *
     * @param connection
     *         WireConnection instance (non-{@code null})
     * @param defaultWarningMessageCallback
     *         default warning message callback (non-{@code null})
     * @return Wire operations implementation
     */
    FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback);
}

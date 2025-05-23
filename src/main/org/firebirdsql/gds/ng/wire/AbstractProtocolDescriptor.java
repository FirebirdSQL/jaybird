// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.fields.BlrCalculator;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Abstract class to simplify implementation of {@link ProtocolDescriptor}
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractProtocolDescriptor implements ProtocolDescriptor {

    private final int version;
    private final int architecture;
    private final int minimumType;
    private final int maximumType;
    private final boolean supportsWireCompression;
    private final int weight;
    private final int hashCode;

    /**
     * Initializes the basic ProtocolDescriptor fields.
     *
     * @param version
     *         Version of the protocol
     * @param architecture
     *         Architecture of the protocol
     * @param minimumType
     *         Minimum supported protocol type
     * @param maximumType
     *         Maximum supported protocol type
     * @param supportsWireCompression
     *         {@code true} if this version supports zlib wire compression
     * @param weight
     *         Selection weight (higher values have higher preference)
     */
    protected AbstractProtocolDescriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        this.version = version;
        this.architecture = architecture;
        this.minimumType = minimumType;
        this.maximumType = maximumType;
        this.weight = weight;
        this.supportsWireCompression = supportsWireCompression;
        hashCode = Objects.hash(version, architecture, minimumType, maximumType, weight);
    }

    @Override
    public final int getVersion() {
        return version;
    }

    @Override
    public final int getArchitecture() {
        return architecture;
    }

    @Override
    public final int getMinimumType() {
        return minimumType;
    }

    @Override
    public final int getMaximumType() {
        return maximumType;
    }

    @Override
    public final boolean supportsWireCompression() {
        return supportsWireCompression;
    }

    @Override
    public final int getWeight() {
        return weight;
    }

    @Override
    public BlrCalculator createBlrCalculator(FbWireDatabase database) {
        return DefaultBlrCalculator.of(database.getConnectionDialect(), database.getServerVersion());
    }

    /**
     * @return Hash code based on {@code version}, {@code architecture}, {@code minimumType}, {@code maximumType} and
     * {@code weight}.
     */
    @Override
    public final int hashCode() {
        return hashCode;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation for the rules described in {@link ProtocolDescriptor}. Returns {@code true} if the other
     * object is of the exact same class as this instance.
     * </p>
     */
    @Override
    public boolean equals(Object other) {
        return other != null && this.getClass() == other.getClass();
    }

    @Override
    public final DatabaseParameterBuffer createDatabaseParameterBuffer(WireDatabaseConnection connection)
            throws SQLException {
        return getParameterConverter()
                .toDatabaseParameterBuffer(connection);
    }

    @Override
    public final ServiceParameterBuffer createAttachServiceParameterBuffer(WireServiceConnection connection)
            throws SQLException {
        return getParameterConverter()
                .toServiceParameterBuffer(connection);
    }

    /**
     * @return {@code ParameterConverter} for populating the database parameter buffer.
     */
    protected abstract ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter();
}

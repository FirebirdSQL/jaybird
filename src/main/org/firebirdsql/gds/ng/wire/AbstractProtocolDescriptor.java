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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.util.ObjectUtils;

import java.sql.SQLException;

/**
 * Abstract class to simplify implementation of {@link ProtocolDescriptor}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractProtocolDescriptor implements ProtocolDescriptor {

    private final int version;
    private final int architecture;
    private final int minimumType;
    private final int maximumType;
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
     * @param weight
     *         Preference weight
     */
    protected AbstractProtocolDescriptor(int version, int architecture, int minimumType, int maximumType, int weight) {
        this.version = version;
        this.architecture = architecture;
        this.minimumType = minimumType;
        this.maximumType = maximumType;
        this.weight = weight;
        hashCode = ObjectUtils.hash(version, architecture, minimumType, maximumType, weight);
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
    public final int getWeight() {
        return weight;
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
    public final ServiceParameterBuffer createServiceParameterBuffer(WireServiceConnection connection)
            throws SQLException {
        return getParameterConverter()
                .toServiceParameterBuffer(connection);
    }

    /**
     * @return {@code ParameterConverter} for populating the database parameter buffer.
     */
    protected abstract ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter();
}

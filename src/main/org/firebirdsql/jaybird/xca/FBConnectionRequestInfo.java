/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.gds.ng.IConnectionProperties;

import java.io.Serial;
import java.io.Serializable;

/**
 * The class {@code FBConnectionRequestInfo} holds connection-specific information such as user, password, and other
 * information.
 *
 * @author David Jencks
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public final class FBConnectionRequestInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @SuppressWarnings("java:S1948")
    private final IConnectionProperties connectionProperties;

    /**
     * Creates a connection request info based on a set of connection properties.
     * <p>
     * Mutable connection properties are used as is, so the caller is responsible to provide a copy if mutations
     * shouldn't propagate. Immutable properties are automatically copied to a mutable version.
     * </p>
     *
     * @param connectionProperties
     *         Connection properties
     * @since 5
     */
    public FBConnectionRequestInfo(IConnectionProperties connectionProperties) {
        this.connectionProperties =
                connectionProperties.isImmutable() ? connectionProperties.asNewMutable() : connectionProperties;
    }

    public void setUserName(String userName) {
        connectionProperties.setUser(userName);
    }

    public void setPassword(String password) {
        connectionProperties.setPassword(password);
    }

    /**
     * @return A mutable view on the connection properties of this connection request
     * @since 5
     */
    public IConnectionProperties asIConnectionProperties() {
        return connectionProperties;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FBConnectionRequestInfo other)) return false;
        return connectionProperties.equals(other.connectionProperties);
    }

    @Override
    public int hashCode() {
        return connectionProperties.hashCode();
    }

}

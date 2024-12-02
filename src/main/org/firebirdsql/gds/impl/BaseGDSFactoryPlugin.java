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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jdbc.FBConnection;

import java.sql.SQLException;

/**
 * Base class for {@link GDSFactoryPlugin} implementations.
 * <p>
 * Handles commonalities across existing implementations.
 * </p>
 * 
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class BaseGDSFactoryPlugin implements GDSFactoryPlugin {

    @Override
    public Class<?> getConnectionClass() {
        return FBConnection.class;
    }

    @Override
    public String getDefaultProtocol() {
        return getSupportedProtocolList().get(0);
    }

    @Override
    public String getDatabasePath(String jdbcUrl) throws SQLException {
        for (String protocol : getSupportedProtocolList()) {
            if (jdbcUrl.startsWith(protocol)) {
                return jdbcUrl.substring(protocol.length());
            }
        }

        throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                .messageParameter(jdbcUrl, "JDBC URL not supported by protocol: " + getTypeName())
                .toSQLException();
    }

    /**
     * Checks if {@code path} is not {@code null}.
     *
     * @param path
     *         path to check
     * @throws SQLException
     *         if {@code path} is {@code null}
     * @since 6
     */
    protected static void requirePath(String path) throws SQLException {
        if (path == null) {
            throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_databasePathRequired);
        }
    }

    @Override
    public String getDatabasePath(String server, Integer port, String path) throws SQLException {
        requirePath(path);

        if (server == null) {
            return path;
        }

        var sb = new StringBuilder();
        sb.append("//").append(server);
        if (port != null) {
            sb.append(':').append(port.intValue());
        }
        sb.append('/').append(path);

        return sb.toString();
    }

    @Override
    public final int hashCode() {
        return getTypeName().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return obj != null && (obj == this || getClass() == obj.getClass());
    }
}

/*
 * $Id$
 *
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
package org.firebirdsql.util;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jdbc.FBConnection;

import java.sql.SQLException;

/**
 * Helper class that reports if a Firebird version supports a specific feature. Intended as a repository for
 * tests to check their assumptions, or decide on test behavior based on functionality support.
 * <p>
 * TODO Consider moving to org.firebirdsql.util in main instead of test
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class FirebirdSupportInfo {

    private final GDSServerVersion serverVersion;

    private FirebirdSupportInfo(GDSServerVersion serverVersion) {
        if (serverVersion == null) {
            throw new NullPointerException("serverVersion");
        }
        if (serverVersion.equals(GDSServerVersion.INVALID_VERSION)) {
            throw new IllegalArgumentException("serverVersion is an invalid version (GDSServerVersion.INVALID_VERSION)");
        }
        this.serverVersion = serverVersion;
    }

    /**
     * Checks if BIGINT is supported.
     * <p>
     * Low level this feature was added in Interbase 6.0 / Firebird 1.0, but it was never surfaced in DDL
     * </p>
     *
     * @return <code>true</code> when the data type BIGINT is supported
     */
    public boolean supportsBigint() {
        return serverVersion.isEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when the data type BOOLEAN is supported
     */
    public boolean supportsBoolean() {
        return serverVersion.isEqualOrAbove(3, 0);
    }

    /**
     * @return <code>true</code> when the COMMENT statement is supported
     */
    public boolean supportsComment() {
        return serverVersion.isEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when RDB$GET_CONTEXT and RDB$SET_CONTEXT are supported
     */
    public boolean supportsGetSetContext() {
        return serverVersion.isEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when CASE (simple or searched) is supported
     */
    public boolean supportsCase() {
        return serverVersion.isEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when the blob character set is reported in the scale of the field descriptor
     */
    public boolean reportsBlobCharSetInDescriptor() {
        // TODO Check if this is the right version
        return serverVersion.isEqualOrAbove(1, 5);
    }

    /**
     * TODO: Check if this is for all types or only for metadata.
     *
     * @return <code>true</code> when the length of the field descriptor reports the byte length (max byte per char * char length)
     */
    public boolean reportsByteLengthInDescriptor() {
        return serverVersion.isEqualOrAbove(1, 5);
    }

    /**
     * TODO: Add methods for other RETURNING types?
     *
     * @return <code>true</code> when INSERT ... RETURNING ... is supported
     */
    public boolean supportsInsertReturning() {
        return serverVersion.isEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when the server knows the UTF8 character set (NOTE: For firebird 1.5 it is an alias for
     * UNICODE_FSS)
     */
    public boolean supportsUtf8() {
        return serverVersion.isEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when SAVEPOINT is supported
     */
    public boolean supportsSavepoint() {
        return serverVersion.isEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when CREATE/ALTER/DROP USER is supported
     */
    public boolean supportsSqlUserManagement() {
        return serverVersion.isEqualOrAbove(2, 5);
    }

    /**
     * @param serverVersion
     *         Server version
     * @return FirebirdVersionSupport instance
     */
    public static FirebirdSupportInfo supportInfoFor(GDSServerVersion serverVersion) {
        return new FirebirdSupportInfo(serverVersion);
    }

    /**
     * @param database
     *         Low level database object
     * @return FirebirdVersionSupport instance
     */
    public static FirebirdSupportInfo supportInfoFor(FbDatabase database) {
        return supportInfoFor(database.getServerVersion());
    }

    /**
     * @param connection
     *         A database connection (NOTE: {@link java.sql.Connection} is used, but it most be or unwrap to a
     *         {@link org.firebirdsql.jdbc.FBConnection}.
     * @return FirebirdVersionSupport instance
     */
    public static FirebirdSupportInfo supportInfoFor(java.sql.Connection connection) {
        try {
            if (connection.isWrapperFor(FBConnection.class)) {
                return supportInfoFor(connection.unwrap(FBConnection.class).getFbDatabase());
            } else {
                throw new IllegalArgumentException("connection needs to be an FBConnection");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}

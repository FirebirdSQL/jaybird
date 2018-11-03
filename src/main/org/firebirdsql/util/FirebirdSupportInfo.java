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
package org.firebirdsql.util;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.wire.auth.legacy.LegacyAuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp224AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp256AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp384AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp512AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.SrpAuthenticationPluginSpi;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.management.PageSizeConstants;

import java.sql.SQLException;

/**
 * Helper class that reports if a Firebird version supports a specific feature.
 * <p>
 * Intended as a repository for Jaybird to check for functionality support, or tests to check their assumptions, or
 * decide on test or application behavior based on functionality support.
 * </p>
 * <p>
 * Primary reason for existence of this class is to support version dependent functionality in Jaybird or
 * version dependent tests in the Jaybird test suite, so feature checks are only added when they are necessary for
 * Jaybird or the test suite. That said: if you miss feature checks, don't hesitate to create an issue in
 * the <a href="http://tracker.firebirdsql.org/browse/JDBC">Jaybird tracker</a>.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class FirebirdSupportInfo {

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
     * Check if the major.minor of this version is equal to or larger than the specified version.
     *
     * @param majorVersion
     *         Major version
     * @param minorVersion
     *         Minor version
     * @return {@code true} when current major is larger than required, or major is same and minor is equal to or
     * larger than required
     */
    public boolean isVersionEqualOrAbove(int majorVersion, int minorVersion) {
        return serverVersion.isEqualOrAbove(majorVersion, minorVersion);
    }

    /**
     * Check if the major.minor.variant of this version is equal to or larger than the specified required version.
     *
     * @param requiredMajorVersion
     *         Required major version
     * @param requiredMinorVersion
     *         Required minor version
     * @param requiredVariant
     *         Required variant version
     * @return {@code true} when current major is larger than required, or major is same and minor is equal to required
     * and variant equal to or larger than required, or major is same and minor is larger than required
     */
    public boolean isVersionEqualOrAbove(int requiredMajorVersion, int requiredMinorVersion, int requiredVariant) {
        return serverVersion.isEqualOrAbove(requiredMajorVersion, requiredMinorVersion, requiredVariant);
    }

    /**
     * Check if the major.minor of this version is below the specified version.
     * <p>
     * Equivalent to {@code !isVersionEqualOrAbove(majorVersion, minorVersion)}.
     * </p>
     *
     * @param majorVersion
     *         Major version
     * @param minorVersion
     *         Minor version
     * @return {@code true} when current major is smaller than the specified major, or major is same and minor is
     * smaller than the specified minor
     */
    public boolean isVersionBelow(int majorVersion, int minorVersion) {
        return !isVersionEqualOrAbove(majorVersion, minorVersion);
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
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when the data type BOOLEAN is supported
     */
    public boolean supportsBoolean() {
        return isVersionEqualOrAbove(3, 0);
    }

    /**
     * @return {@code true} when the data type DECFLOAT is supported
     */
    public boolean supportsDecfloat() {
        return isVersionEqualOrAbove(4, 0);
    }

    /**
     * Support for decimal (and numeric) precision.
     *
     * @param precision Precision
     * @return {@code true} when DECIMAL (and NUMERIC) support the supplied precision; 0 or negative precision always
     * return {@code false}
     */
    public boolean supportsDecimalPrecision(int precision) {
        if (precision < 1) {
            return false;
        } else if (precision <= 18) {
            // all Firebird versions
            return true;
        } else if (precision <= 34) {
            return isVersionEqualOrAbove(4, 0);
        }
        return false;
    }

    /**
     * @return The maximum decimal and numeric precision
     */
    public int maxDecimalPrecision() {
        if (isVersionEqualOrAbove(4, 0)) {
            return 34;
        }
        return 18;
    }

    /**
     * @return <code>true</code> when the COMMENT statement is supported
     */
    public boolean supportsComment() {
        return isVersionEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when RDB$GET_CONTEXT and RDB$SET_CONTEXT are supported
     */
    public boolean supportsGetSetContext() {
        return isVersionEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when CASE (simple or searched) is supported
     */
    public boolean supportsCase() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when the blob character set is reported in the scale of the field descriptor
     */
    public boolean reportsBlobCharSetInDescriptor() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * TODO: Check if this is for all types or only for metadata.
     *
     * @return <code>true</code> when the length of the field descriptor reports the byte length (max byte per char *
     * char length)
     */
    public boolean reportsByteLengthInDescriptor() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * TODO: Add methods for other RETURNING types?
     *
     * @return <code>true</code> when INSERT ... RETURNING ... is supported
     */
    public boolean supportsInsertReturning() {
        return isVersionEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when UPDATE ... RETURNING ... is supported
     */
    public boolean supportsUpdateReturning() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * @return <code>true</code> when the server knows the UTF8 character set (NOTE: For firebird 1.5 it is an alias for
     * UNICODE_FSS)
     */
    public boolean supportsUtf8() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when SAVEPOINT is supported
     */
    public boolean supportsSavepoint() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return <code>true</code> when EXECUTE BLOCK is supported
     */
    public boolean supportsExecuteBlock() {
        return isVersionEqualOrAbove(2, 0);
    }

    /**
     * @return <code>true</code> when CREATE/ALTER/DROP USER is supported
     */
    public boolean supportsSqlUserManagement() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return <code>true</code> when fb_cancel_operation is supported
     */
    public boolean supportsCancelOperation() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} when field descriptors contain table alias information
     */
    public boolean supportsTableAlias() {
        return isVersionEqualOrAbove(2, 0);
    }

    /**
     * @return {@code true} when the {@code NULL} data type and {@code ? IS NULL} is supported
     */
    public boolean supportsNullDataType() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} when {@code isc_spb_sec_userid} and {@code isc_spb_sec_groupid} are supported.
     */
    public boolean supportsUserAndGroupIdInUser() {
        return isVersionBelow(3, 0);
    }

    /**
     * Checks support for protocol versions. The check is limited to those protocol versions supported by Jaybird
     * (10-13 at this time).
     *
     * @param protocolVersion
     *         Protocol version number
     * @return {@code true} when the database supports the specified protocol
     */
    public boolean supportsProtocol(int protocolVersion) {
        switch (protocolVersion) {
        case 10:
            return true;
        case 11:
            return isVersionEqualOrAbove(2, 1);
        case 12:
            return isVersionEqualOrAbove(2, 5);
        case 13:
            return isVersionEqualOrAbove(3, 0);
        default:
            return false;
        }
    }

    /**
     * @return {@code true} when custom exception messages are supported.
     */
    public boolean supportsCustomExceptionMessages() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return {@code true} when parametrized exceptions are supported.
     */
    public boolean supportsParametrizedExceptions() {
        return isVersionEqualOrAbove(3, 0);
    }

    /**
     * @return {@code true} when monitoring tables are supported.
     */
    public boolean supportsMonitoringTables() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * @return {@code true} when global temporary tables (GTTs) are supported.
     */
    public boolean supportsGlobalTemporaryTables() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} when blobs are fully searchable (eg using `LIKE`).
     */
    public boolean supportsFullSearchableBlobs() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * @return {@code true} when identity columns are supported.
     */
    public boolean supportsIdentityColumns() {
        return isVersionEqualOrAbove(3, 0);
    }

    /**
     * @return The maximum number of characters in an identifier.
     */
    public int maxIdentifierLengthCharacters() {
        if (isVersionEqualOrAbove(4, 0)) {
            // Technically this is configurable
            return 63;
        } else {
            return 31;
        }
    }

    /**
     * @return The maximum number of bytes in an identifier.
     * @see #maxReportedIdentifierLengthBytes()
     */
    public int maxIdentifierLengthBytes() {
        if (isVersionEqualOrAbove(4, 0)) {
            // Technically this is configurable
            return 4 * 63;
        } else {
            // Firebird 3 and below
            return 31;
        }
    }

    /**
     * @return The maximum number of bytes reported in parameter metadata for an identifier
     * @see #maxIdentifierLengthBytes()
     */
    public int maxReportedIdentifierLengthBytes() {
        if (isVersionEqualOrAbove(4, 0)) {
            // Technically this is configurable
            return 4 * 63;
        } else if (reportsByteLengthInDescriptor()) {
            // Firebird 1.5 ... 3 use unicode_fss and report 3 * identifier length, but actually store max 31 bytes(!)
            return 3 * 31;
        } else {
            return 31;
        }
    }

    /**
     * @return The character set id of system metadata
     */
    public int reportedMetadataCharacterSetId() {
        if (isVersionEqualOrAbove(4, 0)) {
            return 4; // UTF8
        } else {
            return 3; // UNICODE_FSS
        }
    }

    public boolean supportsPageSize(int pageSize) {
        switch (pageSize) {
        case PageSizeConstants.SIZE_1K:
            return !isVersionEqualOrAbove(2, 1);
        case PageSizeConstants.SIZE_2K:
            return !isVersionEqualOrAbove(2, 1);
        case PageSizeConstants.SIZE_4K:
            return true;
        case PageSizeConstants.SIZE_8K:
            return true;
        case PageSizeConstants.SIZE_16K:
            // TODO check
            return isVersionEqualOrAbove(2, 0);
        case PageSizeConstants.SIZE_32K:
            return isVersionEqualOrAbove(4, 0);
        }
        return false;
    }

    public boolean supportsWireEncryption() {
        return isVersionEqualOrAbove(3, 0);
    }

    /**
     * @return {@code true} when UDFs (User Defined Functions) - backed by a native library - are supported
     */
    public boolean supportsNativeUserDefinedFunctions() {
        return isVersionBelow(4, 0);
    }

    /**
     * @return {@code true} when PSQL functions are supported
     */
    public boolean supportsPSQLFunctions() {
        return isVersionEqualOrAbove(3, 0);
    }

    /**
     * Checks whether the Firebird version supports a plugin name.
     * <p>
     * Firebird version 2.5 and earlier are considered to support only {@code Legacy_Auth}.
     * </p>
     * <p>
     * NOTE: This method only checks if the specified plugin was shipped with a Firebird version, it does not
     * check whether the plugin is enabled, nor if additional plugins are installed.
     * </p>
     *
     * @param pluginName Authentication plugin name (case-sensitive)
     * @return {@code true} if supported, {@code false} otherwise.
     */
    public boolean supportsAuthenticationPlugin(String pluginName) {
        switch (pluginName) {
        case LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME:
            return true;
        case SrpAuthenticationPluginSpi.SRP_AUTH_NAME:
            return isVersionEqualOrAbove(3, 0);
        case Srp224AuthenticationPluginSpi.SRP_224_AUTH_NAME:
        case Srp256AuthenticationPluginSpi.SRP_256_AUTH_NAME:
        case Srp384AuthenticationPluginSpi.SRP_384_AUTH_NAME:
        case Srp512AuthenticationPluginSpi.SRP_512_AUTH_NAME:
            return isVersionEqualOrAbove(3, 0, 4);
        default:
            return false;
        }
    }

    /**
     * @return {@code true} when {@code RDB$RECORD_VERSION} pseudo column is supported
     */
    public boolean supportsRecordVersionPseudoColumn() {
        return isVersionEqualOrAbove(3, 0);
    }

    /**
     * The number of system tables (including monitoring tables).
     *
     * @return Number of system tables, or {@code -1} if the Firebird version is not known/supported.
     */
    public int getSystemTableCount() {
        final int databaseMajorVersion = serverVersion.getMajorVersion();
        final int databaseMinorVersion = serverVersion.getMinorVersion();
        if (databaseMajorVersion < 2) {
            return 32;
        } else if (databaseMajorVersion == 2 && databaseMinorVersion == 0) {
            return 33;
        } else if (databaseMajorVersion == 2 && databaseMinorVersion == 1) {
            return 40;
        } else if (databaseMajorVersion == 2 && databaseMinorVersion == 5) {
            return 42;
        } else if (databaseMajorVersion == 3 && databaseMinorVersion == 0) {
            return 50;
        } else if (databaseMajorVersion == 4 && databaseMinorVersion == 0) {
            return 50;
        } else {
            return -1;
        }
    }

    /**
     * @return {@code true} when this Firebird version supports case sensitive user names.
     */
    public boolean supportsCaseSensitiveUserNames() {
        return isVersionEqualOrAbove(3, 0);
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
     * @param attachment
     *         Low level attachment object
     * @return FirebirdVersionSupport instance
     */
    public static FirebirdSupportInfo supportInfoFor(FbAttachment attachment) {
        return supportInfoFor(attachment.getServerVersion());
    }

    /**
     * @param connection
     *         A database connection (NOTE: {@link java.sql.Connection} is used, but it must be or unwrap to a
     *         {@link org.firebirdsql.jdbc.FirebirdConnection}.
     * @return FirebirdVersionSupport instance
     * @throws java.lang.IllegalArgumentException
     *         When the provided connection is not an instance of or wrapper for
     *         {@link org.firebirdsql.jdbc.FirebirdConnection}
     * @throws java.lang.IllegalStateException
     *         When an SQLException occurs unwrapping the connection, or creating
     *         the {@link org.firebirdsql.util.FirebirdSupportInfo} instance
     */
    public static FirebirdSupportInfo supportInfoFor(java.sql.Connection connection) {
        try {
            if (connection.isWrapperFor(FirebirdConnection.class)) {
                return supportInfoFor(connection.unwrap(FirebirdConnection.class).getFbDatabase());
            } else {
                throw new IllegalArgumentException(
                        "connection needs to be (or unwrap to) an org.firebirdsql.jdbc.FBConnection");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}

// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.util;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.OdsVersion;
import org.firebirdsql.gds.ng.wire.auth.legacy.LegacyAuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp224AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp256AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp384AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.Srp512AuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.SrpAuthenticationPluginSpi;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.management.PageSizeConstants;

import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

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
 * the <a href="https://github.com/FirebirdSQL/jaybird/issues/">Jaybird tracker</a>.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@SuppressWarnings("unused")
public final class FirebirdSupportInfo {

    private final GDSServerVersion serverVersion;

    private FirebirdSupportInfo(GDSServerVersion serverVersion) {
        if (requireNonNull(serverVersion, "serverVersion").equals(GDSServerVersion.INVALID_VERSION)) {
            throw new IllegalArgumentException("serverVersion is an invalid version (GDSServerVersion.INVALID_VERSION)");
        }
        this.serverVersion = serverVersion;
    }

    /**
     * Check if the <em>major</em> of this version is equal to or larger than the specified version.
     *
     * @param majorVersion
     *         Major version
     * @return {@code true} when current major is equal to or larger than required
     */
    public boolean isVersionEqualOrAbove(int majorVersion) {
        return serverVersion.isEqualOrAbove(majorVersion);
    }

    /**
     * Check if the <em>major.minor</em> of this version is equal to or larger than the specified version.
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
     * Check if the <em>major.minor</em> of this version is below the specified version.
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
     * Check if the <em>major</em> of this version is below the specified version.
     * <p>
     * Equivalent to {@code !isVersionEqualOrAbove(majorVersion)}.
     * </p>
     *
     * @param majorVersion
     *         Major version
     * @return {@code true} when current major is smaller than the specified major
     */
    public boolean isVersionBelow(int majorVersion) {
        return !isVersionEqualOrAbove(majorVersion);
    }

    /**
     * Checks if BIGINT is supported.
     * <p>
     * Low level this feature was added in Interbase 6.0 / Firebird 1.0, but it was never surfaced in DDL
     * </p>
     *
     * @return {@code true} when the data type BIGINT is supported
     */
    public boolean supportsBigint() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return {@code true} when the data type BOOLEAN is supported
     */
    public boolean supportsBoolean() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} when the data type DECFLOAT is supported
     */
    public boolean supportsDecfloat() {
        return isVersionEqualOrAbove(4);
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
        } else if (precision <= 38) {
            // NOTE: Can result in problems for Firebird 4.0.0.1603 or earlier
            return isVersionEqualOrAbove(4);
        }
        return false;
    }

    /**
     * @return The maximum decimal and numeric precision
     */
    public int maxDecimalPrecision() {
        if (isVersionEqualOrAbove(4)) {
            return 38;
        }
        return 18;
    }

    /**
     * @return {@code true} when the data type INT128 is supported
     */
    public boolean supportsInt128() {
        // NOTE: Can result in problems for Firebird 4.0.0.2075 or earlier
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when the COMMENT statement is supported
     */
    public boolean supportsComment() {
        return isVersionEqualOrAbove(2);
    }

    /**
     * @return {@code true} when RDB$GET_CONTEXT and RDB$SET_CONTEXT are supported
     */
    public boolean supportsGetSetContext() {
        return isVersionEqualOrAbove(2);
    }

    /**
     * @return {@code true} when CASE (simple or searched) is supported
     */
    public boolean supportsCase() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return {@code true} when the blob character set is reported in the scale of the field descriptor
     */
    public boolean reportsBlobCharSetInDescriptor() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * @return {@code true} when the length of the field descriptor reports the byte length (max byte per char *
     * char length)
     */
    public boolean reportsByteLengthInDescriptor() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * TODO: Add methods for other RETURNING types?
     *
     * @return {@code true} when INSERT ... RETURNING ... is supported
     */
    public boolean supportsInsertReturning() {
        return isVersionEqualOrAbove(2);
    }

    /**
     * @return {@code true} when UPDATE ... RETURNING ... is supported
     */
    public boolean supportsUpdateReturning() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * @return {@code true} when {@code RETURNING *} and {@code RETURNING ref.*} is supported.
     */
    public boolean supportsReturningAll() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when {@code RETURNING} supports multiple rows, {@code false} only singleton results
     */
    public boolean supportsMultiRowReturning() {
        return isVersionEqualOrAbove(5);
    }

    /**
     * @return {@code true} when the server knows the UTF8 character set (NOTE: For firebird 1.5 it is an alias for
     * UNICODE_FSS)
     */
    public boolean supportsUtf8() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return {@code true} when SAVEPOINT is supported
     */
    public boolean supportsSavepoint() {
        return isVersionEqualOrAbove(1, 5);
    }

    /**
     * @return {@code true} when EXECUTE BLOCK is supported
     */
    public boolean supportsExecuteBlock() {
        return isVersionEqualOrAbove(2);
    }

    /**
     * @return {@code true} when CREATE/ALTER/DROP USER is supported
     */
    public boolean supportsSqlUserManagement() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} when fb_cancel_operation is supported
     */
    public boolean supportsCancelOperation() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} when field descriptors contain table alias information
     */
    public boolean supportsTableAlias() {
        return isVersionEqualOrAbove(2);
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
        return isVersionBelow(3);
    }

    /**
     * Checks support for protocol versions. The check is limited to those protocol versions supported by Jaybird
     * (10-16 and 18-19 at this time, although v14 is only implemented as part of v15).
     *
     * @param protocolVersion
     *         Protocol version number
     * @return {@code true} when the database supports the specified protocol
     */
    public boolean supportsProtocol(int protocolVersion) {
        return switch (protocolVersion) {
            case 10 -> true;
            case 11 -> isVersionEqualOrAbove(2, 1);
            case 12 -> isVersionEqualOrAbove(2, 5);
            case 13 -> isVersionEqualOrAbove(3);
            // Jaybird has only implemented protocol version 14 as part of version 15
            case 14, 15 -> isVersionEqualOrAbove(3, 0, 2);
            case 16 -> isVersionEqualOrAbove(4);
            case 18 -> isVersionEqualOrAbove(5);
            case 19 -> isVersionEqualOrAbove(5, 0, 3);
            default -> false;
        };
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
        return isVersionEqualOrAbove(3);
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
     * @return {@code true} when blobs are fully searchable (e.g. using `LIKE`).
     */
    public boolean supportsFullSearchableBlobs() {
        return isVersionEqualOrAbove(2, 1);
    }

    /**
     * @return {@code true} when identity columns are supported.
     */
    public boolean supportsIdentityColumns() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return The maximum number of characters in an identifier.
     */
    public int maxIdentifierLengthCharacters() {
        if (isVersionEqualOrAbove(4)) {
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
        if (isVersionEqualOrAbove(4)) {
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
        if (isVersionEqualOrAbove(4)) {
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
        if (isVersionEqualOrAbove(4)) {
            return 4; // UTF8
        } else {
            return 3; // UNICODE_FSS
        }
    }

    public boolean supportsPageSize(int pageSize) {
        return switch (pageSize) {
            case PageSizeConstants.SIZE_1K, PageSizeConstants.SIZE_2K -> !isVersionEqualOrAbove(2, 1);
            case PageSizeConstants.SIZE_4K, PageSizeConstants.SIZE_8K, PageSizeConstants.SIZE_16K -> true;
            case PageSizeConstants.SIZE_32K -> isVersionEqualOrAbove(4);
            default -> false;
        };
    }

    public boolean supportsWireEncryption() {
        return isVersionEqualOrAbove(3);
    }

    public boolean supportsWireCryptArc4() {
        return isVersionEqualOrAbove(3);
    }

    public boolean supportsWireCryptChaCha() {
        return isVersionEqualOrAbove(4);
    }

    public boolean supportsWireCryptChaCha64() {
        return isVersionEqualOrAbove(4, 0, 1);
    }

    /**
     * @return {@code true} when zlib wire compression is supported
     */
    public boolean supportsWireCompression() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} when UDFs (User Defined Functions) - backed by a native library - are supported
     */
    public boolean supportsNativeUserDefinedFunctions() {
        return isVersionBelow(4);
    }

    /**
     * @return {@code true} when PSQL functions are supported
     */
    public boolean supportsPsqlFunctions() {
        return isVersionEqualOrAbove(3);
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
        return switch (pluginName) {
            case LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME -> true;
            case SrpAuthenticationPluginSpi.SRP_AUTH_NAME -> isVersionEqualOrAbove(3);
            case Srp224AuthenticationPluginSpi.SRP_224_AUTH_NAME, Srp256AuthenticationPluginSpi.SRP_256_AUTH_NAME,
                 Srp384AuthenticationPluginSpi.SRP_384_AUTH_NAME, Srp512AuthenticationPluginSpi.SRP_512_AUTH_NAME ->
                    isVersionEqualOrAbove(3, 0, 4);
            default -> false;
        };
    }

    /**
     * @return {@code true} when {@code RDB$RECORD_VERSION} pseudo column is supported
     */
    public boolean supportsRecordVersionPseudoColumn() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * The number of system tables (including monitoring tables).
     *
     * @return Number of system tables, or {@code -1} if the Firebird version is not known/supported.
     */
    public int getSystemTableCount() {
        return switch (serverVersion.getMajorVersion()) {
            case 0, 1 -> 32;
            case 2 -> switch (serverVersion.getMinorVersion()) {
                case 0 -> 33;
                case 1 -> 40;
                case 5 -> 42;
                default -> -1;
            };
            case 3 -> 50;
            case 4 -> 54;
            case 5 -> 56;
            // Intentionally not merged with case 5 as it is likely to change during Firebird 6 development
            case 6 -> 56;
            default -> -1;
        };
    }

    /**
     * @return {@code true} when this Firebird version supports case-sensitive usernames.
     */
    public boolean supportsCaseSensitiveUserNames() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} when this Firebird version supports explained (detailed) execution plans.
     */
    public boolean supportsExplainedExecutionPlan() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} when this Firebird version supports {@code TIME(STAMP) WITH TIME ZONE}
     */
    public boolean supportsTimeZones() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports packages.
     */
    public boolean supportsPackages() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} when this Firebird version supports FLOAT(p) with binary precision.
     */
    public boolean supportsFloatBinaryPrecision() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports statement timeouts.
     */
    public boolean supportsStatementTimeouts() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports statement unprepare ({@code DSQL_unprepare})
     */
    public boolean supportsStatementUnprepare() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} when this Firebird version supports NBackup backup with GUID
     */
    public boolean supportsNBackupWithGuid() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports NBackup in-place restore
     */
    public boolean supportsNBackupInPlaceRestore() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports NBackup fixup
     */
    public boolean supportsNBackupFixup() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports NBackup preserve sequence
     */
    public boolean supportsNBackupPreserveSequence() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports NBackup clean history
     */
    public boolean supportsNBackupCleanHistory() {
        return isVersionEqualOrAbove(4, 0, 3);
    }

    /**
     * @return {@code true} when this Firebird version supports scrollable cursors. (NOTE: this does not mean
     * the connection supports it, as that depends on the actual protocol (i.e. PURE_JAVA or derivative))
     */
    public boolean supportsScrollableCursors() {
        return isVersionEqualOrAbove(5);
    }

    /**
     * @return {@code true} when this Firebird version supports server-side batch updates. (NOTE: this does not mean
     * the connection supports it, as that depends on the actual protocol (i.e. PURE_JAVA or derivative))
     */
    public boolean supportsServerBatch() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} when this Firebird version supports custom security databases
     */
    public boolean supportsCustomSecurityDb() {
        return isVersionEqualOrAbove(3);
    }

    public boolean supportsWnet() {
        // NOTE: There is probably a lower boundary as well (2.0?), but not checking that
        return isVersionBelow(5);
    }

    /**
     * @return {@code true} when this Firebird version supports statement texts longer than 64KB
     */
    public boolean supportsStatementTextLongerThan64K() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} when this Firebird version supports parallel workers
     */
    public boolean supportsParallelWorkers() {
        return isVersionEqualOrAbove(5);
    }

    /**
     * @return {@code true} when this Firebird version has the {@code RDB$CONFIG} table
     */
    @SuppressWarnings("java:S100")
    public boolean supportsRDB$CONFIG() {
        return isVersionEqualOrAbove(4);
    }

    /**
     * @return {@code true} if the gfix/service repair option upgrade ODS is supported
     */
    public boolean supportsUpgradeOds() {
        return isVersionEqualOrAbove(5);
    }

    /**
     * @return {@code true} if the gfix/service repair option icu (fix ICU) is supported
     */
    public boolean supportsFixIcu() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} if PSQL parameterized exceptions are supported ({@code exception ... using (...)})
     */
    public boolean supportsParameterizedExceptions() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} if database triggers are supported ({@code ON { CONNECT | DISCONNECT | TRANSACTION {START | COMMIT | ROLLBACK } }})
     */
    public boolean supportsDatabaseTriggers() {
        return isVersionEqualOrAbove(2, 5);
    }

    /**
     * @return {@code true} if partial indices are supported
     */
    public boolean supportsPartialIndices() {
        return isVersionEqualOrAbove(5);
    }

    /**
     * @return {@code true} if the nameless service manager is supported
     */
    public boolean supportsNamelessServiceManager() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} if server supports metadata privileges like {@code CREATE DATABASE}
     */
    public boolean supportsMetadataPrivileges() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} if server supports legacy multi-file databases
     */
    public boolean supportsLegacyMultiFileDatabase() {
        return isVersionBelow(6);
    }

    /**
     * @return the ODS version used when creating a database or after using the gfix/service repair upgrade database
     * option
     */
    public OdsVersion getDefaultOdsVersion() {
        return switch (serverVersion.getMajorVersion()) {
            case 0, 1 -> OdsVersion.of(10, 0);
            case 2 -> switch (serverVersion.getMinorVersion()) {
                case 0 -> OdsVersion.of(11, 0);
                case 1 -> OdsVersion.of(11, 1);
                case 5 -> OdsVersion.of(11, 2);
                default -> throw new IllegalArgumentException("Unsupported version: " + serverVersion);
            };
            case 3 -> OdsVersion.of(12, 0);
            case 4 -> OdsVersion.of(13, 0);
            case 5 -> OdsVersion.of(13, 1);
            case 6 -> OdsVersion.of(14, 0);
            default -> throw new IllegalArgumentException("Unsupported version: " + serverVersion);
        };
    }

    /**
     * Reports if the specified ODS version is supported.
     *
     * @param major
     *         ODS major
     * @param minor
     *         ODS minor
     * @return {@code true} if the ODS is supported, {@code false} otherwise
     * @see #supportsOds(OdsVersion)
     */
    public boolean supportsOds(int major, int minor) {
        return switch (major) {
            case 10 -> isVersionBelow(3);
            case 11 -> switch (minor) {
                case 0 -> isVersionEqualOrAbove(2) && isVersionBelow(3);
                case 1 -> isVersionEqualOrAbove(2, 1) && isVersionBelow(3);
                case 2 -> isVersionEqualOrAbove(2, 5) && isVersionBelow(3);
                default -> false;
            };
            case 12 -> isVersionEqualOrAbove(3) && isVersionBelow(4);
            case 13 -> switch (minor) {
                case 0 -> isVersionEqualOrAbove(4) && isVersionBelow(6);
                case 1 -> isVersionEqualOrAbove(5) && isVersionBelow(6);
                default -> false;
            };
            case 14 -> isVersionEqualOrAbove(6) && isVersionBelow(7);
            default -> false;
        };
    }

    /**
     * Reports if the specified ODS version is supported.
     *
     * @param odsVersion
     *         ODS version
     * @return {@code true} if the ODS is supported, {@code false} otherwise
     * @see #supportsOds(int, int)
     */
    public boolean supportsOds(OdsVersion odsVersion) {
        return supportsOds(odsVersion.major(), odsVersion.minor());
    }

    /**
     * Reports if inline blobs are supported.
     *
     * @return {@code true} if inline blobs are supported, {@code false} otherwise
     */
    public boolean supportsInlineBlobs() {
        return isVersionEqualOrAbove(5, 0, 3);
    }

    /**
     * @return {@code true} when this Firebird version is considered a supported version
     */
    public boolean isSupportedVersion() {
        return isVersionEqualOrAbove(3);
    }

    /**
     * @return {@code true} if the default ODS of this Firebird version has column {@code RDB$PROCEDURE_TYPE}
     */
    public boolean hasProcedureTypeColumn() {
        return isVersionEqualOrAbove(2, 1);
    }

    public boolean isWindows() {
        return serverVersion.getPlatform().equals("WI");
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
     *         {@link org.firebirdsql.jdbc.FirebirdConnection}).
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

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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Factory to create appropriate {@code GeneratedKeysSupport} based on the configuration, Firebird version and parser
 * availability.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class GeneratedKeysSupportFactory {

    static final String REASON_NO_RETURNING_SUPPORT =
            "This version of Firebird does not support retrieving generated keys (support was added in Firebird 2.0)";
    static final String REASON_EXPLICITLY_DISABLED = "disabled through connection property";
    private static final String GENERATED_KEYS_ENABLED_DEFAULT = "default";
    private static final String GENERATED_KEYS_DISABLED = "disabled";
    private static final String GENERATED_KEYS_IGNORED = "ignored";

    private GeneratedKeysSupportFactory() {
        // No instances
    }

    /**
     * Create generated keys support based on the provided config and database metadata.
     * <p>
     * The supported values for {@code generatedKeysEnabled} are
     * <dl>
     * <dt>{@code default}</dt>
     * <dd>Support all query types supported by Firebird version (if none are supported, will throw
     * {@code SQLFeatureNotSupportedException} for attempts to use generated keys facility, except
     * {@code Statement.RETURN_NO_GENERATED_KEYS})</dd>
     * <dt>{@code disabled}</dt>
     * <dd>Explicitly disabled support, will throw {@code SQLFeatureNotSupportedException} for attempts to use
     * generated keys facility, except {@code Statement.RETURN_NO_GENERATED_KEYS})</dd>
     * <dt>{@code ignored}</dt>
     * <dd>Ignore generated keys support. Any attempt to use generated keys will be ignored and executed as a
     * normal non-generated keys statement.</dd>
     * <dt>Comma-separated list of query types</dt>
     * <dd>Only enable support for the specified query types. All other query types are handled as non-generated
     * keys statement</dd>
     * </dl>
     * </p>
     * <p>
     * The last option is a comma-separated list of query types to enable for support. Primary use-case is to enable
     * only {@code insert} to avoid the singleton limitation for the other DML types. Other example
     * {@code insert,update} enables only insert and update. Supported query type values (case insensitive) are:
     * <ul>
     * <li>{@code insert}</li>
     * <li>{@code update}</li>
     * <li>{@code delete}</li>
     * <li>{@code update_or_insert}</li>
     * <li>{@code merge}</li>
     * </ul>
     * </p>
     * <p>
     * Supplying a list of query types will only enable the query types that are specified in the list (if supported by
     * the connected Firebird version). Unknown query types are ignored. If all specified query types are unknown or not
     * supported, then this behaves as {@code ignored}. NOTE: For Firebird 1.5 and earlier (unsupported) this will -
     * same as for {@code default} - behave as {@code disabled} and attempts to use generated keys (except
     * {@code Statement.RETURN_NO_GENERATED_KEYS}) will always throw a {@code SQLFeatureNotSupportedException}.
     * </p>
     *
     * @param generatedKeysEnabled
     *         Generated keys enabled value (case insensitive: {@code disabled}, {@code ignored},
     *         {@code default} / {@code null} / empty string, or a list of query types to enable
     * @param fbDatabaseMetaData
     *         Database metadata object
     * @return Appropriate generated keys support determined by config value, and Firebird feature support.
     * @throws SQLException
     *         if a database access error occurs while determining feature support
     */
    static GeneratedKeysSupport createFor(String generatedKeysEnabled, FirebirdDatabaseMetaData fbDatabaseMetaData)
            throws SQLException {
        String normalizedConfigValue = generatedKeysEnabled != null && !generatedKeysEnabled.isEmpty()
                ? generatedKeysEnabled.toLowerCase(Locale.ROOT)
                : GENERATED_KEYS_ENABLED_DEFAULT;
        if (GENERATED_KEYS_DISABLED.equals(normalizedConfigValue)) {
            return DisabledGeneratedKeysSupport.EXPLICITLY_DISABLED;
        } else if (GENERATED_KEYS_IGNORED.equals(normalizedConfigValue)) {
            return IgnoredGeneratedKeysSupport.INSTANCE;
        } else {
            Set<GeneratedKeysSupport.QueryType> returningSupport =
                    GeneratedKeysSupport.QueryType.returningSupportForVersion(
                            fbDatabaseMetaData.getDatabaseMajorVersion(),
                            fbDatabaseMetaData.getDatabaseMinorVersion());
            if (returningSupport.isEmpty()) {
                return new DisabledGeneratedKeysSupport(REASON_NO_RETURNING_SUPPORT);
            } else if (GENERATED_KEYS_ENABLED_DEFAULT.equals(normalizedConfigValue)) {
                return new DefaultGeneratedKeysSupport(fbDatabaseMetaData, returningSupport);
            } else {
                Set<GeneratedKeysSupport.QueryType> enabledTypes =
                        getEnabledTypes(normalizedConfigValue, returningSupport);
                if (enabledTypes.isEmpty()) {
                    return IgnoredGeneratedKeysSupport.INSTANCE;
                }
                return new DefaultGeneratedKeysSupport(fbDatabaseMetaData, enabledTypes);
            }
        }
    }

    private static Set<GeneratedKeysSupport.QueryType> getEnabledTypes(String normalizedConfigValue,
            Set<GeneratedKeysSupport.QueryType> returningSupport) {
        String[] queryTypeNames = normalizedConfigValue.split(",");
        EnumSet<GeneratedKeysSupport.QueryType> enabledTypes = EnumSet.noneOf(GeneratedKeysSupport.QueryType.class);
        // Given the limited number of values of queryType, double iteration is acceptable
        for (String queryTypeName : queryTypeNames) {
            for (GeneratedKeysSupport.QueryType queryType : returningSupport) {
                if (queryType.matches(queryTypeName.trim())) {
                    enabledTypes.add(queryType);
                }
            }
        }
        return enabledTypes;
    }

    /**
     * The default generated keys support, either based on the capabilities of Firebird or on a reduced set based on
     * configuration.
     *
     * @since 4.0
     */
    private static final class DefaultGeneratedKeysSupport implements GeneratedKeysSupport {

        private final FirebirdDatabaseMetaData fbDatabaseMetaData;
        private final Set<QueryType> supportedQueryTypes;

        private DefaultGeneratedKeysSupport(FirebirdDatabaseMetaData fbDatabaseMetaData,
                Set<QueryType> supportedQueryTypes) {
            assert !supportedQueryTypes.contains(QueryType.UNSUPPORTED)
                    : "supportedQueryTypes should not contain UNSUPPORTED";
            assert !supportedQueryTypes.isEmpty() : "At least one query type should be present";
            this.fbDatabaseMetaData = fbDatabaseMetaData;
            this.supportedQueryTypes = supportedQueryTypes;
        }

        @Override
        public Query buildQuery(String sql, int autoGeneratedKeys) throws SQLException {
            switch (autoGeneratedKeys) {
            case Statement.NO_GENERATED_KEYS:
                return GeneratedKeysQueryBuilder
                        .create(sql, supportedQueryTypes)
                        .forNoGeneratedKeysOption();
            case Statement.RETURN_GENERATED_KEYS:
                return GeneratedKeysQueryBuilder
                        .create(sql, supportedQueryTypes)
                        .forReturnGeneratedKeysOption(fbDatabaseMetaData);
            default:
                throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidGeneratedKeysOption)
                        .toSQLException();
            }
        }

        @Override
        public Query buildQuery(String sql, int[] columnIndexes) throws SQLException {
            return GeneratedKeysQueryBuilder
                    .create(sql, supportedQueryTypes)
                    .forColumnsByIndex(columnIndexes, fbDatabaseMetaData);
        }

        @Override
        public Query buildQuery(String sql, String[] columnNames) throws SQLException {
            return GeneratedKeysQueryBuilder
                    .create(sql, supportedQueryTypes)
                    .forColumnsByName(columnNames);
        }

        @Override
        public Set<QueryType> supportedQueryTypes() {
            return Collections.unmodifiableSet(supportedQueryTypes);
        }

        @Override
        public boolean supportsGetGeneratedKeys() {
            return true;
        }
    }

    /**
     * Disabled generated keys support.
     * <p>
     * Throws a {@code SQLFeatureNotSupportedException} with a configured reason for most options except
     * using {@code autoGeneratedKeys} with value {@link Statement#NO_GENERATED_KEYS}.
     * </p>
     *
     * @since 4.0
     */
    private static final class DisabledGeneratedKeysSupport implements GeneratedKeysSupport {
        private static final DisabledGeneratedKeysSupport EXPLICITLY_DISABLED =
                new DisabledGeneratedKeysSupport(REASON_EXPLICITLY_DISABLED);

        private final String reasonDisabled;

        public DisabledGeneratedKeysSupport(String reasonDisabled) {
            this.reasonDisabled = reasonDisabled;
        }

        @Override
        public Query buildQuery(String sql, int autoGeneratedKeys) throws SQLException {
            switch (autoGeneratedKeys) {
            case Statement.NO_GENERATED_KEYS:
                return new Query(false, sql);
            case Statement.RETURN_GENERATED_KEYS:
                throw disabled();
            default:
                throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidGeneratedKeysOption)
                        .toSQLException();
            }
        }

        @Override
        public Query buildQuery(String sql, int[] columnIndexes) throws SQLException {
            throw disabled();
        }

        @Override
        public Query buildQuery(String sql, String[] columnNames) throws SQLException {
            throw disabled();
        }

        @Override
        public Set<QueryType> supportedQueryTypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean supportsGetGeneratedKeys() {
            return false;
        }

        private SQLException disabled() {
            return FbExceptionBuilder.forException(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable)
                    .messageParameter(reasonDisabled)
                    .toSQLException();
        }
    }

    /**
     * Ignores generated keys support and processes queries without producing a generated keys result set.
     * <p>
     * This ignores requests for generated keys handling and acts as if no query types support generated keys at all.
     * This allows executing all queries normally without support for {@code getGeneratedKeys}. This option will
     * yield errors when executing statements with a {@code RETURNING} clause with {@code executeUpdate} or
     * {@code executeLargeUpdate}.
     * </p>
     *
     * @since 4.0
     */
    private static final class IgnoredGeneratedKeysSupport implements GeneratedKeysSupport {

        private static final IgnoredGeneratedKeysSupport INSTANCE = new IgnoredGeneratedKeysSupport();

        @Override
        public Query buildQuery(String sql, int autoGeneratedKeys) {
            // NOTE: Violates JDBC spec: allow values other than NO_GENERATED_KEYS and RETURN_GENERATED_KEYS for ignored
            return createNonGeneratedKeysQuery(sql);
        }

        @Override
        public Query buildQuery(String sql, int[] columnIndexes) {
            return createNonGeneratedKeysQuery(sql);
        }

        @Override
        public Query buildQuery(String sql, String[] columnNames) {
            return createNonGeneratedKeysQuery(sql);
        }

        @Override
        public Set<QueryType> supportedQueryTypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean supportsGetGeneratedKeys() {
            return false;
        }

        private Query createNonGeneratedKeysQuery(String sql) {
            return new Query(false, sql);
        }
    }
}

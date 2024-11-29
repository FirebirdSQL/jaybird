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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jdbc.InternalTransactionCoordinator.MetaDataTransactionCoordinator;

import java.sql.ClientInfoStatus;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_clientInfoInvalidPropertyName;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_clientInfoSystemContextReadOnly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Implementation for retrieving client info properties for a connection.
 *
 * @author Mark Rotteveel
 * @since 6
 */
final class ClientInfoProvider {

    private static final String USER_SESSION = "USER_SESSION";
    private static final String USER_TRANSACTION = "USER_TRANSACTION";
    private static final String SYSTEM = "SYSTEM";
    private static final Set<String> SUPPORTED_CONTEXTS = Set.of(USER_SESSION, USER_TRANSACTION, SYSTEM);
    private static final String APPLICATION_NAME = "ApplicationName";
    private static final ClientInfoProperty APPLICATION_NAME_PROP =
            new ClientInfoProperty(APPLICATION_NAME, USER_SESSION);
    private static final ClientInfoProperty APPLICATION_NAME_FALLBACK_PROP =
            new ClientInfoProperty("CLIENT_PROCESS", SYSTEM);
    private static final Set<ClientInfoProperty> DEFAULT_CLIENT_INFO_PROPERTIES = Set.of(
            APPLICATION_NAME_PROP,
            new ClientInfoProperty("ClientUser", USER_SESSION),
            new ClientInfoProperty("ClientHostname", USER_SESSION));
    private static final Set<String> DEFAULT_CLIENT_INFO_PROPERTY_NAMES = DEFAULT_CLIENT_INFO_PROPERTIES.stream()
            .map(Object::toString)
            .collect(toUnmodifiableSet());

    private final FBConnection connection;
    // if null, use DEFAULT_CLIENT_INFO_PROPERTIES
    private Set<ClientInfoProperty> knownProperties;
    // Statement used for setting or retrieving client info properties.
    // We don't try to close this statement, and rely on it getting closed by connection close
    private Statement statement;

    ClientInfoProvider(FBConnection connection) throws SQLException {
        connection.checkValidity();
        if (!supportInfoFor(connection).supportsGetSetContext()) {
            throw new FBDriverNotCapableException(
                    "Required functionality (RDB$SET_CONTEXT()) only available in Firebird 2.0 or higher");
        }
        this.connection = connection;
    }

    private Statement getStatement() throws SQLException {
        Statement statement = this.statement;
        if (statement != null && !statement.isClosed()) return statement;
        var metaDataTransactionCoordinator = new MetaDataTransactionCoordinator(connection.txCoordinator);
        // Create statement which piggybacks on active transaction, starts one when needed, but does not commit (not
        // even in auto-commit)
        var rsBehavior = ResultSetBehavior.of(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        return this.statement = new FBStatement(connection, rsBehavior, metaDataTransactionCoordinator);
    }

    /**
     * Reset the currently known properties of this connection back to the default
     * <p>
     * This only clears the list held by this instance, it does not remove or reset values on the server.
     * </p>
     */
    void resetKnownProperties() {
        knownProperties = null;
    }

    /**
     * @return gets the known properties of this instance (it may be unmodifiable by returning the global default)
     */
    // default access for tests
    Set<ClientInfoProperty> getKnownProperties() {
        return knownProperties != null ? knownProperties : DEFAULT_CLIENT_INFO_PROPERTIES;
    }

    /**
     * @return gets the known properties of this instance (guaranteed to be a modifiable set for this instance)
     */
    private Set<ClientInfoProperty> getOrCreateKnownProperties() {
        Set<ClientInfoProperty> knownProperties = this.knownProperties;
        if (knownProperties != null) return knownProperties;
        return this.knownProperties = new HashSet<>(DEFAULT_CLIENT_INFO_PROPERTIES);
    }

    /**
     * Registers {@code property} as a known property for this instance.
     *
     * @param property
     *         client info property to register
     */
    // default access for tests
    void registerKnownProperty(ClientInfoProperty property) {
        if (DEFAULT_CLIENT_INFO_PROPERTIES.contains(property)) return;
        getOrCreateKnownProperties().add(property);
    }

    /**
     * Registers {@code properties} as known properties for this instance.
     *
     * @param properties
     *         client info properties to register
     */
    // default access for tests
    void registerKnownProperties(Collection<ClientInfoProperty> properties) {
        if (DEFAULT_CLIENT_INFO_PROPERTIES.containsAll(properties)) return;
        getOrCreateKnownProperties().addAll(properties);
    }

    /**
     * Default client info property names for this connection.
     *
     * @return names of the client info properties
     * @see #getDefaultClientInfoProperties()
     */
    Collection<String> getDefaultClientInfoPropertyNames() {
        return DEFAULT_CLIENT_INFO_PROPERTY_NAMES;
    }

    /**
     * Default client info properties for this connection.
     * <p>
     * This method only reports the client info properties which are supported by default, any additional properties
     * discovered during the lifetime of the connection will not be reported.
     * </p>
     * <p>
     * The current implementation will only report the properties specified in JDBC ({@code ApplicationName},
     * {@code ClientUser} and {@code ClientHostname}).
     * </p>
     *
     * @return client info properties
     * @see #getDefaultClientInfoPropertyNames()
     */
    Collection<ClientInfoProperty> getDefaultClientInfoProperties() {
        return DEFAULT_CLIENT_INFO_PROPERTIES;
    }

    /**
     * Implementation of {@link FBConnection#getClientInfo(String)}.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public String getClientInfo(String name) throws SQLException {
        ClientInfoProperty property;
        try {
            property = ClientInfoProperty.parse(name);
        } catch (RuntimeException e) {
            throw FbExceptionBuilder.forNonTransientException(jb_clientInfoInvalidPropertyName)
                    .messageParameter(name)
                    .cause(e)
                    .toSQLException();
        }
        // Waste of resources to query USER_TRANSACTION in auto-commit mode
        if (USER_TRANSACTION.equals(property.context) && connection.getAutoCommit()) return null;

        QuoteStrategy quoteStrategy = connection.getQuoteStrategy();
        var sb = new StringBuilder("select ");
        renderGetValue(sb, property, quoteStrategy);
        sb.append(" from RDB$DATABASE");
        try (var rs = getStatement().executeQuery(sb.toString())) {
            if (rs.next()) {
                registerKnownProperty(property);
                return rs.getString(1);
            }
            return null;
        } catch (SQLException e) {
            if (e.getErrorCode() == ISCConstants.isc_ctx_var_not_found) {
                // queried a non-existent SYSTEM variable
                return null;
            }
            throw e;
        }
    }

    private void renderGetValue(StringBuilder sb, ClientInfoProperty property, QuoteStrategy quoteStrategy) {
        // CLIENT_PROCESS@SYSTEM was introduced in Firebird 2.5.3, so don't fall back for earlier versions
        if (APPLICATION_NAME_PROP.equals(property) && supportInfoFor(connection).isVersionEqualOrAbove(2, 5, 3)) {
            sb.append("coalesce(");
            property.appendAsGetContext(sb, quoteStrategy).append(',');
            APPLICATION_NAME_FALLBACK_PROP.appendAsGetContext(sb, quoteStrategy).append(')');
        } else {
            property.appendAsGetContext(sb, quoteStrategy);
        }
    }

    /**
     * Implementation of {@link FBConnection#getClientInfo()}.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public Properties getClientInfo() throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        QuoteStrategy quoteStrategy = connection.getQuoteStrategy();
        var sb = new StringBuilder("""
                execute block returns (
                  CONTEXT_NAME varchar(20) character set ASCII,
                  CONTEXT_VAR_NAME varchar(80) character set NONE,
                  CONTEXT_VAR_VALUE varchar(32765) character set NONE)
                as
                begin
                """);
        for (ClientInfoProperty property : getKnownProperties()) {
            // Skip retrieving USER_TRANSACTION in auto-commit mode
            if (autoCommit && USER_TRANSACTION.equals(property.context)) continue;

            sb.append("CONTEXT_NAME=");
            quoteStrategy.appendLiteral(property.context, sb).append(";\n");
            sb.append("CONTEXT_VAR_NAME=");
            quoteStrategy.appendLiteral(property.name, sb).append(";\n");
            boolean systemContext = SYSTEM.equals(property.context);
            if (systemContext) {
                // Use block for error handling (ctx_var_not_found is raised only for SYSTEM)
                sb.append("""
                        begin
                        """);
            }
            sb.append("CONTEXT_VAR_VALUE=");
            renderGetValue(sb, property, quoteStrategy);
            sb.append(";\n");
            if (systemContext) {
                // Error handling for SYSTEM (see above)
                sb.append("""
                        when gdscode ctx_var_not_found do CONTEXT_VAR_VALUE = null;
                        end
                        """);
            }
            sb.append("suspend;\n");
        }
        sb.append("end");

        try (var rs = getStatement().executeQuery(sb.toString())) {
            var properties = new Properties();
            while (rs.next()) {
                var property = new ClientInfoProperty(rs.getString("CONTEXT_VAR_NAME"), rs.getString("CONTEXT_NAME"));
                String value = rs.getString("CONTEXT_VAR_VALUE");
                if (value != null) {
                    properties.setProperty(property.toString(), value);
                }
            }
            return properties;
        }
    }

    /**
     * Implementation of {@link FBConnection#setClientInfo(String, String)}.
     *
     * @throws SQLClientInfoException
     *         for invalid property names or if {@code name} is a property in context {@code SYSTEM} (read-only)
     * @throws SQLException
     *         for database access errors
     */
    public void setClientInfo(String name, String value) throws SQLException {
        ClientInfoProperty property;
        try {
            property = ClientInfoProperty.parse(name);
        } catch (RuntimeException e) {
            SQLException forMessage = FbExceptionBuilder.forException(jb_clientInfoInvalidPropertyName)
                    .messageParameter(name)
                    .toSQLException();
            throw new SQLClientInfoException(forMessage.getMessage(), forMessage.getSQLState(),
                    forMessage.getErrorCode(),
                    Map.of(requireNonNullElse(name, "<null>"), ClientInfoStatus.REASON_UNKNOWN), e);
        }
        if (SYSTEM.equals(property.context)) {
            SQLException forMessage = FbExceptionBuilder.forException(jb_clientInfoSystemContextReadOnly)
                    .messageParameter(name)
                    .toSQLException();
            throw new SQLClientInfoException(forMessage.getMessage(), forMessage.getSQLState(),
                    forMessage.getErrorCode(), Map.of(name, ClientInfoStatus.REASON_UNKNOWN));
        }
        // Waste of resources to set USER_TRANSACTION in auto-commit mode
        if (USER_TRANSACTION.equals(property.context) && connection.getAutoCommit()) return;

        executeSetClientInfo(Map.of(property, value));
    }

    /**
     * Implementation of {@link FBConnection#setClientInfo(Properties)}.
     *
     * @throws SQLException
     *         for database access errors
     */
    public void setClientInfo(Properties properties) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        // Include USER_SESSION, and USER_TRANSACTION if not in auto-commit
        Predicate<ClientInfoProperty> includePropertyPredicate = property ->
                USER_SESSION.equals(property.context) || !autoCommit && USER_TRANSACTION.equals(property.context);
        var propertyValues = new HashMap<ClientInfoProperty, String>();
        // Populating with null to clear properties not included in parameter properties
        getKnownProperties().stream()
                .filter(includePropertyPredicate)
                .forEach(property -> propertyValues.put(property, null));

        Predicate<ClientInfoProperty> excludePropertyPredicate = includePropertyPredicate.negate();
        for (String propertyName : properties.stringPropertyNames()) {
            var property = ClientInfoProperty.parse(propertyName);
            if (excludePropertyPredicate.test(property)) continue;

            propertyValues.put(property, properties.getProperty(propertyName));
        }

        executeSetClientInfo(propertyValues);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    private void executeSetClientInfo(Map<ClientInfoProperty, String> propertyValues) throws SQLException {
        QuoteStrategy quoteStrategy = connection.getQuoteStrategy();
        var sb = new StringBuilder("""
                execute block
                as
                begin
                """);
        propertyValues.forEach((property, value) ->
                property.appendAsSetContext(sb, quoteStrategy, value).append(";\n"));
        sb.append("end");

        getStatement().execute(sb.toString());
        registerKnownProperties(propertyValues.keySet());
    }

    /**
     * Client info property.
     *
     * @param name
     *         name of the property (without context)
     * @param context
     *         context
     * @since 6
     */
    record ClientInfoProperty(String name, String context) {

        static final Pattern PROPERTY_PATTERN =
                Pattern.compile("^(.*?)@(USER_(?:SESSION|TRANSACTION)|SYSTEM)$", Pattern.DOTALL);

        /**
         * Creates a client info property.
         *
         * @param name
         *         property name
         * @param context
         *         property context
         * @throws NullPointerException
         *         if {@code name} or {@code context} is {@code null}
         * @throws IllegalArgumentException
         *         if {@code name} ends in {@code @USER_SESSION}, {@code @USER_TRANSACTION} or {@code @SYSTEM}, or if
         *         {@code context} is not {@code USER_SESSION}, {@code USER_TRANSACTION} or {@code SYSTEM}
         */
        ClientInfoProperty {
            if (PROPERTY_PATTERN.matcher(requireNonNull(name, "name")).matches()) {
                throw new IllegalArgumentException("Name '%s' should not end in @ followed by %s"
                        .formatted(name, SUPPORTED_CONTEXTS));
            }
            if (!SUPPORTED_CONTEXTS.contains(requireNonNull(context, "context"))) {
                throw new IllegalArgumentException(
                        "Unknown context '%s', expected one of %s".formatted(context, SUPPORTED_CONTEXTS));
            }
        }

        /**
         * Parses a property name to a {@link ClientInfoProperty} instance.
         * <p>
         * If {@code name} ends in {@code @USER_SESSION}, {@code @USER_TRANSACTION} or {@code @SYSTEM}, the name is
         * parsed as {@code <property-name>@<property-context>}. For all other names, the {@code name} is taken as-is in
         * the {@code USER_SESSION} context. As a result of this rules, both {@code SomeProperty} and
         * {@code SomeProperty@USER_SESSION} result in the same property.
         * </p>
         *
         * @param name
         *         property name, cannot be {@code null}, can end in {@code @USER_SESSION}, {@code @USER_TRANSACTION} or
         *         {@code @SYSTEM} to specify the context, otherwise context {@code USER_SESSION} is used
         * @return client info property instance
         * @throws NullPointerException
         *         if name is {@code null}
         * @throws IllegalArgumentException
         *         if name contains multiple context suffixes (e.g. property@SYSTEM@USER_SESSION)
         */
        static ClientInfoProperty parse(String name) {
            Matcher matcher = PROPERTY_PATTERN.matcher(name);
            if (matcher.matches()) {
                String propertyName = matcher.group(1);
                String context = matcher.group(2);
                if (matcher.reset(propertyName).matches()) {
                    throw new IllegalArgumentException(
                            "Name '%s' should not end in multiple occurrences of @ followed by %s"
                                    .formatted(name, SUPPORTED_CONTEXTS));
                }
                return new ClientInfoProperty(propertyName, context);
            }
            return new ClientInfoProperty(name, USER_SESSION);
        }

        @Override
        public String toString() {
            if (USER_SESSION.equals(context)) {
                return name;
            }
            return name + '@' + context;
        }

        /**
         * Renders this property as an {@code RDB$GET_CONTEXT} call into {@code sb} without leading or trailing spaces.
         *
         * @param sb
         *         string builder to append to
         * @return {@code sb} for chaining calls
         */
        StringBuilder appendAsGetContext(StringBuilder sb, QuoteStrategy quoteStrategy) {
            // 25 = 16 (prefix) + 6 (4 quotes, comma and closing parenthesis) + space for three quotes to escape
            sb.ensureCapacity(sb.length() + 25 + context.length() + name.length());
            sb.append("RDB$GET_CONTEXT(");
            quoteStrategy.appendLiteral(context, sb);
            sb.append(',');
            quoteStrategy.appendLiteral(name, sb);
            return sb.append(')');
        }

        /**
         * Renders this property as an {@code RDB$SET_CONTEXT} call with {@code value} into {@code sb} without leading
         * or trailing spaces.
         *
         * @param sb
         *         string builder to append to
         * @param value
         *         value to set, use {@code null} to set SQL {@code NULL}
         * @return {@code sb} for chaining calls
         */
        StringBuilder appendAsSetContext(StringBuilder sb, QuoteStrategy quoteStrategy, String value) {
            // 30 = 16 (prefix) + 9 (6 quotes, 2 commas, and closing parenthesis) + space for five quotes to escape
            sb.ensureCapacity(
                    sb.length() + 30 + context.length() + name.length() + (value != null ? value.length() : 4));
            sb.append("RDB$SET_CONTEXT(");
            quoteStrategy.appendLiteral(context, sb);
            sb.append(',');
            quoteStrategy.appendLiteral(name, sb);
            sb.append(',');
            if (value == null) {
                sb.append("NULL");
            } else {
                quoteStrategy.appendLiteral(value, sb);
            }
            return sb.append(')');
        }

    }

}

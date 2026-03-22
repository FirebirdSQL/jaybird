// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

/**
 * SPI interface to define JDBC protocol implementations for Jaybird.
 * <p>
 * Please be aware that although this is formally an API of Jaybird, it is considered an internal API. This means that
 * it should be considered unstable, though we strive to not make changes in point releases.
 * </p>
 * <p>
 * Additional protocols can be registered by creating a service definition in
 * {@code META-INF/services/org.firebirdsql.gds.impl.GDSFactoryPlugin} <b>and</b> defining a {@code provides} entry in
 * {@code module-info.java}.
 * </p>
 */
public interface GDSFactoryPlugin {

    /**
     * @return descriptive name of the plugin
     */
    @SuppressWarnings("unused")
    String getPluginName();

    /**
     * @return primary type name of the plugin
     * @see #getTypeAliasList()
     */
    String getTypeName();

    /**
     * List of type aliases (in addition to {@link #getTypeName()}). For example, the PURE_JAVA type has alias TYPE4.
     * <p>
     * In general, we recommend not to define aliases for types, but instead only have a {@code typeName}.
     * </p>
     * <p>
     * The default implementation returns an empty list ({@code List.of()}).
     * </p>
     *
     * @return list of type aliases (empty list if there are no aliases)
     * @since 6
     */
    default List<String> getTypeAliasList() {
        return List.of();
    }

    /**
     * Class used for connection.
     * <p>
     * The class must define a one-arg constructor accepting {@link org.firebirdsql.jaybird.xca.FBManagedConnection}.
     * Currently, the Jaybird implementation also requires that the class is {@link org.firebirdsql.jdbc.FBConnection}
     * or a subclass. This may change in the future.
     * </p>
     * <p>
     * The default implementation returns {@code FBConnection.class}.
     * </p>
     *
     * @return class for connection
     */
    default Class<?> getConnectionClass() {
        return FBConnection.class;
    }

    /**
     * The default protocol prefix for this type (for example, for PURE_JAVA, it's {@code "jdbc:firebirdsql:"}).
     * <p>
     * The protocol prefix <b>must</b> be distinct from other plugins.
     * </p>
     *
     * @return default protocol name
     */
    String getDefaultProtocol();

    /**
     * List of JDBC supported protocol prefixes, including {@code defaultProtocol}. For example the PURE_JAVA type has
     * supported protocols {@code ["jdbc:firebirdsql:java:", "jdbc:firebird:java:", "jdbc:firebird:", "jdbc:firebirdsql:"]}.
     * <p>
     * In general, one protocol should suffice. An exception can be made if the default is
     * {@code "jdbc:firebirdsql:subtype:"} to also define {@code "jdbc:firebird:subtype:"} (or vice versa).
     * </p>
     * <p>
     * If a protocol is a prefix of another protocol, it should be listed after that protocol. This is not enforced,
     * but failure to do so will result in the default implementation of {@link #getDatabasePath(String)} to produce
     * the wrong value.
     * </p>
     *
     * @return list of JDBC protocol prefixes (must include the value of {@link #getDefaultProtocol()})
     * @since 6
     */
    List<String> getSupportedProtocolList();

    /**
     * @return instance of {@link FbDatabaseFactory} for this implementation
     */
    FbDatabaseFactory getDatabaseFactory();

    // TODO Path is required, but making it @NonNull results in warnings, and making it @Nullable does not fit as that
    //  will always result in an exception
    @NullUnmarked
    String getDatabasePath(@Nullable String server, @Nullable Integer port, String path) throws SQLException;

    @InternalApi
    default String getDatabasePath(DbAttachInfo dbAttachInfo) throws SQLException {
        int portNumber = dbAttachInfo.portNumber();
        return getDatabasePath(dbAttachInfo.serverName(),
                portNumber != PropertyConstants.DEFAULT_PORT ? portNumber : null, dbAttachInfo.attachObjectName());
    }

    /**
     * Extracts the database <em>path</em> from a JDBC URL.
     * <p>
     * The default implementation returns the URL without the JDBC protocol prefix. This default implementation relies
     * on order of protocols returned by {@link #getSupportedProtocolList()}.
     * </p>
     * <p>
     * Implementations are free to return what they want, as long as their connection creation can handle it
     * appropriately. However, we recommend to just return the remainder of the JDBC URL without the protocol prefix.
     * </p>
     *
     * @param jdbcUrl
     *         JDBC URL <em>without</em> connection properties
     * @return database path
     * @throws SQLException
     *         if the offered JDBC URL is not supported by this plugin
     */
    default String getDatabasePath(String jdbcUrl) throws SQLException {
        for (String protocol : getSupportedProtocolList()) {
            if (jdbcUrl.startsWith(protocol)) {
                return jdbcUrl.substring(protocol.length());
            }
        }

        throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                .messageParameter(jdbcUrl, "JDBC URL not supported by protocol: " + getTypeName())
                .toSQLException();
    }

}

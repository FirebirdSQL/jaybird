// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.util.InternalApi;

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
 * {@code META-INF/services/org.firebirdsql.gds.impl.GDSFactoryPlugin}.
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
     * List of type aliases (in addition to {@link #getTypeName()}), for example the PURE_JAVA type has alias TYPE4.
     * <p>
     * In general, we recommend not to define aliases for types, but instead only have a {@code typeName}.
     * </p>
     *
     * @return list of type aliases (empty list if there are no aliases)
     * @since 6
     */
    List<String> getTypeAliasList();

    /**
     * Class used for connection.
     * <p>
     * The class must define a one-arg constructor accepting {@link org.firebirdsql.jaybird.xca.FBManagedConnection}.
     * Currently, the Jaybird implementation also requires that the class is {@link org.firebirdsql.jdbc.FBConnection}
     * or a subclass. This may change in the future.
     * </p>
     *
     * @return class for connection
     */
    Class<?> getConnectionClass();

    /**
     * The default protocol prefix for this type (for example, for PURE_JAVA, it's {@code "jdbc:firebirdsql:"}.
     * <p>
     * The protocol prefix must be distinct from other plugins.
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
     * {@code "jdbc:firebirdsql:subtype:"} to also define {@code "jdbc:firebird:subtype"}.
     * </p>
     *
     * @return list of type aliases (must include the value of {@link #getDefaultProtocol()})
     * @since 6
     */
    List<String> getSupportedProtocolList();

    /**
     * @return instance of {@link FbDatabaseFactory} for this implementation
     */
    FbDatabaseFactory getDatabaseFactory();

    String getDatabasePath(String server, Integer port, String path) throws SQLException;

    @InternalApi
    default String getDatabasePath(DbAttachInfo dbAttachInfo) throws SQLException {
        int portNumber = dbAttachInfo.portNumber();
        return getDatabasePath(dbAttachInfo.serverName(),
                portNumber != PropertyConstants.DEFAULT_PORT ? portNumber : null, dbAttachInfo.attachObjectName());
    }

    String getDatabasePath(String jdbcUrl) throws SQLException;
}

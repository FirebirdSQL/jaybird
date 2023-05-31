/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
    String getPluginName();

    /**
     * @return primary type name of the plugin
     * @see #getTypeAliasList()
     */
    String getTypeName();

    /**
     * List of type aliases.
     * <p>
     * Implementations with aliases are encouraged to explicitly implement {@link #getTypeAliasList()} to return
     * an immutable list and override this method to use {@code return getTypeAliasList().toArray(new String[0])} or
     * similar.
     * </p>
     *
     * @return array with type aliases (empty array if there are no aliases)
     * @see #getTypeAliasList()
     * @deprecated Use {@link #getTypeAliasList()}, may be removed in Jaybird 7 or later
     */
    @Deprecated(since = "6", forRemoval = true)
    String[] getTypeAliases();

    /**
     * List of type aliases (in addition to {@link #getTypeName()}), for example the PURE_JAVA type has alias TYPE4.
     * <p>
     * In general, we recommend not to define aliases for types, but instead only have a {@code typeName}.
     * </p>
     * <p>
     * The default implementation wraps {@link #getTypeAliases()}, but implementations are encouraged to implement this
     * with an immutable list. This default implementation will be removed when {@link #getTypeAliases()} is removed.
     * </p>
     *
     * @return list of type aliases (empty list if there are no aliases)
     * @since 6
     */
    default List<String> getTypeAliasList() {
        return List.of(getTypeAliases());
    }

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
     * List of JDBC supported protocol prefixes.
     * <p>
     * Implementations with aliases are encouraged to explicitly implement {@link #getSupportedProtocolList()} to
     * return an immutable list and override this method to use {@code return getSupportedProtocolList().toArray(new String[0])}
     * or similar.
     * </p>
     *
     * @return array with supported protocol prefixes (must include the value of {@link #getDefaultProtocol()}).
     * @see #getSupportedProtocolList()
     * @deprecated Use {@link #getSupportedProtocolList()}, may be removed in Jaybird 7 or later
     */
    @Deprecated(since = "6", forRemoval = true)
    String[] getSupportedProtocols();

    /**
     * List of JDBC supported protocol prefixes, including {@code defaultProtocol}. For example the PURE_JAVA type has
     * supported protocols {@code ["jdbc:firebirdsql:java:", "jdbc:firebird:java:", "jdbc:firebird:", "jdbc:firebirdsql:"]}.
     * <p>
     * In general, one protocol should suffice. An exception can be made if the default is
     * {@code "jdbc:firebirdsql:subtype:"} to also define {@code "jdbc:firebird:subtype"}.
     * </p>
     * <p>
     * The default implementation wraps {@link #getSupportedProtocols()}, but implementations are encouraged to
     * implement this with an immutable list. This default implementation will be removed when
     * {@link #getSupportedProtocols()} is removed.
     * </p>
     *
     * @return list of type aliases (must include the value of {@link #getDefaultProtocol()})
     * @since 6
     */
    default List<String> getSupportedProtocolList() {
        return List.of(getSupportedProtocols());
    }

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

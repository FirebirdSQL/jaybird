package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.nativeoo.FbOOEmbeddedDatabaseFactory;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.List;

/**
 * GDS factory plugin implementation for embedded OO API
 *
 * @since 6.0
 */
public class FbOOEmbeddedGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String EMBEDDED_TYPE_NAME = "FBOOEMBEDDED";
    private static final String DEFAULT_PROTOCOL = "jdbc:firebirdsql:fboo:embedded:";
    private static final List<String> JDBC_PROTOCOLS = List.of(DEFAULT_PROTOCOL, "jdbc:firebird:fboo:embedded:");

    public String getPluginName() {
        return "GDS implementation for embedded server via OO API.";
    }

    public String getTypeName() {
        return EMBEDDED_TYPE_NAME;
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "6", forRemoval = true)
    @Override
    public String[] getTypeAliases() {
        return new String[0];
    }

    @Override
    public List<String> getTypeAliasList() {
        return List.of();
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "6", forRemoval = true)
    @Override
    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS.toArray(new String[0]);
    }

    @Override
    public List<String> getSupportedProtocolList() {
        return JDBC_PROTOCOLS;
    }

    @Override
    public String getDefaultProtocol() {
        return DEFAULT_PROTOCOL;
    }

    public String getDatabasePath(String server, Integer port, String path) throws SQLException {
        if (path == null) {
            throw new SQLNonTransientConnectionException("Database name/path is required.");
        }

        return path;
    }

    @Override
    public FbOOEmbeddedDatabaseFactory getDatabaseFactory() {
        return FbOOEmbeddedDatabaseFactory.getInstance();
    }
}

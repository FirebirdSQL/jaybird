package org.firebirdsql.gds.impl.nativeoo;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.nativeoo.gds.ng.FbOOEmbeddedDatabaseFactory;

public class FbOOEmbeddedGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String EMBEDDED_TYPE_NAME = "FBOOEMBEDDED";

    private static final String[] TYPE_ALIASES = new String[0];

    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:fboo:embedded:", "jdbc:firebird:fboo:embedded:"
    };

    public String getPluginName() {
        return "GDS implementation for embedded server.";
    }

    public String getTypeName() {
        return EMBEDDED_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public String getDatabasePath(String server, Integer port, String path) throws GDSException {
        if (path == null) {
            throw new GDSException("Database name/path is required.");
        }

        return path;
    }

    @Override
    public FbOOEmbeddedDatabaseFactory getDatabaseFactory() {
        return FbOOEmbeddedDatabaseFactory.getInstance();
    }
}

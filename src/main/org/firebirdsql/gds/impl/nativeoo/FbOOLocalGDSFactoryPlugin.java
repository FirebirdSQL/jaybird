package org.firebirdsql.gds.impl.nativeoo;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.nativeoo.gds.ng.FbOOLocalDatabaseFactory;

public class FbOOLocalGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String LOCAL_TYPE_NAME = "FBOOLOCAL";

    private static final String[] TYPE_ALIASES = new String[0];
    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:fboo:local:", "jdbc:firebird:fboo:local:"
    };

    public String getPluginName() {
        return "JNA-based GDS implementation using IPC communication.";
    }

    public String getTypeName() {
        return LOCAL_TYPE_NAME;
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
    public FbOOLocalDatabaseFactory getDatabaseFactory() {
        return FbOOLocalDatabaseFactory.getInstance();
    }
}
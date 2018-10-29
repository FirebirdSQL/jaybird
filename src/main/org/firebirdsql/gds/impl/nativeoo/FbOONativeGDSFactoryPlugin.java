package org.firebirdsql.gds.impl.nativeoo;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.nativeoo.gds.ng.FbOOClientDatabaseFactory;

public class FbOONativeGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String NATIVE_TYPE_NAME = "FBOONATIVE";

    private static final String[] TYPE_ALIASES = new String[] { "TYPE2" };
    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:fboo:native:", "jdbc:firebird:fboo:native:"
    };

    public String getPluginName() {
        return "JNA-based GDS implementation.";
    }

    public String getTypeName() {
        return NATIVE_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public String getDatabasePath(String server, Integer port, String path) throws GDSException {
        if (server == null) {
            throw new GDSException("Server name/address is required for native implementation.");
        }
        if (path == null) {
            throw new GDSException("Database name/path is required.");
        }

        StringBuilder sb = new StringBuilder();

        sb.append(server);
        if (port != null) {
            sb.append('/').append(port.intValue());
        }

        sb.append(':').append(path);

        return sb.toString();
    }

    @Override
    public FbOOClientDatabaseFactory getDatabaseFactory() {
        return FbOOClientDatabaseFactory.getInstance();
    }
}

package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.AbstractGDS;
import org.firebirdsql.gds.impl.GDSFactoryPlugin;
import org.firebirdsql.jdbc.FBConnection;


public class WireGDSFactoryPlugin implements GDSFactoryPlugin {

    private static final String[] TYPE_ALIASES = new String[]{"TYPE4"};
    private static final String[] JDBC_PROTOCOLS = new String[]{"jdbc:firebirdsql:java:", "jdbc:firebirdsql:"};
    
    public String getPluginName() {
        return "Pure Java GDS implementation.";
    }

    public String getTypeName() {
        return JavaGDSImpl.PURE_JAVA_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public Class getConnectionClass() {
        return FBConnection.class;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public AbstractGDS getGDS() {
        return new JavaGDSImpl();
    }

    public String getDatabasePath(String server, Integer port, String path) throws GDSException{
        if (server == null)
            throw new GDSException("Server name/address is required " +
                    "for pure Java implementation.");
        
        if (path == null)
            throw new GDSException("Database name/path is required.");
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(server);
        if (port != null)
            sb.append("/").append(port.intValue());
        
        sb.append(":").append(path);
        
        return sb.toString();
    }
    
    public String getDatabasePath(String jdbcUrl) throws GDSException {
        
        String[] protocols = getSupportedProtocols();
        for (int i = 0; i < protocols.length; i++) {
            if (jdbcUrl.startsWith(protocols[i]))
                return jdbcUrl.substring(protocols[i].length());
        }

        throw new IllegalArgumentException("Incorrect JDBC protocol handling: "
                + jdbcUrl);
    }

    public String getDefaultProtocol() {
        return getSupportedProtocols()[0];
    }

}

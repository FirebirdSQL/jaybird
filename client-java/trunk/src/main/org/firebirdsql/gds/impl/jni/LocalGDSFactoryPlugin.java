package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.AbstractGDS;
import org.firebirdsql.gds.impl.GDSFactoryPlugin;
import org.firebirdsql.jdbc.FBConnection;


public class LocalGDSFactoryPlugin implements GDSFactoryPlugin {

    private static final String[] TYPE_ALIASES = new String[0];
    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:local:"};
    
    private static LocalGDSImpl gds;
    
    public String getPluginName() {
        return "JNI-based GDS implementation using IPC communication.";
    }

    public String getTypeName() {
        return LocalGDSImpl.LOCAL_TYPE_NAME;
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
        
        if (gds == null)
            gds = new LocalGDSImpl();
        
        return gds;
    }

    public String getDatabasePath(String server, Integer port, String path) throws GDSException{
        return path;
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

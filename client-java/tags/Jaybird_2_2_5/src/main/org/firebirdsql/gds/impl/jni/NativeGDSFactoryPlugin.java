package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSFactoryPlugin;


public class NativeGDSFactoryPlugin implements GDSFactoryPlugin {

    private static final String[] TYPE_ALIASES = new String[]{"TYPE2"};
    private static final String[] JDBC_PROTOCOLS = new String[]{"jdbc:firebirdsql:native:"};
    
    private static GDS gds;
    
    public String getPluginName() {
        return "JNI-based GDS implementation.";
    }

    public String getTypeName() {
        return NativeGDSImpl.NATIVE_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public Class getConnectionClass() {
        return ClassFactory.get(ClassFactory.FBConnection);
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public GDS getGDS() {
        if (gds == null) 
            gds = new NativeGDSImpl();
        
        return gds;
    }

    public String getDatabasePath(String server, Integer port, String path) throws GDSException{
        if (server == null)
            throw new GDSException("Server name/address is required " +
                    "for native implementation.");
        
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

    public int hashCode() {
        return getTypeName().hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj == this) 
            return true;
        
        if (!(obj instanceof NativeGDSFactoryPlugin))
            return false;
        
        return true;
    }   
}

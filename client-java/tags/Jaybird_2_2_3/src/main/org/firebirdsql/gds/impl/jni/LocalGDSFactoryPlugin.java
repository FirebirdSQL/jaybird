package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSFactoryPlugin;


public class LocalGDSFactoryPlugin implements GDSFactoryPlugin {

    private static final String[] TYPE_ALIASES = new String[0];
    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:local:"};
    
    private static GDS gds;
    
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
        return ClassFactory.get(ClassFactory.FBConnection);
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public GDS getGDS() {
        
        if (gds == null) 
            gds = applySyncPolicy(new LocalGDSImpl());
        
        
        return gds;
    }
    
    /**
     * Apply the synchronization policy if the current platform is not Windows.
     * @param tempGds instance if {@link GDS} to which policy should be applied.
     */
    public static GDS applySyncPolicy(GDS tempGds) {
        GDSSynchronizationPolicy.AbstractSynchronizationPolicy syncPolicy = null;

        String osName = System.getProperty("os.name");
        if (osName != null && osName.indexOf("Windows") == -1)
            syncPolicy = new GDSSynchronizationPolicy.ClientLibrarySyncPolicy(tempGds);

        if (syncPolicy != null)
            return GDSSynchronizationPolicy.applySyncronizationPolicy(tempGds, syncPolicy);
        else
            return tempGds;
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
    
    public int hashCode() {
        return getTypeName().hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj == this) 
            return true;
        
        if (!(obj instanceof LocalGDSFactoryPlugin))
            return false;
        
        return true;
    }   
}

package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;


public interface GDSFactoryPlugin {

    String getPluginName();
    
    String getTypeName();
    
    String[] getTypeAliases();
    
    Class getConnectionClass();
    
    String getDefaultProtocol();
    
    String[] getSupportedProtocols();
    
    AbstractGDS getGDS();
    
    String getDatabasePath(String server, Integer port, String path) throws GDSException;
    
    String getDatabasePath(String jdbcUrl) throws GDSException;
}

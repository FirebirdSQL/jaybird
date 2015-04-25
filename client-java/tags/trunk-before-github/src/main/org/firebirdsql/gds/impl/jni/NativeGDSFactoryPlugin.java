/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.jna.FbClientDatabaseFactory;

public class NativeGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    private static final String[] TYPE_ALIASES = new String[]{"TYPE2"};
    private static final String[] JDBC_PROTOCOLS = new String[]{"jdbc:firebirdsql:native:"};
    
    public String getPluginName() {
        return "JNI-based GDS implementation.";
    }

    public String getTypeName() {
        return NativeGDSImpl.NATIVE_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public String getDatabasePath(String server, Integer port, String path) throws GDSException{
        if (server == null)
            throw new GDSException("Server name/address is required " +
                    "for native implementation.");
        
        if (path == null)
            throw new GDSException("Database name/path is required.");
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(server);
        if (port != null)
            sb.append('/').append(port.intValue());
        
        sb.append(':').append(path);
        
        return sb.toString();
    }
    
    /**
     * Initialization-on-demand depending on classloading behavior specified in JLS 12.4
     */
    private static final class GDSHolder {
        private static final GDS gds = new NativeGDSImpl();
    }

    public GDS getGDS() {
        return GDSHolder.gds;
    }

    @Override
    public FbClientDatabaseFactory getDatabaseFactory() {
        // TODO This needs to change if we are going to release jna-client as a separate jar
        return FbClientDatabaseFactory.getInstance();
    }
}

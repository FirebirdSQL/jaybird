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

public class LocalGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    private static final String[] TYPE_ALIASES = new String[0];
    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:local:"};
    
    public String getPluginName() {
        return "JNI-based GDS implementation using IPC communication.";
    }

    public String getTypeName() {
        return LocalGDSImpl.LOCAL_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }
    
    public String getDatabasePath(String server, Integer port, String path) throws GDSException{
        return path;
    }
    
    /**
     * Initialization-on-demand depending on classloading behavior specified in JLS 12.4
     */
    private static final class GDSHolder {
        private static final GDS gds = GDSSynchronizationPolicy.applyClientSyncPolicyNonWindows(new LocalGDSImpl());
    }

    public GDS getGDS() {
        return GDSHolder.gds;
    }
}

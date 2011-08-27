/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * ObjectFactory for the DataSources in org.firebirdsql.ds.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class DataSourceFactory implements ObjectFactory {

    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?, ?> environment) throws Exception {

        Reference ref = (Reference)obj;
        String className = ref.getClassName();
        if (className.equals("org.firebirdsql.ds.FBConnectionPoolDataSource")) {
            return loadConnectionPoolDS(ref);
        }
        
        return null;
    }

    private Object loadConnectionPoolDS(Reference ref) throws Exception {
        FBConnectionPoolDataSource ds = new FBConnectionPoolDataSource();
        ds.setDescription(getRefAddr(ref, "description"));
        ds.setServerName(getRefAddr(ref, "serverName"));
        String portNumber = getRefAddr(ref, "portNumber");
        if (portNumber != null) {
            ds.setPortNumber(Integer.parseInt(portNumber));
        }
        ds.setDatabaseName(getRefAddr(ref, "databaseName"));
        ds.setUser(getRefAddr(ref, "user"));
        ds.setPassword(getRefAddr(ref, "password"));
        ds.setCharSet(getRefAddr(ref, "charSet"));
        String loginTimeout = getRefAddr(ref, "loginTimeout");
        if (loginTimeout != null) {
            ds.setLoginTimeout(Integer.parseInt(loginTimeout));
        }
        ds.setRoleName(getRefAddr(ref, "roleName"));
        ds.setType(getRefAddr(ref, "type"));
        
        return ds;
    }
    
    protected static String getRefAddr(Reference ref, String type) {
        RefAddr addr = ref.get(type);
        if (addr == null) {
            return null;
        } else {
            return addr.getContent().toString();
        }
    }

}

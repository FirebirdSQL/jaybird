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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.firebirdsql.jdbc.FBConnectionProperties;

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
        if (className.equals("org.firebirdsql.ds.FBXADataSource")) {
            return loadXADS(ref);
        }
        
        return null;
    }

    private Object loadConnectionPoolDS(Reference ref) throws Exception {
        FBConnectionPoolDataSource ds = new FBConnectionPoolDataSource();
        loadAbstractCommonDataSource(ds, ref);
        
        return ds;
    }
    
    private Object loadXADS(Reference ref) throws Exception {
        FBXADataSource ds = new FBXADataSource();
        loadAbstractCommonDataSource(ds, ref);
        
        return ds;
    }
    
    private void loadAbstractCommonDataSource(FBAbstractCommonDataSource ds, Reference ref) throws Exception {
        RefAddr propertyContent = ref.get(FBAbstractCommonDataSource.REF_PROPERTIES);
        if (propertyContent != null) {
            byte[] data = (byte[]) propertyContent.getContent();
            FBConnectionProperties props = (FBConnectionProperties) deserialize(data);
            ds.setConnectionProperties(props);
        }
        ds.setDescription(getRefAddr(ref, FBAbstractCommonDataSource.REF_DESCRIPTION));
        ds.setServerName(getRefAddr(ref, FBAbstractCommonDataSource.REF_SERVER_NAME));
        String portNumber = getRefAddr(ref, FBAbstractCommonDataSource.REF_PORT_NUMBER);
        if (portNumber != null) {
            ds.setPortNumber(Integer.parseInt(portNumber));
        }
        ds.setDatabaseName(getRefAddr(ref, FBAbstractCommonDataSource.REF_DATABASE_NAME));
    }
    
    /**
     * Retrieves the content of the given Reference address (type).
     * 
     * @param ref Reference
     * @param type Address or type
     * @return Content as String
     */
    protected static String getRefAddr(Reference ref, String type) {
        RefAddr addr = ref.get(type);
        if (addr == null) {
            return null;
        } 
        Object content = addr.getContent();
        return content != null ? content.toString() : null;
    }
    
    protected static byte[] serialize(Object obj) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        try {
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(obj);
            out.flush();
        } catch(IOException ex) {
            return null;
        }
        
        return bout.toByteArray();
    }

    protected static Object deserialize(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            return in.readObject();
        } catch(IOException ex) {
            return null;
        } catch(ClassNotFoundException ex) {
            return null;
        }
    }

}

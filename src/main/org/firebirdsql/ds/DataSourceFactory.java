// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBConnectionProperties;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import java.io.*;
import java.util.Hashtable;

/**
 * ObjectFactory for the DataSources in org.firebirdsql.ds.
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
public class DataSourceFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,Hashtable<?, ?> environment)
            throws Exception {
        Reference ref = (Reference) obj;
        return switch (ref.getClassName()) {
            case "org.firebirdsql.ds.FBConnectionPoolDataSource" -> loadConnectionPoolDS(ref);
            case "org.firebirdsql.ds.FBXADataSource" -> loadXADS(ref);
            case "org.firebirdsql.ds.FBSimpleDataSource" -> loadSimpleDS(ref);
            default -> null;
        };
    }

    private Object loadConnectionPoolDS(Reference ref) throws Exception {
        var ds = new FBConnectionPoolDataSource();
        loadAbstractCommonDataSource(ds, ref);

        return ds;
    }

    private Object loadXADS(Reference ref) throws Exception {
        var ds = new FBXADataSource();
        loadAbstractCommonDataSource(ds, ref);

        return ds;
    }

    private Object loadSimpleDS(Reference ref) throws Exception {
        RefAddr propertyContent = ref.get(FBSimpleDataSource.REF_MCF);
        FBManagedConnectionFactory mcf = null;
        if (propertyContent != null) {
            byte[] data = (byte[]) propertyContent.getContent();
            mcf = (FBManagedConnectionFactory) deserialize(data);
        }
        if (mcf == null) {
            mcf = new FBManagedConnectionFactory(false);
        }
        var ds = new FBSimpleDataSource(mcf);
        ds.setDescription(getRefAddr(ref, FBSimpleDataSource.REF_DESCRIPTION));
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
    }

    /**
     * Retrieves the content of the given Reference address (type).
     *
     * @param ref
     *         Reference
     * @param type
     *         Address or type
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

    protected static byte[] serialize(Object obj) throws NamingException {
        try {
            var bout = new ByteArrayOutputStream();
            try (var out = new ObjectOutputStream(bout)) {
                out.writeObject(obj);
            }
            return bout.toByteArray();
        } catch (IOException e) {
            var namingException = new NamingException("Could not serialize object");
            namingException.initCause(e);
            throw namingException;
        }
    }

    protected static Object deserialize(byte[] data) throws NamingException {
        try {
            var in = new ObjectInputStream(new ByteArrayInputStream(data));
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            var namingException = new NamingException("Could not deserialize object");
            namingException.initCause(e);
            throw namingException;
        }
    }

}

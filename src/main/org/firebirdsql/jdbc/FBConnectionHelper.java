/*
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
 
package org.firebirdsql.jdbc;

import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.gds.ISCConstants;

import java.util.Properties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import java.lang.reflect.*;
import java.io.*;

/**
 * This class maps the extended JDBC properties to the
 * {@link FBConnectionRequestInfo} instance. It uses
 * <code>java.lang.reflection.</code> to determine correct type of the parameter
 * passed to the {@link java.sql.Driver#connect(String, Properties)} method.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBConnectionHelper {

    public static final String DPB_PREFIX = "isc_dpb_";
    public static final String TPB_PREFIX = "isc_tpb_";

    public static final String ISC_ENCODINGS_RESOURCE =
        "isc_encodings.properties";

    private static final HashMap dpbTypes = new HashMap();
    private static final HashMap tpbTypes = new HashMap();

    private static final HashMap iscEncodings = new HashMap();
    private static final HashMap javaEncodings = new HashMap();

    private static boolean encodingsLoaded = false;

    /*
     * Initialize mappings between various GDS constant names and
     * their values. This operation should be executed only once.
     */
    static {
        Class iscClass = ISCConstants.class;

        Field[] fields = iscClass.getFields();

        for(int i = 0; i < fields.length; i++) {
            if (!fields[i].getType().getName().equals("int"))
                continue;

            String name = fields[i].getName();
            Integer value;
            try {
                value = (Integer)fields[i].get(null);
            } catch(IllegalAccessException iaex) {
                continue;
            }

            if (name.startsWith(DPB_PREFIX)) {
                // put the correct parameter name
                dpbTypes.put(name.substring(DPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                dpbTypes.put(name, value);
            } else
            if (name.startsWith(TPB_PREFIX)) {
                // put the correct parameter name
                tpbTypes.put(name.substring(TPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tpbTypes.put(name, value);
            } else
                continue;
        }

    }

    public static FBConnectionRequestInfo getDefaultCri() {
        FBConnectionRequestInfo result = new FBConnectionRequestInfo();
        //
        // Important for performance on inserts. the charset code must not be none
        //
/* There are some old problems where the database use character set NONE 
 * and the client request a different character set 
        String enc = FBConnectionHelper.getDefaultJavaEncoding();
        String iscEncoding = FBConnectionHelper.getIscEncoding(enc);
        if (iscEncoding != null)
            result.setProperty(ISCConstants.isc_dpb_lc_ctype,iscEncoding);
        else 
            result.setProperty(ISCConstants.isc_dpb_lc_ctype,"ISO8859_1");
*/		  
        //result.setProperty(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
        //result.setProperty(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        //result.setProperty(ISCConstants.isc_dpb_force_write, new byte[] {0});

        return result;
    }

    /**
     * Get the instance of {@link FBConnectionRequestInfo} containing all
     * relevant parameters from the <code>info</code> properties.
     *
     * @param info instance of {@link Properties} containing connection
     * parameters.
     *
     * @param baseCri connection request info used as the base for a newly
     * created one. All types are copied from the <code>baseCri</code> into
     * the result, but these properties can be overriden by the <code>info</code>
     * properties. Can be <code>null</code>.
     *
     * @return instance of {@link FBConnectionRequestInfo} containing all
     * relevant connection parameters.
     */
    public static FBConnectionRequestInfo getCri(Properties info,
        FBConnectionRequestInfo baseCri)
    {
        FBConnectionRequestInfo cri;

        if (baseCri != null)
            cri = new FBConnectionRequestInfo(baseCri);
        else
            cri = new FBConnectionRequestInfo(getDefaultCri());

        // process all set keys
        Iterator keys = info.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String value = info.getProperty(key);

            Integer type = (Integer)dpbTypes.get(key);

            // if the type is unknown, continue
            if (type == null) continue;

            // set the value of the DPB
            try {

                // try to deal with a value as a byte
                byte byteValue = Byte.parseByte(value);
                cri.setProperty(type.intValue(), new byte[] {byteValue});

            } catch(NumberFormatException nfex) {

                // ok, that's not a byte, then set it as string
                cri.setProperty(type.intValue(), value);
            }
        }

        return cri;
    }

    /**
     * Get the transaction parameter buffer using the information supplied
     * with connection.
     *
     * @param info instance of {@link Properties} containing the connection
     * parameters.
     *
     * @return a TPB containing all relevant parameters extracted from
     * <code>info</code>
     */
    public static FBTpb getTpb(Properties info) {
        FBTpb tpb = new FBTpb();

        Iterator keys = info.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            Integer type = (Integer)tpbTypes.get(key);

            if (type == null) continue;

            tpb.add(type);
        }

        return tpb;
    }

    /**
     * Load mapping between Java and InterBase encodings. This method loads the
     * mapping using the classloader that loaded this class.
     */
    private static void loadEncodings() {
        ClassLoader cl = FBConnectionHelper.class.getClassLoader();

        InputStream in = null;

        // get the stream from the classloader or system classloader
        if (cl == null)
            in = ClassLoader.getSystemResourceAsStream(ISC_ENCODINGS_RESOURCE);
        else
            in = cl.getResourceAsStream(ISC_ENCODINGS_RESOURCE);

        if (in == null) return;

        // load properties
        Properties props = new Properties();
        try {
            props.load(in);
        } catch(IOException ioex) {
            ioex.printStackTrace();
            return;
        } finally {
            try {
                in.close();
            } catch(IOException ex) {
                ex.printStackTrace();
                // do nothing here
            }
        }

        // fill the direct and inversed mappings
        iscEncodings.putAll(props);

        Iterator iterator = props.keySet().iterator();
        while(iterator.hasNext()) {
            String iscEncoding = (String)iterator.next();
            String javaEncoding = (String)props.get(iscEncoding);
            javaEncodings.put(javaEncoding, iscEncoding);
        }

        encodingsLoaded = true;
    }

    /**
     * Get Java language encoding for given InterBase encoding.
     *
     * @param iscEncoding InterBase encoding
     * @return corresponding Java encoding or <code>null</code> if none found.
     */
    public static String getJavaEncoding(String iscEncoding) {
        if (!encodingsLoaded)
            loadEncodings();

        // 
        // very important for performance
        // if javaEncoding is the default one, set to null
        //
        String javaEncoding = (String)iscEncodings.get(iscEncoding);
        String defaultEncoding = getDefaultJavaEncoding();
        if (javaEncoding.equalsIgnoreCase(defaultEncoding)) 
            return null;
        else 
            return javaEncoding;
    }

    public static String getDefaultJavaEncoding() {
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(new byte[2])); 
        return reader.getEncoding();
    }
    /**
     * Get InterBase encoding for given Java language encoding.
     *
     * @param javaEncoding Java language encoding.
     * @return corresponding InterBase encoding or <code>null</code> if none
     * found.
     */
    public static String getIscEncoding(String javaEncoding) {
        if (!encodingsLoaded)
            loadEncodings();

        return (String)javaEncodings.get(javaEncoding);
    }

}

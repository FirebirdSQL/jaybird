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
import org.firebirdsql.jca.FBTpbMapper;
import org.firebirdsql.jca.FBResourceException;
import org.firebirdsql.gds.GDS;
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
    
    public static final String TRANSACTION_SERIALIZABLE = "TRANSACTION_SERIALIZABLE";
    public static final String TRANSACTION_REPEATABLE_READ = "TRANSACTION_REPEATABLE_READ";
    public static final String TRANSACTION_READ_COMMITTED = "TRANSACTION_READ_COMMITTED";
    

    public static final String DPB_PREFIX = "isc_dpb_";
    public static final String TPB_PREFIX = "isc_tpb_";

    public static final String ISC_ENCODINGS_RESOURCE =
        "isc_encodings.properties";
        
    public static final String ISC_ENCODING_SIZE_RESOURCE = 
        "isc_encoding_size.properties";

    private static final HashMap dpbTypes = new HashMap();
    private static final HashMap tpbTypes = new HashMap();

    private static final HashMap iscEncodings = new HashMap();
    private static final HashMap javaEncodings = new HashMap();
    private static final HashMap iscEncodingSizes = new HashMap();

    private static boolean encodingsLoaded = false;
    private static boolean encodingSizesLoaded = false;

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
        FBTpb tpb = new FBTpb(FBTpbMapper.DEFAULT_MAPPER);

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
     * This method extracts TPB mapping information from the connection 
     * parameters. Two formats are supported:
     * <ul>
     * <li><code>info</code> contains <code>"tpb_mapping"</code> parameter
     * pointing to a resource bundle with mapping information;
     * <li><code>info</code> contains separate mappings for each of following
     * transaction isolation levels: <code>"TRANSACTION_SERIALIZABLE"</code>, 
     * <code>"TRANSACTION_REPEATABLE_READ"</code> and 
     * <code>"TRANSACTION_READ_COMMITTED"</code>.
     * </ul>
     * 
     * @param info connection parameters passed into a driver.
     * 
     * @return instance of {@link FBTpbMapper} containing specified TPB mapping
     * or <code>null</code> if not TPB mapping was specified.
     * 
     * @throws FBResourceException if specified mapping is incorrect.
     */
    public static FBTpbMapper getTpbMapper(Properties info) throws FBResourceException {
        String tpbMapping = (String)info.getProperty(FBDriver.TPB_MAPPING);
        
        FBTpbMapper tpbMapper = null;
        
        if (tpbMapping != null) 
            tpbMapper = new FBTpbMapper(tpbMapping, 
                FBConnectionHelper.class.getClassLoader());
        else {
            HashMap mapping = new HashMap();
            
            if (info.containsKey(TRANSACTION_SERIALIZABLE))
                mapping.put(TRANSACTION_SERIALIZABLE, 
                    info.get(TRANSACTION_SERIALIZABLE));
                
            if (info.containsKey(TRANSACTION_REPEATABLE_READ))
                mapping.put(TRANSACTION_REPEATABLE_READ, 
                    info.get(TRANSACTION_REPEATABLE_READ));
                    
            if (info.containsKey(TRANSACTION_READ_COMMITTED))
                mapping.put(TRANSACTION_READ_COMMITTED,
                    info.get(TRANSACTION_READ_COMMITTED));
                    
            if (mapping.size() > 0)
                tpbMapper = new FBTpbMapper(mapping);
        }
        
        return tpbMapper;
    }
    
    /**
     * Get value of TPB parameter for the specified name. This method tries to
     * match string representation of the TPB parameter with its value.
     * 
     * @param name string representation of TPB parameter, can have "isc_tpb_"
     * prefix.
     * 
     * @return value corresponding to the specified parameter name or null if
     * nothing was found.
     */
    public static Integer getTpbParam(String name) {
        return (Integer)tpbTypes.get(name);
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
     * Load mapping between Java and InterBase encodings. This method loads the
     * mapping using the classloader that loaded this class.
     */
    private static void loadEncodingSizes() {
        ClassLoader cl = FBConnectionHelper.class.getClassLoader();

        InputStream in = null;

        // get the stream from the classloader or system classloader
        if (cl == null)
            in = ClassLoader.getSystemResourceAsStream(ISC_ENCODING_SIZE_RESOURCE);
        else
            in = cl.getResourceAsStream(ISC_ENCODING_SIZE_RESOURCE);

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

        Iterator iterator = props.keySet().iterator();
        while(iterator.hasNext()) {
            String iscEncoding = (String)iterator.next();
            String size = (String)props.get(iscEncoding);
            iscEncodingSizes.put(iscEncoding, new Byte(size));
        }

        encodingSizesLoaded = true;
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
        if (javaEncoding == null || javaEncoding.equalsIgnoreCase(defaultEncoding)) 
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
    
    /**
     * Get size of a character for the specified InterBase encoding.
     *
     * @param iscEncoding InterBase encoding.
     * @return maximum size of the character in bytes or 1 if encoding was 
     * not found.
     */
    public static byte getIscEncodingSize(String iscEncoding) {
        if (!encodingSizesLoaded)
            loadEncodingSizes();
            
        Byte result = (Byte)iscEncodingSizes.get(iscEncoding);
        if (result == null)
            return 1;
        else
            return result.byteValue();
    }

}

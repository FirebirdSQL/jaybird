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
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.GDS;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.Iterator;

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
    
    /**
     * Get a default instance of {@link FBConnectionRequestInfo} for a 
     * given GDS implementation. The returned FBConnectionRequestInfo
     * will be bound to the GDS implemenation that is passed in as a parameter.
     *
     * @param gds The <code>GDS</code> implementation
     * @return The default <code>FBConnectionRequestInfo</code> for the GDS
     * implementation
     */
    public static FBConnectionRequestInfo getDefaultCri(GDS gds) {
        FBConnectionRequestInfo result = FBConnectionRequestInfo.newInstance(gds);
        return result;
    }

    /**
     * Get the instance of {@link FBConnectionRequestInfo} containing all
     * relevant parameters from the <code>info</code> properties.
     *
     * The FBConnectionRequestInfo instance returned will be 'bound' to the
     * same GDS implementation as baseCri.
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
    public static FBConnectionRequestInfo getCri(Properties info, FBConnectionRequestInfo baseCri) {
        final FBConnectionRequestInfo cri = baseCri.deepCopy();

        copyPropertiesIntoCri(info, cri);

        return cri;
    }

    /**
     * Get the instance of {@link FBConnectionRequestInfo} containing all
     * relevant parameters from the <code>info</code> properties.
     *
     * The FBConnectionRequestInfo instance returned will be 'bound' to the
     * same GDS implementation supplied in the gdsToUse parameter.
     *
     * @param info instance of {@link Properties} containing connection
     * parameters.
     *
     * @param gdsToUse The GDS implementation to which the returned
     * {@link FBConnectionRequestInfo} will be bound.
     *
     * @return instance of {@link FBConnectionRequestInfo} containing all
     * relevant connection parameters.
     */
    public static FBConnectionRequestInfo getCri(Properties info, GDS gdsToUse) {
        final FBConnectionRequestInfo cri = getDefaultCri(gdsToUse).deepCopy();

        copyPropertiesIntoCri(info, cri);

        return cri;
    }

    /**
     * Get integer value of the DPB key corresponding to the specified name.
     * 
     * @param name name of the key.
     * 
     * @return instance of {@link Integer} corresponding to the specified name
     * or <code>null</code> if value is not known.
     */
    public static Integer getDpbKey(String name) {
        return (Integer)dpbTypes.get(name);
    }
    
    /**
     * Get mapping between DPB names and their keys.
     * 
     * @return instance of {@link Map}, where key is the name of DPB parameter,
     * value is its DPB key.
     */
    public static Map getDpbMap() {
        return Collections.unmodifiableMap(dpbTypes);
    }
    
    /**
     * Copy the properties supplied in the 'info' parameter into the FBConnectionRequestInfo
     * instance supplied in the 'cri' parameter.
     */
    private static void copyPropertiesIntoCri(Properties info, FBConnectionRequestInfo cri) {
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
                if ("".equals(value))
                    cri.setProperty(type.intValue());
                else
                    cri.setProperty(type.intValue(), value);
            }
        }
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
    public static FBTpb getTpb(GDS gds, Properties info) {
        FBTpb tpb = new FBTpb(FBTpbMapper.getDefaultMapper(gds));

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
    public static FBTpbMapper getTpbMapper(GDS gds, Properties info) throws FBResourceException {
        String tpbMapping = (String)info.getProperty(FBDriver.TPB_MAPPING);
        
        FBTpbMapper tpbMapper = null;
        
        if (tpbMapping != null) 
            tpbMapper = new FBTpbMapper(gds, tpbMapping, 
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
                tpbMapper = new FBTpbMapper(gds, mapping);
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
        String defaultEncoding = System.getProperty("file.encoding");
        if (javaEncoding == null || javaEncoding.equalsIgnoreCase(defaultEncoding)) 
            return null;
        else 
            return javaEncoding;
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
    
    private static final int[][] CHARSET_MAXIMUM_SIZE = new int[][] {
          { 0, 1}   // NONE
        , { 1, 1}   // OCTETS
        , { 2, 1}   // ASCII
        , { 3, 3}   // UNICODE_FSS
        , { 5, 2}   // SJIS_0208
        , { 6, 2}   // EUJC_0208
        , { 9, 1}   // DOS737
        , {10, 1}   // DOS437
        , {11, 1}   // DOS850
        , {12, 1}   // DOS865
        , {13, 1}   // DOS775
        , {14, 1}   // DOS863
        , {15, 1}   // DOS775
        , {16, 1}   // DOS858
        , {17, 1}   // DOS862
        , {18, 1}   // DOS864
        , {19, 1}   // NEXT
        , {21, 1}   // ISO8859_1
        , {22, 1}   // ISO8859_2
        , {23, 1}   // ISO8859_3
        , {34, 1}   // ISO8859_4
        , {35, 1}   // ISO8859_5
        , {36, 1}   // ISO8859_6
        , {37, 1}   // ISO8859_7
        , {38, 1}   // ISO8859_8
        , {39, 1}   // ISO8859_9
        , {40, 1}   // ISO8859_13
        , {44, 2}   // KSC_5601
        , {45, 1}   // DOS852
        , {46, 1}   // DOS857
        , {47, 1}   // DOS861
        , {48, 1}   // DOS866
        , {49, 1}   // DOS869
        , {50, 1}   // CYRL
        , {51, 1}   // WIN1250
        , {52, 1}   // WIN1251
        , {53, 1}   // WIN1252
        , {54, 1}   // WIN1253
        , {55, 1}   // WIN1254
        , {56, 2}   // BIG_5
        , {57, 2}   // GB2312
        , {58, 1}   // WIN1255
        , {59, 1}   // WIN1256
        , {60, 1}   // WIN1257
    };
    
    /**
     * Get size of a character for the specified InterBase encoding.
     *
     * @param iscEncoding InterBase encoding.
     * @return maximum size of the character in bytes or 1 if encoding was 
     * not found.
     */
    public static int getIscEncodingSize(String iscEncoding) {
        if (!encodingSizesLoaded)
            loadEncodingSizes();
            
        Byte result = (Byte)iscEncodingSizes.get(iscEncoding);
        if (result == null)
            return 1;
        else
            return result.byteValue();
    }
    
    /**
     * Get size of a character for the specified character set.
     *
     * @param characterSetId of the character set.
     * @return maximum size of the character in bytes or 1 if charset was 
     * not found.
     */
    public static int getCharacterSetSize(int characterSetId) {
        
        for (int i = 0; i < CHARSET_MAXIMUM_SIZE.length; i++) {
            if (CHARSET_MAXIMUM_SIZE[i][0] == characterSetId)
                return CHARSET_MAXIMUM_SIZE[i][1];
        }

        // let's assume that default length is 1
        return 1;
    }

}

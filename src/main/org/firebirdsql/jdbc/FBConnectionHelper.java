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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBResourceException;

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
    
    private static final int TYPE_UNKNOWN = 0;
    private static final int TYPE_BOOLEAN = 1;
    private static final int TYPE_BYTE = 2;
    private static final int TYPE_INT = 3;
    private static final int TYPE_STRING = 4;
    
    public static final String TRANSACTION_SERIALIZABLE = "TRANSACTION_SERIALIZABLE";
    public static final String TRANSACTION_REPEATABLE_READ = "TRANSACTION_REPEATABLE_READ";
    public static final String TRANSACTION_READ_COMMITTED = "TRANSACTION_READ_COMMITTED";
    

    public static final String DPB_PREFIX = "isc_dpb_";
    public static final String TPB_PREFIX = "isc_tpb_";

    public static final String TPB_MAPPING_PROPERTY = "tpb_mapping";
    
    public static final String ISC_ENCODINGS_RESOURCE =
        "isc_encodings.properties";
        
    public static final String ISC_ENCODING_SIZE_RESOURCE = 
        "isc_encoding_size.properties";
    
    public static final String ISC_DPB_TYPES_RESOURCE = 
        "isc_dpb_types.properties";

    private static final HashMap dpbTypes = new HashMap();
    private static final HashMap dpbParameterTypes = new HashMap();
    
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
        
        loadDpbParameterTypes();

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
    
    public static Object parseDpbString(String name, Object value) {
        
        // for the sake of unification we allow passing boolean, byte and integer
        // types too, we loose some cycles here, but that is called relatively
        // rarely, a tradeoff between code maintainability and CPU cycles.
        if (value instanceof Boolean)
            return value;
        else
        if (value instanceof Byte)
            return value;
        else
        if (value instanceof Integer)
            return value;
        
        // if passed value is not string, throw an exception
        if (value != null && !(value instanceof String))
            throw new ClassCastException(value.getClass().getName());
        
        Integer type = (Integer)dpbParameterTypes.get(name);
        
        if (type == null)
            type = new Integer(TYPE_UNKNOWN);
        
        switch(type.intValue()) {
            case TYPE_BOOLEAN : 
                return "".equals(value) ? Boolean.TRUE : new Boolean((String)value);
            
            case TYPE_BYTE : 
                return new Byte((String)value);
                
            case TYPE_INT :
                return new Integer((String)value);
                
            case TYPE_STRING : 
                return value;
                
            case TYPE_UNKNOWN :
            default :
                
                // set the value of the DPB by probing to convert string
                // into int or byte value, this method gives very good result 
                // for guessing the method to call from the actual value;
                // null values are assumed to be booleans.
                
                if (value == null)
                    return Boolean.TRUE;
            
                try {

                    // try to deal with a value as a byte
                    int intValue = Integer.parseInt((String)value);

                    if (intValue < 256)
                        return new Byte((byte) intValue);
                    else
                        return new Integer(intValue);

                } catch (NumberFormatException nfex) {

                    // ok, that's not a byte, then set it as string
                    if ("".equals(value))
                        return Boolean.TRUE;
                    else
                        return value;
                }
        }
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
    public static void processTpbMapping(GDS gds,
            FirebirdConnectionProperties connectionProperties, Properties info)
            throws FBResourceException {
        
        if (info.containsKey(TRANSACTION_SERIALIZABLE))
            connectionProperties.setTransactionParameters(
                    Connection.TRANSACTION_SERIALIZABLE, 
                    FBTpbMapper.processMapping(gds, 
                            info.getProperty(TRANSACTION_SERIALIZABLE)));
            
        if (info.containsKey(TRANSACTION_REPEATABLE_READ))
            connectionProperties.setTransactionParameters(
                    Connection.TRANSACTION_REPEATABLE_READ, 
                    FBTpbMapper.processMapping(gds, 
                            info.getProperty(TRANSACTION_REPEATABLE_READ)));
                
        if (info.containsKey(TRANSACTION_READ_COMMITTED))
            connectionProperties.setTransactionParameters(
                    Connection.TRANSACTION_READ_COMMITTED, 
                    FBTpbMapper.processMapping(gds, 
                            info.getProperty(TRANSACTION_READ_COMMITTED)));
                
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
    private synchronized static void loadEncodings() {
        
        if (encodingsLoaded)
            return;
        
        Properties props;
        try {
            props = loadProperties(ISC_ENCODINGS_RESOURCE);
        } catch(IOException ioex) {
            ioex.printStackTrace();
            return;
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
    private synchronized static void loadEncodingSizes() {
        
        if (encodingSizesLoaded)
            return;
        
        Properties props;
        try {
            props = loadProperties(ISC_ENCODING_SIZE_RESOURCE);
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
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
     * Load mapping between DPB key and their parameter types.
     */
    private static void loadDpbParameterTypes() {
        Properties props;
        try {
            props = loadProperties(ISC_DPB_TYPES_RESOURCE);
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
        }
        
        for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            
            String key = (String)entry.getKey();
            String shortKey = key.substring(DPB_PREFIX.length());
            String value = (String)entry.getValue();
            
            if ("boolean".equals(value)) {
                dpbParameterTypes.put(key, new Integer(TYPE_BOOLEAN));
                dpbParameterTypes.put(shortKey, new Integer(TYPE_BOOLEAN));
            } else
            if ("byte".equals(value)) {
                dpbParameterTypes.put(key, new Integer(TYPE_BYTE));
                dpbParameterTypes.put(shortKey, new Integer(TYPE_BYTE));
            } else
            if ("int".equals(value)) {
                dpbParameterTypes.put(key, new Integer(TYPE_INT));
                dpbParameterTypes.put(shortKey, new Integer(TYPE_INT));
            } else
            if ("string".equals(value)) {
                dpbParameterTypes.put(key, new Integer(TYPE_STRING));
                dpbParameterTypes.put(shortKey, new Integer(TYPE_STRING));
            } else
                continue;
        }
    }
    
    /**
     * Load properties from the specified resource. This method uses the same
     * class loader that loaded this class.
     * 
     * @param resource path to the resource relative to the root of the 
     * classloader.
     * 
     * @return instance of {@link Properties} containing loaded resources or
     * <code>null</code> if resource was not found.
     * 
     * @throws IOException if I/O error occured.
     */
    private static Properties loadProperties(String resource) throws IOException {
        ClassLoader cl = FBConnectionHelper.class.getClassLoader();

        InputStream in = null;

        // get the stream from the classloader or system classloader
        if (cl == null)
            in = ClassLoader.getSystemResourceAsStream(resource);
        else
            in = cl.getResourceAsStream(resource);

        if (in == null) 
            return null;

        try {
            Properties props = new Properties();
            props.load(in);
            return props;
        } finally {
            in.close();
        }
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

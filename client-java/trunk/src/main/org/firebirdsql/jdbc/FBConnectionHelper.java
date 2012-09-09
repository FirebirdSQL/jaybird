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
    
    public static final String ISC_DPB_TYPES_RESOURCE = 
        "isc_dpb_types.properties";

    private static final Map<String, Integer> dpbTypes;
    private static final Map<String, Integer> dpbParameterTypes;
    private static final Map<String, Integer> tpbTypes;

    /*
     * Initialize mappings between various GDS constant names and
     * their values. This operation should be executed only once.
     */
    static {
        final Map<String, Integer> tempDpbTypes = new HashMap<String, Integer>();
        final Map<String, Integer> tempTpbTypes = new HashMap<String, Integer>();
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
                tempDpbTypes.put(name.substring(DPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tempDpbTypes.put(name, value);
            } else if (name.startsWith(TPB_PREFIX)) {
                // put the correct parameter name
                tempTpbTypes.put(name.substring(TPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tempTpbTypes.put(name, value);
            }
        }
        
        dpbTypes = Collections.unmodifiableMap(tempDpbTypes);
        tpbTypes = Collections.unmodifiableMap(tempTpbTypes);
        dpbParameterTypes = Collections.unmodifiableMap(loadDpbParameterTypes());
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
        return dpbTypes.get(name);
    }
    
    /**
     * Get mapping between DPB names and their keys.
     * 
     * @return instance of {@link Map}, where key is the name of DPB parameter,
     * value is its DPB key.
     */
    public static Map<String, Integer> getDpbMap() {
        return dpbTypes;
    }
    
    public static Object parseDpbString(String name, Object value) {
        
        // for the sake of unification we allow passing boolean, byte and integer
        // types too, we loose some cycles here, but that is called relatively
        // rarely, a tradeoff between code maintainability and CPU cycles.
        if (value instanceof Boolean || value instanceof Byte || value instanceof Integer)
            return value;
        
        // if passed value is not string, throw an exception
        if (value != null && !(value instanceof String))
            throw new ClassCastException(value.getClass().getName());
        
        Integer type = dpbParameterTypes.get(name);
        
        if (type == null)
            type = Integer.valueOf(TYPE_UNKNOWN);
        
        switch(type.intValue()) {
            case TYPE_BOOLEAN : 
                return "".equals(value) ? Boolean.TRUE : Boolean.valueOf((String)value);
            
            case TYPE_BYTE : 
                return Byte.valueOf((String)value);
                
            case TYPE_INT :
                return Integer.valueOf((String)value);
                
            case TYPE_STRING : 
                return value;
                
            case TYPE_UNKNOWN :
            default :
                /* set the value of the DPB by probing to convert string
                 * into int or byte value, this method gives very good result
                 * for guessing the method to call from the actual value;
                 * null values and empty strings are assumed to be booleans.
                 */
                if (value == null || "".equals(value))
                    return Boolean.TRUE;
            
                try {
                    // try to deal with a value as a byte or int
                    int intValue = Integer.parseInt((String)value);
                    // TODO Find out if this is intentional
                    if (intValue < 256)
                        return Byte.valueOf((byte) intValue);
                    else
                        return Integer.valueOf(intValue);
                } catch (NumberFormatException nfex) {
                    // all else fails: return as is (string)
                    return value;
                }
        }
    }
    
    /**
     * This method extracts TPB mapping information from the connection 
     * parameters and adds it to the connectionProperties. Two formats are supported:
     * <ul>
     * <li><code>info</code> contains <code>"tpb_mapping"</code> parameter
     * pointing to a resource bundle with mapping information;
     * <li><code>info</code> contains separate mappings for each of following
     * transaction isolation levels: <code>"TRANSACTION_SERIALIZABLE"</code>, 
     * <code>"TRANSACTION_REPEATABLE_READ"</code> and 
     * <code>"TRANSACTION_READ_COMMITTED"</code>.
     * </ul>
     * 
     * @param gds GDS object
     * @param connectionProperties FirebirdConnectionProperties to set transaction state
     * @param info connection parameters passed into a driver.
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
        return tpbTypes.get(name);
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
     * Load mapping between DPB key and their parameter types.
     */
    private static Map<String, Integer> loadDpbParameterTypes() {
        Properties props;
        try {
            props = loadProperties(ISC_DPB_TYPES_RESOURCE);
        } catch(IOException ex) {
            // TODO Log, throw error?
            ex.printStackTrace();
            return Collections.emptyMap();
        }
        final Map<String, Integer> tempDpbParameterTypes = new HashMap<String, Integer>();
        
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String)entry.getKey();
            String shortKey = key.substring(DPB_PREFIX.length());
            String value = (String)entry.getValue();
            
            Integer typeValue;
            if ("boolean".equals(value)) {
                typeValue = Integer.valueOf(TYPE_BOOLEAN);
            } else if ("byte".equals(value)) {
                typeValue = Integer.valueOf(TYPE_BYTE);
            } else if ("int".equals(value)) {
                typeValue = Integer.valueOf(TYPE_INT);
            } else if ("string".equals(value)) {
                typeValue = Integer.valueOf(TYPE_STRING);
            } else {
                continue;
            }
            tempDpbParameterTypes.put(key, typeValue);
            tempDpbParameterTypes.put(shortKey, typeValue);
        }
        return tempDpbParameterTypes;
    }
}

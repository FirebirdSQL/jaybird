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
package org.firebirdsql.gds.impl;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Type-safe "enumeration" of the GDS types registered in the system. 
 */
public final class GDSType implements Serializable {
    
    private static HashMap typeMap;

    // Getter for the typeMap variable, please note that static initializer of
    // the GDSFactory class will access the registerType(String) method
    private static HashMap getTypeMap() {
        if (typeMap == null)
            typeMap = new HashMap();
        
        return typeMap;
    }
    
    // needed to initiate static initialization of the GDSFactory
    private static final GDSFactory factory = new GDSFactory();
    
//    public static final String PURE_JAVA_STR = "PURE_JAVA";
//    public static final String TYPE4_STR = "TYPE4";
    
//    public static final String NATIVE_STR = "NATIVE";
//    public static final String TYPE2_STR = "TYPE2";
//    public static final String EMBEDDED_STR = "EMBEDDED";
//    public static final String LOCAL_STR = "LOCAL";
//    public static final String ORACLE_MODE_STR = "ORACLE_MODE";
//    
//    public static final GDSType PURE_JAVA = new GDSType(PURE_JAVA_STR);
//    public static final GDSType NATIVE = new GDSType(NATIVE_STR);
//    public static final GDSType NATIVE_EMBEDDED = new GDSType(EMBEDDED_STR);
//    public static final GDSType NATIVE_LOCAL = new GDSType(LOCAL_STR);
//    public static final GDSType ORACLE_MODE = new GDSType(ORACLE_MODE_STR);
//
//    private static final GDSType[] PRIVATE_VALUES = {
//        PURE_JAVA, NATIVE, NATIVE_EMBEDDED, NATIVE_LOCAL, ORACLE_MODE
//    };
    
//    static {
//        STRING_MAP.put(PURE_JAVA_STR, PURE_JAVA);
//        STRING_MAP.put(TYPE4_STR, PURE_JAVA);
//        STRING_MAP.put(NATIVE_STR, NATIVE);
//        STRING_MAP.put(TYPE2_STR, NATIVE);
//        STRING_MAP.put(EMBEDDED_STR, NATIVE_EMBEDDED);
//        STRING_MAP.put(LOCAL_STR, NATIVE_LOCAL);
//        STRING_MAP.put(ORACLE_MODE_STR, ORACLE_MODE);
//    }
    
    /**
     * Factory method for instances of this class. There's only three possible
     * instances of this class, however linking to them directly is not always
     * possible and desirable (for example when type is specified in configuration).
     * This method gives the possibility to translate string representation of
     * the type into correct type instance.
     * 
     * @param type string representation of the type to match (matching is case
     * insensitive). Possible values are:
     * <ul>
     * <li><code>"PURE_JAVA"</code> or <code>"TYPE4"</code> for pure Java (type 4)
     * driver version;
     * <li><code>"NATIVE"</code> or <code>"TYPE2"</code> for type 2 JDBC driver
     * that will use Firebird client library to access the database using JNI 
     * link.
     * <li><code>"EMBEDDED"</code> for type 2 JDBC driver that will use embedded
     * version of the server to perform database-related operations. 
     * </ul>
     * 
     * @return instance of {@link GDSType} corresponding to the specified
     * string representation or <code>null</code> if no match could be found. 
     */
    public static GDSType getType(String type) {
        if (type == null)
            return null;
            
        return (GDSType)typeMap.get(type.toUpperCase());
    }
    
    /**
     * Register the GDS type. Method can be called only by 
     * {@link GDSFactory#registerPlugin(GDSFactoryPlugin)} method.
     *  
     * @param typeName name of the GDS type.
     * 
     * @return instance of {@link GDSType} corresponding to the specified type
     * name.
     */
    static GDSType registerType(String typeName) {
        HashMap typeMap = getTypeMap();
        
        synchronized(typeMap) {
            if (typeMap.containsKey(typeName))
                return (GDSType)typeMap.get(typeName);
            
            GDSType type = new GDSType(typeName);
            typeMap.put(typeName, type);
            
            return type;
        }
    }
    
    private GDSType(String s) {
        name = s;
    }

    public Object readResolve() {
        return registerType(name);
    }

    public String toString() {
        return name;
    }

    private final String name;
}

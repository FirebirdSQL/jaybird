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
package org.firebirdsql.gds;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Type of GDS: pure java, native (or type 2), or embedded. 
 */
public final class GDSType implements Serializable {
    public static final String PURE_JAVA_STR = "PURE_JAVA";
    public static final String TYPE4_STR = "TYPE4";
    public static final String NATIVE_STR = "NATIVE";
    public static final String TYPE2_STR = "TYPE2";
    public static final String EMBEDDED_STR = "EMBEDDED";
    public static final String LOCAL_STR = "LOCAL";
    
    private static int nextOrdinal = 0;

    public static final GDSType PURE_JAVA = new GDSType(PURE_JAVA_STR);
    public static final GDSType NATIVE = new GDSType(NATIVE_STR);
    public static final GDSType NATIVE_EMBEDDED = new GDSType(EMBEDDED_STR);
    public static final GDSType NATIVE_LOCAL = new GDSType(LOCAL_STR);

    private static final GDSType[] PRIVATE_VALUES = {
        PURE_JAVA, NATIVE, NATIVE_EMBEDDED
    };
    
    private static final HashMap STRING_MAP = new HashMap();
    static {
        STRING_MAP.put(PURE_JAVA_STR, PURE_JAVA);
        STRING_MAP.put(TYPE4_STR, PURE_JAVA);
        STRING_MAP.put(NATIVE_STR, NATIVE);
        STRING_MAP.put(TYPE2_STR, NATIVE);
        STRING_MAP.put(EMBEDDED_STR, NATIVE_EMBEDDED);
        STRING_MAP.put(LOCAL_STR, NATIVE_LOCAL);
    }
    
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
     * 
     * @return instance of {@link GDSType} corresponding to the specified
     * string representation or <code>null</code> if no match could be found. 
     */
    public static GDSType getType(String type) {
        if (type == null)
            return null;
            
        return (GDSType)STRING_MAP.get(type.toUpperCase());
    }

    private GDSType(String s) {
        name = s;
        ordinal = nextOrdinal++;
    }

    public Object readResolve() {
        return PRIVATE_VALUES[ordinal];
    }

    public String toString() {
        return name;
    }

    private final String name;
    private final int ordinal;
}
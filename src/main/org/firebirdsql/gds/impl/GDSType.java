/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Type-safe "enumeration" of the GDS types registered in the system. 
 */
public final class GDSType implements Serializable {
    
    private static final Map<String, GDSType> typeMap = new HashMap<>();

    // DO NOT REMOVE: needed to initiate static initialization of the GDSFactory
    @SuppressWarnings("unused")
    private static final GDSFactory factory = new GDSFactory();
    
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
        if (type == null) {
            return null;
        }
            
        return typeMap.get(type.toUpperCase());
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
        synchronized(typeMap) {
            if (typeMap.containsKey(typeName)) {
                return typeMap.get(typeName);
            }
            
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

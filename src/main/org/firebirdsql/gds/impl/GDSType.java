/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl;

import org.jspecify.annotations.NullUnmarked;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Type-safe "enumeration" of the GDS types registered in the system. 
 */
// TODO Nullability may need to be rethought and further refined
@NullUnmarked
public final class GDSType implements Serializable {

    /*
     NOTE: We currently synchronize on typeMap in registerType (r/w), but not getType (r only). The chance of this
     causing issues are slim to none as generally writes should only occur while loading GDSFactory. Otherwise, writes
     can only occur when explicitly registering plugins with GDSFactory.registerPlugin or deserializing GDSType when
     the name is not already registered. The chances of this occurring are slim to none, and the expectation is that
     serializing registerType is sufficient protection in general.
    */
    private static final Map<String, GDSType> typeMap = new HashMap<>();
    @Serial
    private static final long serialVersionUID = 817804953480527534L;

    // DO NOT REMOVE: needed to initiate static initialization of the GDSFactory
    static {
        //noinspection ResultOfMethodCallIgnored
        GDSFactory.getDefaultGDSType();
    }

    /**
     * Factory method for instances of this class. There's only three possible
     * instances of this class, however linking to them directly is not always
     * possible and desirable (for example when type is specified in configuration).
     * This method gives the possibility to translate string representation of
     * the type into correct type instance.
     * 
     * @param type string representation of the type to match (matching is case-insensitive). Possible values are:
     * <ul>
     * <li>{@code "PURE_JAVA"} or {@code "TYPE4"} for pure Java (type 4)
     * driver version;
     * <li>{@code "NATIVE"} or {@code "TYPE2"} for type 2 JDBC driver
     * that will use Firebird client library to access the database using JNI 
     * link.
     * <li>{@code "EMBEDDED"} for type 2 JDBC driver that will use embedded
     * version of the server to perform database-related operations. 
     * </ul>
     * 
     * @return instance of {@link GDSType} corresponding to the specified
     * string representation or {@code null} if no match could be found.
     */
    // TODO should type accept null?
    public static GDSType getType(String type) {
        if (type == null) {
            return null;
        }
        return typeMap.get(type.toUpperCase(Locale.ROOT));
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
        // NOTE: See also comment on typeMap
        synchronized (typeMap) {
            return typeMap.computeIfAbsent(typeName.toUpperCase(Locale.ROOT), GDSType::new);
        }
    }
    
    private GDSType(String name) {
        this.name = name;
    }

    @Serial
    private Object readResolve() {
        return registerType(requireNonNull(name, "name"));
    }

    @Override
    public String toString() {
        return name;
    }

    private final String name;
}

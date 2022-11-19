/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps Firebird privilege codes (from {@code RDB$USER_PRIVILEGES.RDB$PRIVILEGE}) to a privilege name.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class PrivilegeMapping {

    private static final Map<String, String> PRIVILEGE_MAPPING;
    static {
        Map<String, String> tempMapping = new HashMap<>();
        // NOTE: 'A' doesn't seem to be used by Firebird (maybe in older versions?)
        tempMapping.put("A", "ALL");
        tempMapping.put("D", "DELETE");
        tempMapping.put("I", "INSERT");
        tempMapping.put("R", "REFERENCES");
        tempMapping.put("S", "SELECT");
        tempMapping.put("U", "UPDATE");

        tempMapping.put("C", "CREATE");
        tempMapping.put("L", "ALTER");
        tempMapping.put("O", "DROP");
        
        // Executing procedures/functions (unused in JDBC metadata)
        tempMapping.put("X", "EXECUTE");
        // Usage of object (unused in JDBC metadata)
        tempMapping.put("G", "USAGE");
        // User/object is a member of a ROLE (unused in JDBC metadata)
        tempMapping.put("M", "MEMBEROF");
        PRIVILEGE_MAPPING = Collections.unmodifiableMap(tempMapping);
    }

    private PrivilegeMapping() {
        // no instances
    }

    /**
     * Maps the (one character) Firebird privilege code to the equivalent JDBC privilege.
     *
     * @param firebirdPrivilege
     *         Firebird privilege character
     * @return privilege name or {@code "UNKNOWN"} when the privilege is not known
     */
    public static String mapPrivilege(String firebirdPrivilege) {
        return PRIVILEGE_MAPPING.getOrDefault(firebirdPrivilege, "UNKNOWN");
    }
}

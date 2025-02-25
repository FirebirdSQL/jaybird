// SPDX-FileCopyrightText: Copyright 2001-2023 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import java.util.Map;

/**
 * Maps Firebird privilege codes (from {@code RDB$USER_PRIVILEGES.RDB$PRIVILEGE}) to a privilege name.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class PrivilegeMapping {

    private static final Map<String, String> PRIVILEGE_MAPPING = Map.ofEntries(
            // NOTE: 'A' doesn't seem to be used by Firebird (maybe in older versions?)
            Map.entry("A", "ALL"),
            Map.entry("D", "DELETE"),
            Map.entry("I", "INSERT"),
            Map.entry("R", "REFERENCES"),
            Map.entry("S", "SELECT"),
            Map.entry("U", "UPDATE"),

            Map.entry("C", "CREATE"),
            Map.entry("L", "ALTER"),
            Map.entry("O", "DROP"),

            // Executing procedures/functions (unused in JDBC metadata)
            Map.entry("X", "EXECUTE"),
            // Usage of object (unused in JDBC metadata)
            Map.entry("G", "USAGE"),
            // User/object is a member of a ROLE (unused in JDBC metadata)
            Map.entry("M", "MEMBEROF"));

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

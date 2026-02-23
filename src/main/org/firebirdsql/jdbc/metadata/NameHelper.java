// SPDX-FileCopyrightText: Copyright 2023-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jaybird.util.ObjectReference;
import org.jspecify.annotations.Nullable;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Helper methods for generating object names.
 *
 * @author Mark Rotteveel
 * @since 6
 */
final class NameHelper {

    private NameHelper() {
        // no instances
    }

    /**
     * Generates a name for the {@code SPECIFIC_NAME} column of {@code getFunctions}, {@code getFunctionColumns},
     * {@code getProcedures} and {@code getProcedureColumns}.
     * <p>
     * According to the JDBC API documentation, the specific name <i>uniquely identifies this [routine]
     * within its schema</i>. In other words, the schema itself is not part of the specific name. Its intention is to
     * disambiguate overloads (which Firebird doesn't have). ISO 9075-11 (Schemata) only says it is the qualified
     * identifier of the routine. Be aware that the name we generate for a packaged routine does not comply with these
     * definitions.
     * </p>
     * <p>
     * The specific name is generated as follows:
     * </p>
     * <ul>
     *     <li>for non-packaged routines, {@code routineName} is returned as-is</li>
     *     <li>for packaged routines, {@code catalog} (package name) and {@code routineName} are transformed to quoted
     *     identifiers and separated by {@code .} (period)</li>
     * </ul>
     *
     * @param catalog
     *         generally {@code null}, or &mdash; when {@code useCatalogAsPackage = true} &mdash; an empty string (no
     *         package) or a package name
     * @param routineName
     *         name of the routine (procedure or function)
     * @return specific name
     * @since 7
     */
    static String toSpecificName(@Nullable String catalog, String routineName) {
        return isNullOrEmpty(catalog) ? routineName : ObjectReference.of(catalog, routineName).toString();
    }

}

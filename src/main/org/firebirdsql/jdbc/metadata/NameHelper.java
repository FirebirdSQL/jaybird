// SPDX-FileCopyrightText: Copyright 2023-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jaybird.util.ObjectReference;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Helper methods for generating object names.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@NullMarked
final class NameHelper {

    private NameHelper() {
        // no instances
    }

    /**
     * Generates a name for the {@code SPECIFIC_NAME} column of {@code getFunctions}, {@code getFunctionColumns},
     * {@code getProcedures} and {@code getProcedureColumns}.
     * <p>
     * The specific name is generated as follows:
     * </p>
     * <ul>
     * <li>
     *     <p>For Firebird versions <em>without</em> schema support</p>
     *     <ul>
     *         <li>for non-packaged routines, the {@code routineName}</li>
     *         <li>for packaged routines, both {@code catalog} (package name) and {@code routineName} are transformed to
     *         quoted identifiers and separated by {@code .} (period)</li>
     *     </ul>
     * </li>
     * <li>
     *     <p>For Firebird versions <em>with</em> schema support</p>
     *     <ul>
     *         <li>for non-packaged routines, both {@code schema} and {@code routineName} are transformed to
     *         quoted identifiers and separated by {@code .} (period)</li>
     *         <li>for packaged routines, {@code catalog} (package name), {@code schema} and {@code routineName} are
     *         transformed to quoted identifiers and separated by {@code .} (period)</li>
     *     </ul>
     * </li>
     * </ul>
     *
     * @param catalog
     *         generally {@code null}, or &mdash; when {@code useCatalogAsPackage = true} &mdash; an empty string (no
     *         package) or a package name
     * @param schema
     *         schema name, or {@code null} for Firebird versions without schema support, empty string is handled same
     *         as {@code null}
     * @param routineName
     *         name of the routine (procedure or function)
     * @return specific name
     * @since 7
     */
    static String toSpecificName(@Nullable String catalog, @Nullable String schema, String routineName) {
        if (isNullOrEmpty(catalog) && isNullOrEmpty(schema)) {
            // TODO Add schema support: consider quoting always for consistency
            return routineName;
        } else if (isNullOrEmpty(catalog)) {
            return ObjectReference.of(schema, routineName).toString();
        }
        // this order assumes the catalog actually represents the package name
        return ObjectReference.of(schema, catalog, routineName).toString();
    }

}

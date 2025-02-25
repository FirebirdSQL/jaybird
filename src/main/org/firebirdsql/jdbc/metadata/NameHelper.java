// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jdbc.QuoteStrategy;

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
     *
     * @param catalog
     *         generally {@code null}, or &mdash; when {@code useCatalogAsPackage = true} &mdash; an empty string (no
     *         package) or a package name
     * @param routineName
     *         name of the routine (procedure or function)
     * @return specific name: for non-packaged routines the {@code routineName}, of packaged routines, both
     * {@code catalog} (package name) and {@code routineName} are transformed to quoted identifiers and separated by
     * {@code .} (period)
     */
    static String toSpecificName(String catalog, String routineName) {
        if (catalog == null || catalog.isEmpty()) {
            return routineName;
        }
        var quoteStrategy = QuoteStrategy.DIALECT_3;
        // 5: 4 quotes + 1 separator
        var sb = new StringBuilder(catalog.length() + routineName.length() + 5);
        quoteStrategy.appendQuoted(catalog, sb).append('.');
        quoteStrategy.appendQuoted(routineName, sb);
        return sb.toString();
    }

}

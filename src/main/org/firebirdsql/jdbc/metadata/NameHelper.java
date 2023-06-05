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

import org.firebirdsql.jdbc.QuoteStrategy;

/**
 * @author Mark Rotteveel
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
        var quoteStrategy = QuoteStrategy.QUOTES;
        var sb = new StringBuilder(catalog.length() + routineName.length() + 5);
        quoteStrategy.appendQuoted(catalog, sb);
        sb.append('.');
        quoteStrategy.appendQuoted(routineName, sb);
        return sb.toString();
    }

}

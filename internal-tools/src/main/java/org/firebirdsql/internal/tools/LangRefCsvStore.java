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
package org.firebirdsql.internal.tools;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

/**
 * Firebird error store which generates the CSV as used by the Firebird Language Reference.
 * <p>
 * NOTE: This is not used for Jaybird, but as I also maintain the Firebird Language Reference, I reuse this code to
 * generate the file since the removal of MSG.FDB as part of the Firebird build.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
class LangRefCsvStore implements FirebirdErrorStore {

    private static final Set<Facility> DOCUMENTED_FACILITIES = unmodifiableSet(
            EnumSet.of(Facility.JRD, Facility.DSQL, Facility.DYN, Facility.SQLERR));

    private static final Comparator<FirebirdError> FIREBIRD_ERROR_COMPARATOR =
            Comparator.<FirebirdError>comparingInt(e -> Objects.requireNonNullElse(e.sqlCode(), 0)).reversed()
                    .thenComparingInt(FirebirdError::errorCode);

    private final SortedSet<FirebirdError> firebirdErrors = new TreeSet<>(FIREBIRD_ERROR_COMPARATOR);

    @Override
    public void addFirebirdError(FirebirdError firebirdError) {
        if (isDocumented(firebirdError)) {
            firebirdErrors.add(firebirdError);
        }
    }

    private boolean isDocumented(FirebirdError firebirdError) {
        return DOCUMENTED_FACILITIES.contains(firebirdError.facility()) && firebirdError.hasSymbolName();
    }

    @Override
    public void reset() {
        firebirdErrors.clear();
    }

    @Override
    public void save() throws IOException {
        // NOTE: Simple CSV output; no need to use CSV library
        try (var out = Files.newBufferedWriter(Path.of("firebird-errors.csv"), StandardCharsets.UTF_8)) {
            // NOTE: The U+00AD (soft hyphen) is used to allow a word-break in the SQLCODE when rendered in the docs
            out.write("SQL\u00ADCODE,GDSCODE,Symbol,Message Text\n");
            for (FirebirdError firebirdError : firebirdErrors) {
                out.append(String.valueOf(firebirdError.sqlCode() != null ? firebirdError.sqlCode() : 0))
                        .append(',')
                        .append(String.valueOf(firebirdError.errorCode()))
                        .append(',')
                        .append(requireNonNull(firebirdError.symbolName(), ""))
                        .append(",\"");
                escapeQuotes(firebirdError.message(), out);
                out.write("\"\n");
            }
        }
    }

    private static void escapeQuotes(String message, Writer out) throws IOException {
        if (message.indexOf('"') == -1) {
            out.write(message);
            return;
        }
        for (int idx = 0; idx < message.length(); idx++) {
            char ch = message.charAt(idx);
            if (ch == '"') out.append('"');
            out.append(ch);
        }
    }
}

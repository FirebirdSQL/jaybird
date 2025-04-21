// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.internal.tools;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.firebirdsql.internal.tools.MessageConverter.toNetProviderMessageFormat;

/**
 * Generates the list of error messages as used by the Firebird .NET Provider, class {@code FirebirdSql.Data.Common.IscErrorMessages}.
 *
 * @author Mark Rotteveel
 * @since 7
 */
class NetProviderStore implements FirebirdErrorStore {

    private static final Comparator<FirebirdError> FIREBIRD_ERROR_COMPARATOR =
            Comparator.comparingInt(FirebirdError::errorCode);

    private final SortedSet<FirebirdError> firebirdErrors = new TreeSet<>(FIREBIRD_ERROR_COMPARATOR);

    @Override
    public void addFirebirdError(FirebirdError firebirdError) {
        firebirdErrors.add(firebirdError);
    }

    @Override
    public void reset() {
        firebirdErrors.clear();
    }

    @Override
    public void save() throws IOException {
        try (var out = Files.newBufferedWriter(Path.of("IscErrorMessages-fragment.cs"), StandardCharsets.UTF_8)) {
            for (FirebirdError error : firebirdErrors) {
                writeError(out, error);
            }
        }
    }

    private void writeError(Writer out, FirebirdError error) throws IOException {
        out.append("\t{")
                .append(String.valueOf(error.errorCode())).append(", ")
                .append(enquoteCsharpString(toNetProviderMessageFormat(error.message())))
                .append("},");
        if (error.hasSymbolName()) {
            out.append("\t\t/* ").append(error.symbolName()).append(" */");
        }
        out.append("\n");
    }

    private String enquoteCsharpString(String message) {
        var builder = new StringBuilder(2 + (int) (message.length() * 1.1f));
        builder.append('"');
        for (char c : message.toCharArray()) {
            switch (c) {
            case '\r' -> { /* do nothing */ }
            case '\n' -> builder.append("\\n");
            case '\t' -> builder.append("\\t");
            case '"' -> builder.append("\\\"");
            case '\\' -> builder.append("\\\\");
            default -> builder.append(c);
            }
        }
        builder.append('"');
        return builder.toString();
    }
}

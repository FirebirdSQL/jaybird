// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jaybird.parser.FirebirdReservedWords;
import org.firebirdsql.jaybird.parser.SearchPathExtractor;
import org.firebirdsql.jaybird.parser.SqlParser;
import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Helpers for working with identifiers.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public final class SearchPathHelper {

    /**
     * Parses the elements of the search path to a list of <em>unquoted</em> schema names.
     *
     * @param searchPath
     *         comma-separated search path, with &mdash; optionally &mdash; quoted schema names
     * @return list of unquoted schema names, or empty if {@code searchPath} is {@code null}, blank or an invalid search
     * path (e.g. not a comma-separated list of potential schema names, or unquoted schema names are reserved words)
     */
    public static List<String> parseSearchPath(@Nullable String searchPath) {
        if (searchPath == null || searchPath.isBlank()) return List.of();
        var extractor = new SearchPathExtractor();
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(extractor)
                .of(searchPath)
                .parse();
        return extractor.getSearchPathList();
    }

    /**
     * Creates a search path from {@code searchPathList}.
     *
     * @param searchPathList
     *         list of unquoted schema names, blank values are ignored
     * @param quoteStrategy
     *         quote strategy
     * @return comma and space separated search path, quoted according to {@code quoteStrategy}
     */
    public static String toSearchPath(List<String> searchPathList, QuoteStrategy quoteStrategy) {
        if (searchPathList.isEmpty()) return "";
        // Assume each entry takes 15 characters, including quotes and separators
        var sb = new StringBuilder(searchPathList.size() * 15);
        for (String schema : searchPathList) {
            if (schema.isBlank()) continue;
            quoteStrategy.appendQuoted(schema, sb).append(", ");
        }
        // Remove last separator
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

}

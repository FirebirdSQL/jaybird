// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.util.SearchPathHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Token visitor to extract a search path to a list of unquoted schema names.
 * <p>
 * This visitor is written for the needs of {@link SearchPathHelper#parseSearchPath(String)}, and
 * may not be generally usable.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 7
 */
public final class SearchPathExtractor implements TokenVisitor {

    private final List<String> identifiers = new ArrayList<>();
    @Nullable
    private Token previousToken;

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        if (token.isWhitespaceOrComment()) return;
        try {
            extractIdentifier(token, visitorRegistrar);
        } finally {
            previousToken = token;
        }
    }

    private void extractIdentifier(Token token, VisitorRegistrar visitorRegistrar) {
        if (isPreviousTokenSeparator()) {
            if (token instanceof QuotedIdentifierToken quotedIdentifier) {
                identifiers.add(quotedIdentifier.name());
            } else if (token instanceof GenericToken identifier && identifier.isValidIdentifier()) {
                // Firebird returns the search path with quoted identifiers, but this way we can also parse unquoted
                // values (e.g. user-provided)
                identifiers.add(identifier.text().toUpperCase(Locale.ROOT));
            } else {
                // Unexpected token, end parsing
                visitorRegistrar.removeVisitor(this);
                identifiers.clear();
            }
        } else if (!(token instanceof CommaToken && isPreviousTokenIdentifier())) {
            // Unexpected token, end parsing
            visitorRegistrar.removeVisitor(this);
            identifiers.clear();
        }
    }

    private boolean isPreviousTokenIdentifier() {
        return previousToken instanceof QuotedIdentifierToken || previousToken instanceof GenericToken;
    }

    private boolean isPreviousTokenSeparator() {
        return previousToken instanceof CommaToken || previousToken == null;
    }

    @Override
    public void complete(VisitorRegistrar visitorRegistrar) {
        if (!isPreviousTokenIdentifier()) {
            // Unexpected token, clear list; for most cases, we already cleared the list, except if last was CommaToken
            identifiers.clear();
        }
    }

    /**
     * The extracted search path list, or empty if not parsed or if the parsed text was not a valid search path list.
     *
     * @return immutable list of unquoted search path entries
     */
    public List<String> getSearchPathList() {
        return List.copyOf(identifiers);
    }

}
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
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * A token is an individual element of a SQL statement.
 * <p>
 * The available implementations of {@code Token} are primarily guided by the implementation of the tokenization
 * and the needs of the parser and included visitors. It does not distinguish all types of tokens. For example there is
 * {@link QuotedIdentifierToken} because the tokenization needs handling for
 * quoted identifiers, while a normal identifier is a {@link GenericToken}, because that is handled by the fallback
 * tokenization after checking for all other types. On the other hand, open and close curly braces, square brackets and
 * parentheses each have their own type, as the parser may need this to find nested contexts.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public interface Token {

    /**
     * Token text.
     *
     * @return the text of the token; this is the original text from the source
     * @see #textAsCharSequence()
     */
    String text();

    /**
     * Token text as {@link CharSequence}.
     * <p>
     * Default implementation returns {@link #text()}. As an optimization, implementations may return their contained
     * {@code CharSequence} to avoid unnecessary conversion to string.
     * </p>
     *
     * @return the text of the token; this is the original text from the source
     * @see #text()
     */
    default CharSequence textAsCharSequence() {
        return text();
    }

    /**
     * Appends the current token to the supplied String builder.
     *
     * @param sb
     *         String builder to append to
     */
    default void appendTo(StringBuilder sb) {
        sb.append(text());
    }

    /**
     * Token position.
     *
     * @return 0-based position of the occurrence of this token in the source (the first character)
     */
    int position();

    /**
     * Token text length.
     *
     * @return Length of the token text
     */
    int length();

    /**
     * @return {@code true} if this token is whitespace or a comment, {@code false} for all other tokens
     */
    default boolean isWhitespaceOrComment() {
        return false;
    }

    /**
     * Case-insensitive equality of this tokens text using an equivalent of {@link String#equalsIgnoreCase(String)}.
     *
     * @param tokenText
     *         Token text to compare
     * @return {@code true} if {@code tokenText} is equal - ignoring case - to the text of this token,
     * {@code false} otherwise
     */
    default boolean equalsIgnoreCase(String tokenText) {
        return text().equalsIgnoreCase(tokenText);
    }

    /**
     * Detects if the token is valid as an identifier (ignoring length constraints).
     * <p>
     * This will always return {@code false} for {@link ReservedToken} or other
     * specialised tokens (e.g. {@link OperatorToken} with {@code IS} or {@code LIKE})
     * that can't occur as an identifier.
     * </p>
     *
     * @return {@code true} if the token is valid as an identifier, {@code false} otherwise
     */
    default boolean isValidIdentifier() {
        return false;
    }

}

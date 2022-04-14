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
package org.firebirdsql.jdbc.parser;

import java.util.stream.Stream;

@SuppressWarnings("unused")
final class TokenSources {

    private TokenSources() {
        throw new AssertionError("no instances");
    }

    public static boolean isParenthesisToken(Token token) {
        return token instanceof ParenthesisOpen || token instanceof ParenthesisClose;
    }

    public static Stream<Token> nonParenthesisTokens() {
        return tokensOfAllTypes()
                .filter(token -> !isParenthesisToken(token));
    }

    public static Stream<Token> parenthesisTokens() {
        return tokensOfAllTypes()
                .filter(TokenSources::isParenthesisToken);
    }

    public static boolean isSquareBracketToken(Token token) {
        return token instanceof SquareBracketOpen || token instanceof SquareBracketClose;
    }

    public static Stream<Token> nonSquareBracketTokens() {
        return tokensOfAllTypes()
                .filter(token -> !isSquareBracketToken(token));
    }

    public static Stream<Token> squareBracketTokens() {
        return tokensOfAllTypes()
                .filter(TokenSources::isSquareBracketToken);
    }

    public static boolean isCurlyBraceToken(Token token) {
        return token instanceof CurlyBraceOpen || token instanceof CurlyBraceClose;
    }

    public static Stream<Token> nonCurlyBraceTokens() {
        return tokensOfAllTypes()
                .filter(token -> !isCurlyBraceToken(token));
    }

    public static Stream<Token> curlyBraceTokens() {
        return tokensOfAllTypes()
                .filter(TokenSources::isCurlyBraceToken);
    }

    public static boolean isStandardOpenCloseToken(Token token) {
        return isParenthesisToken(token) || isSquareBracketToken(token) || isCurlyBraceToken(token);
    }

    public static Stream<Token> standardOpenCloseTokens() {
        return tokensOfAllTypes()
                .filter(TokenSources::isStandardOpenCloseToken);
    }

    public static boolean isOpenCloseToken(Token token) {
        return token instanceof OpenToken || token instanceof CloseToken;
    }

    public static Stream<Token> nonOpenCloseTokens() {
        return tokensOfAllTypes()
                .filter(token -> !isOpenCloseToken(token));
    }

    public static Stream<Token> tokensOfAllTypes() {
        return Stream.of(new ColonToken(0), new CommaToken(1), new CommentToken(2, "/* ... */"),
                new CurlyBraceOpen(3), new CurlyBraceClose(4), new NumericLiteralToken(5, "1"),
                new OperatorToken(6, "+"), new GenericToken(7, "A"), new ParenthesisOpen(8),
                new ParenthesisClose(9), new PeriodToken(10), new PositionalParameterToken(11),
                new QuotedIdentifierToken(12, "A"), new SemicolonToken(13), new SquareBracketOpen(14),
                new SquareBracketClose(15), new StringLiteralToken(16, "'A'"),
                new WhitespaceToken(17, " "));
    }

}

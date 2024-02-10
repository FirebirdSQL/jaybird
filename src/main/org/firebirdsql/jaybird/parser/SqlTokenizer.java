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

import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.String.format;

/**
 * Converts a SQL statement into tokens.
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @since 5
 */
@InternalApi
public final class SqlTokenizer implements Iterator<Token>, AutoCloseable {

    private static final int EOF = -1;

    private final String src;
    private final ReservedWords reservedWords;
    private int pos = 0;
    private Token next;

    private SqlTokenizer(String src, ReservedWords reservedWords) {
        this.src = src;
        this.reservedWords = reservedWords;
    }

    /**
     * Tokenize with a supplier of reserved words.
     *
     * @param reservedWords
     *         Reserved words
     * @return builder to complete initialization of a tokenizer
     */
    public static Builder withReservedWords(ReservedWords reservedWords) {
        return new Builder(reservedWords);
    }

    @Override
    public boolean hasNext() {
        if (isClosed()) return false;
        if (next == null) {
            next = nextToken();
        }
        return next != null;
    }

    @Override
    public Token next() {
        Token nextToken = this.next;
        if (nextToken != null) {
            this.next = null;
        } else {
            nextToken = nextToken();
        }
        if (nextToken != null) {
            return nextToken;
        } else {
            throw new NoSuchElementException("No more tokens");
        }
    }

    @Override
    public void close() {
        pos = EOF;
    }

    private boolean isClosed() {
        return pos == EOF;
    }

    private int read() {
        int length = src.length();
        if (pos < length) {
            return src.charAt(pos++);
        } else {
            pos = length;
            return EOF;
        }
    }

    private char requireChar() {
        int c = read();
        if (c == EOF) {
            int originalPosition = pos;
            close();
            throw new UnexpectedEndOfInputException(
                    format("Reached end of input at position %d while character was read", originalPosition));
        }
        return (char) c;
    }

    private void skip() {
        if (pos < src.length()) {
            pos++;
        } else {
            throw new UnexpectedEndOfInputException(
                    format("Reached end of input at position %d while skipping", pos));
        }
    }

    private void skip(int amount) {
        int length = src.length();
        if (pos + amount <= length) {
            pos += amount;
        } else {
            pos = length;
            throw new UnexpectedEndOfInputException(
                    format("Reached end of input after position %d while skipping", pos));
        }
    }

    private void unread(int c) {
        // We don't check if the 'unread' character is actually at this position
        if (c != EOF) {
            pos--;
        }
    }

    private void unread(int[] chars, int lastIndex) {
        while (lastIndex >= 0) {
            unread(chars[lastIndex--]);
        }
    }

    private int peek() {
        return pos < src.length() ? src.charAt(pos) : EOF;
    }

    @SuppressWarnings({ "java:S1479", "java:S3776" })
    private Token nextToken() {
        if (isClosed()) return null;
        int start = pos;
        int c = read();
        return switch (c) {
        case EOF -> {
            close();
            yield null;
        }
        case '\t', '\n', '\r', ' ' -> readWhitespaceToken(start);
        case '(' -> new ParenthesisOpen(start);
        case ')' -> new ParenthesisClose(start);
        // curly braces aren't part of the SQL syntax, but of the JDBC escape syntax
        case '{' -> new CurlyBraceOpen(start);
        case '}' -> new CurlyBraceClose(start);
        case '[' -> new SquareBracketOpen(start);
        case ']' -> new SquareBracketClose(start);
        case ';' -> new SemicolonToken(start);
        case ',' -> new CommaToken(start);
        case '.' -> {
            if (isDigit(peek())) {
                yield readNumericLiteral(start, '.');
            }
            yield new PeriodToken(start);
        }
        case '+',
                '*', // Can also signify 'all' (as in select * or select alias.*)
                '=' -> new OperatorToken(start, src, start, pos);
        case '-' -> {
            if (peek() == '-') {
                yield readLineComment(start);
            }
            yield new OperatorToken(start, src, start, pos);
        }
        case '/' -> {
            if (peek() == '*') {
                yield readBlockComment(start);
            }
            yield new OperatorToken(start, src, start, pos);
        }
        case '<' -> {
            int cNext = read();
            yield switch (cNext) {
                case '>', '=' -> new OperatorToken(start, src, start, pos);
                default -> {
                    unread(cNext);
                    yield new OperatorToken(start, src, start, pos);
                }
            };
        }
        case '>' -> {
            int cNext = read();
            if (cNext == '=') {
                yield new OperatorToken(start, src, start, pos);
            }
            unread(cNext);
            yield new OperatorToken(start, src, start, pos);
        }
        case '!', '~', '^'-> {
            int cNext = read();
            yield switch (cNext) {
                case '=', '>', '<' -> new OperatorToken(start, src, start, pos);
                default -> {
                    unread(cNext);
                    // shouldn't occur, but handle as singular operator
                    yield new OperatorToken(start, src, start, pos);
                }
            };
        }
        case '|' -> {
            int cNext = read();
            if (cNext == '|') {
                yield new OperatorToken(start, src, start, pos);
            }
            unread(cNext);
            // shouldn't occur, but handle as singular operator
            yield new OperatorToken(start, src, start, pos);
        }
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> readNumericLiteral(start, (char) c);
        case '\'' -> readStringLiteral(start);
        case 'a', 'A' -> {
            if (detectAnd()) {
                yield readTokenByLength(start, 2, OperatorToken::new);
            }
            yield readOtherToken(start);
        }
        case 'f', 'F' -> {
            if (detectFalse()) {
                yield readTokenByLength(start, 4, BooleanLiteralToken::falseToken);
            }
            yield readOtherToken(start);
        }
        case 'i', 'I' -> {
            if (detectIs()) {
                yield readTokenByLength(start, 1, OperatorToken::new);
            }
            yield readOtherToken(start);
        }
        case 'l', 'L' -> {
            if (detectLike()) {
                yield readTokenByLength(start, 3, OperatorToken::new);
            }
            yield readOtherToken(start);
        }
        case 'n', 'N' -> {
            if (detectNull()) {
                yield readTokenByLength(start, 3, NullLiteralToken::new);
            }
            if (detectNot()) {
                yield readTokenByLength(start, 2, OperatorToken::new);
            }
            yield readOtherToken(start);
        }
        case 'o', 'O' -> {
            if (detectOr()) {
                yield readTokenByLength(start, 1, OperatorToken::new);
            }
            yield readOtherToken(start);
        }
        case 'q', 'Q' -> {
            if (peek() == '\'') {
                yield readQStringLiteral(start);
            }
            yield readOtherToken(start);
        }
        case 't', 'T' -> {
            if (detectTrue()) {
                yield readTokenByLength(start, 3, BooleanLiteralToken::trueToken);
            }
            yield readOtherToken(start);
        }
        case 'u', 'U' -> {
            if (detectUnknown()) {
                yield readTokenByLength(start, 6, BooleanLiteralToken::unknownToken);
            }
            yield readOtherToken(start);
        }
        case 'x', 'X' -> {
            int cNext = read();
            if (cNext == '\'') {
                yield readHexStringLiteral(start);
            }
            unread(cNext);
            yield readOtherToken(start);
        }
        case '?' -> new PositionalParameterToken(start);
        // signals named parameter or array dimension
        case ':' -> new ColonToken(start);
        // signals quoted identifier, or a string literal in dialect 1
        case '"' -> readQuotedIdentifier(start);
        default -> readOtherToken(start);
        };
    }

    private static final char[][] UNKNOWN_SUFFIX =
            { { 'n', 'N' }, { 'k', 'K' }, { 'n', 'N' }, { 'o', 'O' }, { 'w', 'W' }, { 'n', 'N' } };

    private boolean detectUnknown() {
        return detectToken(UNKNOWN_SUFFIX);
    }

    private static final char[][] TRUE_SUFFIX = { { 'r', 'R' }, { 'u', 'U' }, { 'e', 'E' } };

    private boolean detectTrue() {
        return detectToken(TRUE_SUFFIX);
    }

    private static final char[][] OR_SUFFIX = { { 'r', 'R' } };

    private boolean detectOr() {
        return detectToken(OR_SUFFIX);
    }

    private static final char[][] NOT_SUFFIX = { { 'o', 'O' }, { 't', 'T' } };

    private boolean detectNot() {
        return detectToken(NOT_SUFFIX);
    }

    private static final char[][] NULL_SUFFIX = { { 'u', 'U' }, { 'l', 'L' }, { 'l', 'L' } };

    private boolean detectNull() {
        return detectToken(NULL_SUFFIX);
    }

    private static final char[][] LIKE_SUFFIX = { { 'i', 'I' }, { 'k', 'K' }, { 'e', 'E' } };

    private boolean detectLike() {
        return detectToken(LIKE_SUFFIX);
    }

    private static final char[][] IS_SUFFIX = { { 's', 'S' } };

    private boolean detectIs() {
        return detectToken(IS_SUFFIX);
    }

    private static final char[][] FALSE_SUFFIX = { { 'a', 'A' }, { 'l', 'L' }, { 's', 'S' }, { 'e', 'E' } };

    private boolean detectFalse() {
        return detectToken(FALSE_SUFFIX);
    }

    private static final char[][] AND_SUFFIX = { { 'n', 'N' }, { 'd', 'D' } };

    private boolean detectAnd() {
        return detectToken(AND_SUFFIX);
    }

    private WhitespaceToken readWhitespaceToken(int start) {
        int c;
        //noinspection StatementWithEmptyBody
        while (isWhitespace(c = read())) {
            // consume whitespace
        }
        unread(c);
        return new WhitespaceToken(start, src, start, pos);
    }

    private CommentToken readLineComment(int start) {
        // Skip second - of --
        skip();
        int c;
        //noinspection StatementWithEmptyBody
        while (!isEndOfLine(c = read())) {
            // consume remainder of line
        }
        unread(c);
        return new CommentToken(start, src, start, pos);
    }

    private CommentToken readBlockComment(int start) {
        // Skip * of /*
        skip();
        // TODO Consider end of stream to complete comment
        for (char c = requireChar(); ; c = requireChar()) {
            // NOTE: Firebird does not support 'nested' block comments, so neither does this parser
            if (c == '*' && peek() == '/') {
                skip();
                break;
            }
        }
        return new CommentToken(start, src, start, pos);
    }

    private NumericLiteralToken readNumericLiteral(int start, char firstChar) {
        if (firstChar == '0' && peek() == 'x') {
            return continueBinaryNumericLiteral(start);
        }
        boolean beforeDecimalSeparator = firstChar != '.';
        int c;
        while (isDigit(c = read()) || c == '.' && beforeDecimalSeparator) {
            if (c == '.') {
                beforeDecimalSeparator = false;
            }
        }
        if (c == 'e' || c == 'E') {
            c = read();
            if (c != '+' && c != '-' && !isDigit(c)) {
                unread(c);
            }
            // We're allowing invalid literals like 1.0E or 1.0E+
            //noinspection StatementWithEmptyBody
            while (isDigit(c = read())) {
                // consume exponent
            }
        }
        unread(c);
        return new NumericLiteralToken(start, src, start, pos);
    }

    private NumericLiteralToken continueBinaryNumericLiteral(int start) {
        // skip the x/X (already checked by caller using peek())
        skip();
        int c;
        //noinspection StatementWithEmptyBody
        while (isHexDigit(c = read())) {
            // consume binary numeric literal
        }
        unread(c);
        return new NumericLiteralToken(start, src, start, pos);
    }

    private StringLiteralToken readHexStringLiteral(int start) {
        // We're not attempting to verify if it is a valid hex string literal
        // At this point, x' has already been consumed
        return readStringLiteral(start);
    }

    private StringLiteralToken readStringLiteral(int start) {
        int c;
        while ((c = requireChar()) != '\'' || peek() == '\'') {
            if (c == '\'') {
                skip();
            }
        }
        return new StringLiteralToken(start, src, start, pos);
    }

    private StringLiteralToken readQStringLiteral(int start) {
        skip();
        char startToken = requireChar();
        char endToken = computeCloseQuote(startToken);
        for (char c = requireChar(); ; c = requireChar()) {
            if (c == endToken && peek() == '\'') {
                skip();
                break;
            }
        }
        return new StringLiteralToken(start, src, start, pos);
    }

    private char computeCloseQuote(char specialChar) {
        return switch (specialChar) {
            case '[' -> ']';
            case '(' -> ')';
            case '{' -> '}';
            case '<' -> '>';
            default -> specialChar;
        };
    }

    private QuotedIdentifierToken readQuotedIdentifier(int start) {
        int c;
        while ((c = requireChar()) != '"' || peek() == '"') {
            if (c == '"') {
                skip();
            }
        }
        return new QuotedIdentifierToken(start, src, start, pos);
    }

    private Token readOtherToken(int start) {
        int c;
        //noinspection StatementWithEmptyBody
        while (!isNormalTokenBoundary(c = read())) {
            // consume remainder of normal token
        }
        unread(c);
        int end = pos;
        CharSequence tokenText = CharBuffer.wrap(src, start, end);
        if (reservedWords.isReservedWord(tokenText)) {
            return new ReservedToken(start, tokenText);
        }
        return new GenericToken(start, tokenText);
    }

    private <T extends Token> T readTokenByLength(int start, int remainingChars, TokenConstructor<T> tokenConstructor) {
        skip(remainingChars);
        return tokenConstructor.construct(start, src, start, pos);
    }

    @SuppressWarnings("java:S1119")
    private boolean detectToken(char[][] expectedChars) {
        int maxChars = expectedChars.length;
        int[] readChars = new int[maxChars + 1];
        int idx = -1;
        try {
            matchLoop:
            while (++idx < maxChars) {
                int currentChar = readChars[idx] = read();
                for (char expectedChar : expectedChars[idx]) {
                    if (currentChar == expectedChar) {
                        continue matchLoop;
                    }
                }
                // no match
                return false;
            }
            return isNormalTokenBoundary(readChars[idx] = read());
        } finally {
            unread(readChars, idx);
        }
    }

    private static boolean isNormalTokenBoundary(int c) {
        return switch (c) {
            case EOF, '\t', '\n', '\r', ' ', '(', ')', '{', '}', '[', ']', '\'', '"', ':', ';', '.', '+', '-', '/', '*',
                    '=', '>', '<', '~', '^', '!', '?' -> true;
            default -> false;
        };
    }

    private static boolean isWhitespace(int c) {
        return switch (c) {
            case '\t', '\n', '\r', ' ' -> true;
            default -> false;
        };
    }

    private static boolean isEndOfLine(int c) {
        return switch (c) {
            case EOF, '\n', '\r' -> true;
            default -> false;
        };
    }

    private static boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private static boolean isHexDigit(int c) {
        return isDigit(c) || 'A' <= c && c <= 'F' || 'a' <= c && c <= 'f';
    }

    @FunctionalInterface
    private interface TokenConstructor<T extends Token> {

        T construct(int pos, CharSequence src, int start, int end);

    }

    public static final class Builder {

        private final ReservedWords reservedWords;

        private Builder(ReservedWords reservedWords) {
            this.reservedWords = reservedWords;
        }

        /**
         * Creates a SQL tokenizer with the reserved words of this builder.
         *
         * @param statementText
         *         SQL statement
         * @return SQL tokenizer
         */
        public SqlTokenizer of(String statementText) {
            return new SqlTokenizer(statementText, reservedWords);
        }

    }

}

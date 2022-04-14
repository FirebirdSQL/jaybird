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

import org.firebirdsql.util.InternalApi;

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

    private final String src;
    private final char[] srcChars;
    private final ReservedWords reservedWords;
    private int pos = 0;
    private Token next;

    private SqlTokenizer(String src, ReservedWords reservedWords) {
        this.src = src;
        this.srcChars = src.toCharArray();
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
        pos = -1;
    }

    private boolean isClosed() {
        return pos == -1;
    }

    private int read() {
        if (pos < srcChars.length) {
            return srcChars[pos++];
        } else {
            pos = srcChars.length;
            return -1;
        }
    }

    private char requireChar() {
        int c = read();
        if (c == -1) {
            int originalPosition = pos;
            close();
            throw new UnexpectedEndOfInputException(
                    format("Reached end of input at position %d while character was read", originalPosition));
        }
        return (char) c;
    }

    private void skip() {
        if (pos < srcChars.length) {
            pos++;
        } else {
            throw new UnexpectedEndOfInputException(
                    format("Reached end of input at position %d while skipping", pos));
        }
    }

    private void skip(int amount) {
        if (pos + amount <= srcChars.length) {
            pos += amount;
        } else {
            pos = srcChars.length;
            throw new UnexpectedEndOfInputException(
                    format("Reached end of input after position %d while skipping", pos));
        }
    }

    private void unread(int c) {
        // We don't check if the 'unread' character is actually at this position
        if (c != -1) {
            pos--;
        }
    }

    private void unread(int[] chars, int lastIndex) {
        while (lastIndex >= 0) {
            unread(chars[lastIndex--]);
        }
    }

    private int peek() {
        return pos < srcChars.length ? srcChars[pos] : -1;
    }

    private Token nextToken() {
        if (isClosed()) return null;
        int start = pos;
        int c = read();
        switch (c) {
        case -1:
            close();
            return null;
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            return readWhitespaceToken(start);
        case '(':
            return new ParenthesisOpen(start);
        case ')':
            return new ParenthesisClose(start);
        // curly braces aren't part of the SQL syntax, but of the JDBC escape syntax
        case '{':
            return new CurlyBraceOpen(start);
        case '}':
            return new CurlyBraceClose(start);
        case '[':
            return new SquareBracketOpen(start);
        case ']':
            return new SquareBracketClose(start);
        case ';':
            return new SemicolonToken(start);
        case ',':
            return new CommaToken(start);
        case '.':
            if (isDigit(peek())) {
                return readNumericLiteral(start, '.');
            }
            return new PeriodToken(start);
        case '+':
        case '*': // Can also signify 'all' (as in select * or select alias.*)
        case '=':
            return new OperatorToken(start, srcChars, start, pos);
        case '-':
            if (peek() == '-') {
                return readLineComment(start);
            }
            return new OperatorToken(start, srcChars, start, pos);
        case '/':
            if (peek() == '*') {
                return readBlockComment(start);
            }
            return new OperatorToken(start, srcChars, start, pos);
        case '<': {
            int cNext = read();
            switch (cNext) {
            case '>':
            case '=':
                return new OperatorToken(start, srcChars, start, pos);
            default:
                unread(cNext);
                return new OperatorToken(start, srcChars, start, pos);
            }
        }
        case '>': {
            int cNext = read();
            if (cNext == '=') {
                return new OperatorToken(start, srcChars, start, pos);
            }
            unread(cNext);
            return new OperatorToken(start, srcChars, start, pos);
        }
        case '!':
        case '~':
        case '^': {
            int cNext = read();
            switch (cNext) {
            case '=':
            case '>':
            case '<':
                return new OperatorToken(start, srcChars, start, pos);
            default:
                unread(cNext);
                // shouldn't occur, but handle as singular operator
                return new OperatorToken(start, srcChars, start, pos);
            }
        }
        case '|': {
            int cNext = read();
            if (cNext == '|') {
                return new OperatorToken(start, srcChars, start, pos);
            }
            unread(cNext);
            // shouldn't occur, but handle as singular operator
            return new OperatorToken(start, srcChars, start, pos);
        }
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return readNumericLiteral(start, (char) c);
        case '\'':
            return readStringLiteral(start);
        case 'a':
        case 'A':
            if (detectAnd()) {
                return readTokenByLength(start, 2, OperatorToken::new);
            }
            return readOtherToken(start);
        case 'f':
        case 'F':
            if (detectFalse()) {
                return readTokenByLength(start, 4, BooleanLiteralToken::falseToken);
            }
            return readOtherToken(start);
        case 'i':
        case 'I':
            if (detectIs()) {
                return readTokenByLength(start, 1, OperatorToken::new);
            }
            return readOtherToken(start);
        case 'l':
        case 'L':
            if (detectLike()) {
                return readTokenByLength(start, 3, OperatorToken::new);
            }
            return readOtherToken(start);
        case 'n':
        case 'N':
            if (detectNull()) {
                return readTokenByLength(start, 3, NullLiteralToken::new);
            }
            if (detectNot()) {
                return readTokenByLength(start, 2, OperatorToken::new);
            }
            return readOtherToken(start);
        case 'o':
        case 'O':
            if (detectOr()) {
                return readTokenByLength(start, 1, OperatorToken::new);
            }
            return readOtherToken(start);
        case 'q':
        case 'Q':
            if (peek() == '\'') {
                return readQStringLiteral(start);
            }
            return readOtherToken(start);
        case 't':
        case 'T':
            if (detectTrue()) {
                return readTokenByLength(start, 3, BooleanLiteralToken::trueToken);
            }
            return readOtherToken(start);
        case 'u':
        case 'U':
            if (detectUnknown()) {
                return readTokenByLength(start, 6, BooleanLiteralToken::unknownToken);
            }
            return readOtherToken(start);
        case 'x':
        case 'X': {
            int cNext = read();
            if (cNext == '\'') {
                return readHexStringLiteral(start);
            }
            unread(cNext);
            return readOtherToken(start);
        }
        case '?':
            return new PositionalParameterToken(start);
        case ':':
            // signals named parameter or array dimension
            return new ColonToken(start);
        case '"':
            // or a string literal in dialect 1
            return readQuotedIdentifier(start);
        default:
            return readOtherToken(start);
        }
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
        }
        unread(c);
        return new WhitespaceToken(start, srcChars, start, pos);
    }

    private CommentToken readLineComment(int start) {
        // Skip second - of --
        skip();
        int c;
        //noinspection StatementWithEmptyBody
        while (!isEndOfLine(c = read())) {
        }
        unread(c);
        return new CommentToken(start, srcChars, start, pos);
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
        return new CommentToken(start, srcChars, start, pos);
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
            }
        }
        unread(c);
        return new NumericLiteralToken(start, srcChars, start, pos);
    }

    private NumericLiteralToken continueBinaryNumericLiteral(int start) {
        skip();
        int c;
        //noinspection StatementWithEmptyBody
        while (isHexDigit(c = read())) {
        }
        unread(c);
        return new NumericLiteralToken(start, srcChars, start, pos);
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
        return new StringLiteralToken(start, srcChars, start, pos);
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
        return new StringLiteralToken(start, srcChars, start, pos);
    }

    private char computeCloseQuote(char specialChar) {
        switch (specialChar) {
        case '[':
            return ']';
        case '(':
            return ')';
        case '{':
            return '}';
        case '<':
            return '>';
        default:
            return specialChar;
        }
    }

    private QuotedIdentifierToken readQuotedIdentifier(int start) {
        int c;
        while ((c = requireChar()) != '"' || peek() == '"') {
            if (c == '"') {
                skip();
            }
        }
        return new QuotedIdentifierToken(start, srcChars, start, pos);
    }

    private Token readOtherToken(int start) {
        int c;
        //noinspection StatementWithEmptyBody
        while (!isNormalTokenBoundary(c = read())) {
        }
        unread(c);
        int end = pos;
        String tokenText = src.substring(start, end);
        if (reservedWords.isReservedWord(tokenText)) {
            return new ReservedToken(start, tokenText);
        }
        return new GenericToken(start, tokenText);
    }

    private <T extends Token> T readTokenByLength(int start, int remainingChars, TokenConstructor<T> tokenConstructor) {
        skip(remainingChars);
        return tokenConstructor.construct(start, srcChars, start, pos);
    }

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
        switch (c) {
        case -1:
        case '\t':
        case '\n':
        case '\r':
        case ' ':
        case '(':
        case ')':
        case '{':
        case '}':
        case '[':
        case ']':
        case '\'':
        case '"':
        case ':':
        case ';':
        case '.':
        case '+':
        case '-':
        case '/':
        case '*':
        case '=':
        case '>':
        case '<':
        case '~':
        case '^':
        case '!':
        case '?':
            return true;
        default:
            return false;
        }
    }

    private static boolean isWhitespace(int c) {
        switch (c) {
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            return true;
        default:
            return false;
        }
    }

    private static boolean isEndOfLine(int c) {
        switch (c) {
        case -1:
        case '\n':
        case '\r':
            return true;
        default:
            return false;
        }
    }

    private static boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private static boolean isHexDigit(int c) {
        return isDigit(c) || 'A' <= c && c <= 'F' || 'a' <= c && c <= 'f';
    }

    @FunctionalInterface
    private interface TokenConstructor<T extends Token> {

        T construct(int pos, char[] srcChars, int start, int end);

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

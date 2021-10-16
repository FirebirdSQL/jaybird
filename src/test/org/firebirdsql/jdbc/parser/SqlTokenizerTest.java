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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SqlTokenizerTest {

    @Test
    void emptyString_hasNoToken() {
        SqlTokenizer tokenizer = SqlTokenizer.withReservedWords(FirebirdReservedWords.latest()).of("");

        assertThat(tokenizer)
                .describedAs("expected no token").isExhausted();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(tokenizer::next);
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "\t", "\r", "\n", "\r\n\r\n", " \t\r\n " })
    void whitespaceString_returnsSingleToken(String whitespaceString) {
        expectSingleToken(whitespaceString, new WhitespaceToken(0, whitespaceString));
    }

    @ParameterizedTest
    @MethodSource("singularTokensProvider")
    void singularTokens(String input, Token expectedToken) {
        expectSingleToken(input, expectedToken);
        assertThat(expectedToken.text()).isEqualTo(input);
    }

    static Stream<Arguments> singularTokensProvider() {
        return Stream.of(
                arguments("(", new ParenthesisOpen(0)),
                arguments(")", new ParenthesisClose(0)),
                arguments("{", new CurlyBraceOpen(0)),
                arguments("}", new CurlyBraceClose(0)),
                arguments("[", new SquareBracketOpen(0)),
                arguments("]", new SquareBracketClose(0)),
                arguments(";", new SemicolonToken(0)),
                arguments(",", new CommaToken(0)),
                arguments(".", new PeriodToken(0)),
                arguments("+", new OperatorToken(0, "+")),
                arguments("-", new OperatorToken(0, "-")),
                arguments("/", new OperatorToken(0, "/")),
                arguments("*", new OperatorToken(0, "*")),
                arguments("=", new OperatorToken(0, "=")),
                arguments("<>", new OperatorToken(0, "<>")),
                arguments("!=", new OperatorToken(0, "!=")),
                arguments("~=", new OperatorToken(0, "~=")),
                arguments("^=", new OperatorToken(0, "^=")),
                arguments(">", new OperatorToken(0, ">")),
                arguments("<", new OperatorToken(0, "<")),
                arguments(">=", new OperatorToken(0, ">=")),
                arguments("<=", new OperatorToken(0, "<=")),
                arguments("!>", new OperatorToken(0, "!>")),
                arguments("!<", new OperatorToken(0, "!<")),
                arguments("~>", new OperatorToken(0, "~>")),
                arguments("~<", new OperatorToken(0, "~<")),
                arguments("^>", new OperatorToken(0, "^>")),
                arguments("^<", new OperatorToken(0, "^<")),
                arguments("||", new OperatorToken(0, "||")),
                arguments(":", new ColonToken(0)),
                arguments("?", new PositionalParameterToken(0)),
                arguments("and", new OperatorToken(0, "and")),
                arguments("AND", new OperatorToken(0, "AND")),
                arguments("or", new OperatorToken(0, "or")),
                arguments("OR", new OperatorToken(0, "OR")),
                arguments("true", BooleanLiteralToken.trueToken(0, "true")),
                arguments("TRUE", BooleanLiteralToken.trueToken(0, "TRUE")),
                arguments("false", BooleanLiteralToken.falseToken(0, "false")),
                arguments("FALSE", BooleanLiteralToken.falseToken(0, "FALSE")),
                arguments("unknown", BooleanLiteralToken.unknownToken(0, "unknown")),
                arguments("UNKNOWN", BooleanLiteralToken.unknownToken(0, "UNKNOWN")),
                arguments("null", new NullLiteralToken(0, "null")),
                arguments("NULL", new NullLiteralToken(0, "NULL")),
                arguments("select", new ReservedToken(0, "select")),
                arguments("SELECT", new ReservedToken(0, "SELECT")),
                arguments("xmp", new GenericToken(0, "xmp")),
                arguments("qualifier", new GenericToken(0, "qualifier")),
                // technically not valid tokens, but for tokenizing simplicity
                arguments("!", new OperatorToken(0, "!")),
                arguments("~", new OperatorToken(0, "~")),
                arguments("^", new OperatorToken(0, "^")),
                arguments("|", new OperatorToken(0, "|"))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "''", "'a'", "'a'''", "'a''b'" })
    void stringLiteralToken(String input) {
        expectSingleToken(input, new StringLiteralToken(0, input));
    }

    @ParameterizedTest
    @ValueSource(strings = { "q'[]'", "q'()'", "q'{}'", "q'<>'", "q'!!'", "q'{abc{def}ghi}'", "Q'{abc{def}ghi}'" })
    void qStringLiteralToken(String input) {
        expectSingleToken(input, new StringLiteralToken(0, input));
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"a\"", "\"a\"\"\"", "\"a\"\"b\"",
            "\"\"" /* zero-length identifier; not allowed by FB */ })
    void quotedIdentifierToken(String input) {
        expectSingleToken(input, new QuotedIdentifierToken(0, input));
    }

    @ParameterizedTest
    @ValueSource(strings = { "x''", "X''", "x'AE'" })
    void binaryStringLiteralToken(String input) {
        // NOTE: We're not checking if this is a valid binary string literal, just if it's enclosed between quotes
        expectSingleToken(input, new StringLiteralToken(0, input));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", ".1", "0.1", ".5e-5", "100.42e+35", "0e0", "0x00", "0xabcdefABCDEF",
            // invalid but allowed
            "0x", "0x1", "0x123", "1.0e", "1.0e+"
    })
    void numericLiteralToken(String input) {
        // NOTE: the parser allows incomplete approximate numeric (e.g. 1.0e or 1.0e+) and binary (e.g. 0x, 0x1)
        expectSingleToken(input, new NumericLiteralToken(0, input));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-- abc", "/* abc */", "/* abc /* cde */", "--abc\n", "-- abc\r" })
    void commentToken(String input) {
        String originalInput = input;
        SqlTokenizer tokenizer = SqlTokenizer.withReservedWords(FirebirdReservedWords.latest()).of(input);
        char lastChar = input.charAt(input.length() - 1);
        boolean expectWhitespace = lastChar == '\n' || lastChar == '\r';
        if (expectWhitespace) {
            input = input.substring(0, input.length() - 1);
        }
        Token expectedToken = new CommentToken(0, input);

        assertThat(tokenizer)
                .describedAs("expected a token").hasNext();
        assertThat(tokenizer.next())
                .isEqualTo(expectedToken);
        if (expectWhitespace) {
            assertThat(tokenizer.next())
                    .describedAs("expected whitespace token")
                    .isEqualTo(new WhitespaceToken(originalInput.length() - 1, Character.toString(lastChar)));
        }
        assertThat(tokenizer)
                .describedAs("expected no token").isExhausted();
    }

    @Test
    void incompleteBlockComment() {
        SqlTokenizer tokenizer = SqlTokenizer.withReservedWords(FirebirdReservedWords.latest())
                .of("/* block comment without end");

        assertThatExceptionOfType(UnexpectedEndOfInputException.class)
                .isThrownBy(tokenizer::hasNext);
    }

    @Test
    void simpleSelectStatement() {
        String statementText =
                "select * from sometable where a = ? and b = 'abc' and cde = 12.5 and (xyz is unknown or pass)";
        SqlTokenizer tokenizer = SqlTokenizer.withReservedWords(FirebirdReservedWords.latest())
                .of(statementText);

        assertThat(tokenizer).toIterable().containsExactly(
                new ReservedToken(0, "select"),
                new WhitespaceToken(6, " "),
                new OperatorToken(7, "*"),
                new WhitespaceToken(8, " "),
                new ReservedToken(9, "from"),
                new WhitespaceToken(13, " "),
                new GenericToken(14, "sometable"),
                new WhitespaceToken(23, " "),
                new ReservedToken(24, "where"),
                new WhitespaceToken(29, " "),
                new GenericToken(30, "a"),
                new WhitespaceToken(31, " "),
                new OperatorToken(32, "="),
                new WhitespaceToken(33, " "),
                new PositionalParameterToken(34),
                new WhitespaceToken(35, " "),
                new OperatorToken(36, "and"),
                new WhitespaceToken(39, " "),
                new GenericToken(40, "b"),
                new WhitespaceToken(41, " "),
                new OperatorToken(42, "="),
                new WhitespaceToken(43, " "),
                new StringLiteralToken(44, "'abc'"),
                new WhitespaceToken(49, " "),
                new OperatorToken(50, "and"),
                new WhitespaceToken(53, " "),
                new GenericToken(54, "cde"),
                new WhitespaceToken(57, " "),
                new OperatorToken(58, "="),
                new WhitespaceToken(59, " "),
                new NumericLiteralToken(60, "12.5"),
                new WhitespaceToken(64, " "),
                new OperatorToken(65, "and"),
                new WhitespaceToken(68, " "),
                new ParenthesisOpen(69),
                new GenericToken(70, "xyz"),
                new WhitespaceToken(73, " "),
                new OperatorToken(74, "is"),
                new WhitespaceToken(76, " "),
                BooleanLiteralToken.unknownToken(77, "unknown"),
                new WhitespaceToken(84, " "),
                new OperatorToken(85, "or"),
                new WhitespaceToken(87, " "),
                new GenericToken(88, "pass"),
                new ParenthesisClose(92)
        );
    }

    private static void expectSingleToken(String input, Token expectedToken) {
        SqlTokenizer tokenizer = SqlTokenizer.withReservedWords(FirebirdReservedWords.latest()).of(input);

        assertThat(tokenizer)
                .describedAs("expected a token").hasNext();
        assertThat(tokenizer.next())
                .isEqualTo(expectedToken);
        assertThat(tokenizer)
                .describedAs("expected no token").isExhausted();
    }
}

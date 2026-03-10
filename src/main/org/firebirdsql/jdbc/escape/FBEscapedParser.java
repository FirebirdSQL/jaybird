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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.jdbc.FBProcedureCall;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.text.BreakIterator;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The class {@code FBEscapedParser} parses the SQL and converts escaped syntax to native form.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@InternalApi
public final class FBEscapedParser {

    /*
     Stored procedure calls support both following syntax:
        {call procedure_name[(arg1, arg2, ...)]}
     or
        {?= call procedure_name[(arg1, arg2, ...)]}
     */
    private static final String ESCAPE_CALL_KEYWORD = "call";
    private static final String ESCAPE_CALL_KEYWORD3 = "?";
    private static final String ESCAPE_DATE_KEYWORD = "d";
    private static final String ESCAPE_TIME_KEYWORD = "t";
    private static final String ESCAPE_TIMESTAMP_KEYWORD = "ts";
    private static final String ESCAPE_FUNCTION_KEYWORD = "fn";
    private static final String ESCAPE_ESCAPE_KEYWORD = "escape";
    private static final String ESCAPE_OUTERJOIN_KEYWORD = "oj";
    private static final String ESCAPE_LIMIT_KEYWORD = "limit";
    // NOTE: The "disable escape processing" escape ({\ ... \}) a.k.a. "disabled escape" is handled separately

    /**
     * Regular expression to check for existence of JDBC escapes. It is used to skip processing the entire SQL statement
     * if it does not contain any of the escape introducers.
     */
    private static final Pattern CHECK_ESCAPE_PATTERN = Pattern.compile(
            "\\{(?:(?:(?:\\?\\s*=\\s*)?call|d|ts?|escape|fn|oj|limit)\\s|\\\\)", Pattern.CASE_INSENSITIVE);

    private static final String LIMIT_OFFSET_CLAUSE = " offset ";

    private FBEscapedParser() {
        // No instances
    }

    /**
     * Check if the target SQL contains at least one of the escaped syntax commands. This method performs a simple regex
     * match, so it may report that SQL contains escaped syntax when the <code>"&#123;"</code> is followed by
     * the escaped syntax command in regular string constants that are passed as parameters. In this case
     * {@link #parse(String)} will perform complete SQL parsing.
     *
     * @param sql
     *         to test
     * @return {@code true} if the {@code sql} is suspected to contain escaped syntax.
     */
    private static boolean checkForEscapes(String sql) {
        return CHECK_ESCAPE_PATTERN.matcher(sql).find();
    }

    /**
     * Converts escaped parts in {@code sql} to native representation.
     *
     * @param sql
     *         to parse
     * @return native form of the {@code sql}.
     */
    public static String toNativeSql(String sql) throws SQLException {
        return parse(sql);
    }

    /**
     * Converts escaped parts in {@code sql} to native representation.
     *
     * @param sql
     *         to parse
     * @return native form of the {@code sql}.
     */
    @SuppressWarnings("java:S127")
    public static String parse(final String sql) throws SQLException {
        if (!checkForEscapes(sql)) return sql;

        ParserState state = ParserState.INITIAL_STATE;
        // Note initialising to 8 as that was the minimum size in Oracle Java at some point, and we (usually) need less
        // than the default of 16
        final var bufferStack = new ArrayDeque<StringBuilder>(8);
        final var charAccess = new CharAccess(sql);
        var buffer = new StringBuilder(sql.length());

        while (charAccess.hasNext()) {
            char currentChar = charAccess.next(state);
            state = state.nextState(currentChar);
            switch (state) {
            case INITIAL_STATE -> {
                // Ignore leading whitespace
            }
            case NORMAL_STATE, LITERAL_STATE, DELIMITED_IDENTIFIER,
                    START_LINE_COMMENT, LINE_COMMENT, START_BLOCK_COMMENT, BLOCK_COMMENT, END_BLOCK_COMMENT,
                    POSSIBLE_Q_LITERAL_ENTER -> buffer.append(currentChar);
            case ESCAPE_ENTER_STATE -> {
                if (charAccess.hasNext() && charAccess.peekNext(state) == '\\') {
                    // Disable escape processing
                    charAccess.skipNext();
                    state = processDisabledEscape(charAccess, buffer);
                } else {
                    bufferStack.push(buffer);
                    buffer = new StringBuilder();
                }
            }
            case ESCAPE_EXIT_STATE -> {
                if (bufferStack.isEmpty()) {
                    throw new FBSQLParseException("Unbalanced JDBC escape, too many '}'");
                }
                String escapeText = buffer.toString();
                buffer = bufferStack.pop();
                escapeToNative(buffer, escapeText);
            }
            case Q_LITERAL_START -> {
                buffer.append(currentChar);
                final char alternateStartChar = charAccess.next(state);
                buffer.append(alternateStartChar);
                var qLiteralParser = new QLiteralParser(alternateStartChar);
                while (charAccess.hasNext()) {
                    currentChar = charAccess.next(state);
                    buffer.append(currentChar);

                    if (qLiteralParser.isQLiteralEnd(currentChar)) {
                        state = ParserState.NORMAL_STATE;
                        break;
                    }
                }
                if (state == ParserState.Q_LITERAL_START) {
                    throw new FBSQLParseException("Unexpected end of string at parser state " + state);
                }
            }
            default -> throw new FBSQLParseException("Unexpected parser state " + state);
            }
        }
        if (!bufferStack.isEmpty()) {
            throw new FBSQLParseException("Unbalanced JDBC escape, too many '{'");
        }
        return buffer.toString();
    }

    private static void processEscaped(final String escaped, final StringBuilder keyword, final StringBuilder payload) {
        assert keyword.isEmpty() && payload.isEmpty() : "StringBuilders keyword and payload should be empty";

        // Extract the keyword from the escaped syntax.
        final BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(escaped);
        final int keyStart = iterator.first();
        final int keyEnd = iterator.next();
        keyword.append(escaped, keyStart, keyEnd);

        int payloadStart = keyEnd;
        // Remove whitespace before payload
        while (payloadStart < escaped.length() - 1 && escaped.charAt(payloadStart) <= ' ') {
            payloadStart++;
        }
        int payloadEnd = escaped.length();
        // Remove whitespace after payload
        while (payloadEnd > payloadStart && escaped.charAt(payloadEnd - 1) <= ' ') {
            payloadEnd--;
        }
        payload.append(escaped, payloadStart, payloadEnd);
    }

    /**
     * This method checks the passed parameter to conform the escaped syntax,
     * checks for the unknown keywords and re-formats result according to the
     * Firebird SQL syntax.
     *
     * @param target
     *         Target StringBuilder to append to.
     * @param escaped
     *         the part of escaped SQL between the '{' and '}'.
     */
    private static void escapeToNative(final StringBuilder target, final String escaped) throws SQLException {
        final StringBuilder keyword = new StringBuilder();
        final StringBuilder payload = new StringBuilder(Math.max(16, escaped.length()));

        processEscaped(escaped, keyword, payload);

        //Handle keywords.
        final String keywordStr = keyword.toString().toLowerCase(Locale.ROOT);
        // NOTE: We assume here that all KEYWORD constants are lowercase!
        switch (keywordStr) {
        // TODO Should we call convertProcedureCall? It leads to inefficient double parsing
        case ESCAPE_CALL_KEYWORD -> convertProcedureCall(target, "{" + keyword + ' ' + payload + '}');
        // TODO Should we call convertProcedureCall? It leads to inefficient double parsing
        case ESCAPE_CALL_KEYWORD3 -> convertProcedureCall(target, "{" + ESCAPE_CALL_KEYWORD3 + payload + '}');
        case ESCAPE_DATE_KEYWORD -> toDateString(target, payload);
        case ESCAPE_ESCAPE_KEYWORD -> convertEscapeString(target, payload);
        case ESCAPE_FUNCTION_KEYWORD -> convertEscapedFunction(target, payload);
        case ESCAPE_OUTERJOIN_KEYWORD -> convertOuterJoin(target, payload);
        case ESCAPE_TIME_KEYWORD -> toTimeString(target, payload);
        case ESCAPE_TIMESTAMP_KEYWORD -> toTimestampString(target, payload);
        case ESCAPE_LIMIT_KEYWORD -> convertLimitString(target, payload);
        default -> throw new FBSQLParseException("Unknown keyword " + keywordStr + " for escaped syntax.");
        }
    }

    /**
     * This method converts the 'yyyy-mm-dd' date format into the Firebird
     * understandable format.
     *
     * @param target
     *         Target StringBuilder to append to.
     * @param dateStr
     *         the date in the 'yyyy-mm-dd' format.
     */
    private static void toDateString(final StringBuilder target, final CharSequence dateStr) {
        // use shorthand date cast (using just the string will not work in all contexts)
        target.append("DATE ").append(dateStr);
    }

    /**
     * This method converts the 'hh:mm:ss' time format into the Firebird
     * understandable format.
     *
     * @param target
     *         Target StringBuilder to append to.
     * @param timeStr
     *         the date in the 'hh:mm:ss' format.
     */
    private static void toTimeString(final StringBuilder target, final CharSequence timeStr) {
        // use shorthand time cast (using just the string will not work in all contexts)
        target.append("TIME ").append(timeStr);
    }

    /**
     * This method converts the 'yyyy-mm-dd hh:mm:ss' timestamp format into the
     * Firebird understandable format.
     *
     * @param target
     *         Target StringBuilder to append to.
     * @param timestampStr
     *         the date in the 'yyyy-mm-dd hh:mm:ss' format.
     */
    private static void toTimestampString(final StringBuilder target, final CharSequence timestampStr) {
        // use shorthand timestamp cast (using just the string will not work in all contexts)
        target.append("TIMESTAMP ").append(timestampStr);
    }

    /**
     * Converts the escaped procedure call syntax into the native procedure call.
     *
     * @param target
     *         Target StringBuilder to append native procedure call to.
     * @param procedureCall
     *         part of {call proc_name(...)} without curly braces and "call"
     *         word.
     */
    private static void convertProcedureCall(final StringBuilder target, final String procedureCall) throws SQLException {
        FBEscapedCallParser tempParser = new FBEscapedCallParser();
        FBProcedureCall call = tempParser.parseCall(procedureCall);
        call.checkParameters();
        target.append(call.getSQL(false));
    }

    /**
     * This method converts the escaped outer join call syntax into the native
     * outer join. Actually, we do not change anything here, since Firebird's
     * syntax is the same.
     *
     * @param target
     *         Target StringBuilder to append to.
     * @param outerJoin
     *         Outer join text
     */
    private static void convertOuterJoin(final StringBuilder target, final CharSequence outerJoin) {
        target.append(outerJoin);
    }

    /**
     * Convert the {@code "{escape '...'}"} call into the corresponding escape clause for Firebird.
     *
     * @param escapeString
     *         escape string to convert
     */
    private static void convertEscapeString(final StringBuilder target, final CharSequence escapeString) {
        target.append("ESCAPE ").append(escapeString);
    }

    /**
     * Convert the {@code "{limit <rows> [offset <rows_offset>]}"} call into the corresponding rows clause for Firebird.
     * <p>
     * NOTE: We assume that the {limit ...} escape occurs in the right place to work for a
     * <a href="https://www.firebirdsql.org/file/documentation/chunk/en/refdocs/fblangref40/fblangref40-dml.html#fblangref40-dml-select-rows">{@code ROWS}</a>
     * clause in Firebird.
     * </p>
     * <p>
     * This implementation supports a parameter for the value of &lt;rows&gt;, but not for &lt;rows_offset&gt;.
     * </p>
     *
     * @param limitClause
     *         Limit clause
     */
    private static void convertLimitString(final StringBuilder target, final CharSequence limitClause)
            throws FBSQLParseException {
        final String limitEscape = limitClause.toString().toLowerCase(Locale.ROOT);
        final int offsetStart = limitEscape.indexOf(LIMIT_OFFSET_CLAUSE);
        if (offsetStart == -1) {
            target.append("ROWS ").append(limitEscape);
        } else {
            final String rows = limitEscape.substring(0, offsetStart).trim();
            final String offset = limitEscape.substring(offsetStart + LIMIT_OFFSET_CLAUSE.length()).trim();
            if (offset.indexOf('?') != -1) {
                throw new FBSQLParseException("Extended limit escape ({limit <rows> offset <offset_rows>}) does not support parameters for <offset_rows>");
            }
            target.append("ROWS ").append(offset).append(" TO ").append(offset).append("+").append(rows);
        }
    }

    /**
     * This method converts escaped function to a server function call. Actually
     * we do not change anything here, we hope that all UDF are defined.
     *
     * @param target
     *         Target StringBuilder to append to.
     * @param escapedFunction
     *         escaped function call
     * @throws FBSQLParseException
     *         if something was wrong.
     */
    private static void convertEscapedFunction(final StringBuilder target, final CharSequence escapedFunction)
            throws FBSQLParseException {
        final String templateResult = FBEscapedFunctionHelper.convertTemplate(escapedFunction.toString());
        target.append(templateResult != null ? templateResult : escapedFunction);
    }

    private static ParserState processDisabledEscape(CharAccess charAccess, StringBuilder buffer)
            throws FBSQLParseException {
        ParserState state = ParserState.NORMAL_STATE;

        boolean inEscape = true;
        while (charAccess.hasNext()) {
            char currentChar = charAccess.next(state, ParserState.DISABLED_ESCAPE);
            if (currentChar == '\\') {
                currentChar = charAccess.next(state, ParserState.DISABLED_ESCAPE);
                if (currentChar == '}'
                        && (state == ParserState.NORMAL_STATE || state == ParserState.POSSIBLE_Q_LITERAL_ENTER)) {
                    // End of disabled escape
                    inEscape = false;
                    break;
                }
                checkEscapeInDisabledEscape(currentChar, charAccess.position(), state);
            }
            state = nextDisabledEscapeParserState(state, currentChar);
            switch (state) {
            case NORMAL_STATE, LITERAL_STATE, DELIMITED_IDENTIFIER,
                 START_LINE_COMMENT, LINE_COMMENT, START_BLOCK_COMMENT, BLOCK_COMMENT, END_BLOCK_COMMENT,
                 POSSIBLE_Q_LITERAL_ENTER -> buffer.append(currentChar);
            case Q_LITERAL_START -> {
                buffer.append(currentChar);
                currentChar = charAccess.next(state, ParserState.DISABLED_ESCAPE);
                if (currentChar == '\\') {
                    currentChar = charAccess.next(state, ParserState.DISABLED_ESCAPE);
                    checkEscapeInDisabledEscape(currentChar, charAccess.position(), state);
                }
                buffer.append(currentChar);
                var qLiteralParser = new QLiteralParser(currentChar);
                while (charAccess.hasNext()) {
                    currentChar = charAccess.next(state, ParserState.DISABLED_ESCAPE);
                    if (currentChar == '\\') {
                        currentChar = charAccess.next(state, ParserState.DISABLED_ESCAPE);
                        checkEscapeInDisabledEscape(currentChar, charAccess.position(), state);
                    }
                    buffer.append(currentChar);

                    if (qLiteralParser.isQLiteralEnd(currentChar)) {
                        state = ParserState.NORMAL_STATE;
                        break;
                    }
                }
                if (state == ParserState.Q_LITERAL_START) {
                    throw new FBSQLParseException(
                            "Unexpected end of string at parser state " + state + " at " + ParserState.DISABLED_ESCAPE);
                }
            }
            default -> throw new FBSQLParseException("Unexpected parser state " + state + " at "
                    + ParserState.DISABLED_ESCAPE);
            }
        }

        if (inEscape) {
            throw new FBSQLParseException("Unexpected end of string at parser state " + state + " at "
                    + ParserState.DISABLED_ESCAPE);
        }

        return state;
    }

    private static void checkEscapeInDisabledEscape(char ch, int position, ParserState state)
            throws FBSQLParseException {
        if (ch == '}') {
            // This only covers cases within a comment, literal or quoted identifier (see jdp-2026-03); the callers
            // handle the case where '}' is valid and does end the escape.
            throw new FBSQLParseException("Unescaped backslash: occurrence of unescaped \\} in comment, literal, or "
                    + "quoted identifier is not valid");
        } else if (ch != '\\') {
            throw new FBSQLParseException("Unescaped backslash at position " + (position - 1) + " at parser state "
                    + state + " at " + ParserState.DISABLED_ESCAPE);
        }
    }

    /**
     * Remaps normal parser state values to parser state values during disabled escape processing.
     *
     * @param currentState
     *         current parser state
     * @param inputChar
     *         character for next state
     * @return state for disabled escape processing
     */
    private static ParserState nextDisabledEscapeParserState(ParserState currentState, char inputChar)
            throws FBSQLParseException {
        ParserState nextState = currentState.nextState(inputChar);
        return switch (nextState) {
            // No JDBC escape processing inside disabled escape
            case ESCAPE_ENTER_STATE, ESCAPE_EXIT_STATE -> ParserState.NORMAL_STATE;
            default -> nextState;
        };
    }

    private enum ParserState {
        /**
         * Initial parser state (to ignore leading whitespace)
         */
        INITIAL_STATE {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                return Character.isWhitespace(inputChar) ? INITIAL_STATE : NORMAL_STATE.nextState(inputChar);
            }
        },
        /**
         * Normal SQL query text state
         */
        NORMAL_STATE {
            @Override
            protected ParserState nextState(char inputChar) {
                return switch (inputChar) {
                    case '\'' -> LITERAL_STATE;
                    case '"' -> DELIMITED_IDENTIFIER;
                    case '{' -> ESCAPE_ENTER_STATE;
                    case '}' -> ESCAPE_EXIT_STATE;
                    case '-' -> START_LINE_COMMENT;
                    case '/' -> START_BLOCK_COMMENT;
                    case 'q', 'Q' -> POSSIBLE_Q_LITERAL_ENTER;
                    default -> NORMAL_STATE;
                };
            }
        },
        /**
         * SQL literal text (text inside quotes)
         */
        LITERAL_STATE {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '\'') ? NORMAL_STATE : LITERAL_STATE;
            }
        },
        /**
         * Dialect 3 delimited identifier or dialect 1 literal text (text inside double quotes).
         */
        DELIMITED_IDENTIFIER {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '"') ? NORMAL_STATE : DELIMITED_IDENTIFIER;
            }
        },
        /**
         * Start of JDBC escape (<code>"&#123;"</code> character encountered).
         */
        ESCAPE_ENTER_STATE {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                return switch (inputChar) {
                    //@formatter:off
                    case    // start of {?= call ...}
                            '?',
                            // start of {call ...}
                            'c', 'C',
                            // start of {d ...}
                            'd', 'D',
                            // start of {t ...} or {ts ...}
                            't', 'T',
                            // start of {escape ...}
                            'e', 'E',
                            // start of {fn ...}
                            'f', 'F',
                            // start of {oj ...}
                            'o', 'O',
                            // start of {limit ...}
                            'l', 'L' -> NORMAL_STATE;
                    //@formatter:on
                    default -> throw new FBSQLParseException(
                            "Unexpected first character inside JDBC escape: " + inputChar);
                };
            }
        },
        /**
         * End of JDBC escape (<code>"&#125;"</code> character encountered)
         */
        ESCAPE_EXIT_STATE {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                return NORMAL_STATE.nextState(inputChar);
            }
        },
        /**
         * Potential start of line comment
         */
        START_LINE_COMMENT {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                return (inputChar == '-') ? LINE_COMMENT : NORMAL_STATE.nextState(inputChar);
            }
        },
        /**
         * Line comment
         */
        LINE_COMMENT {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '\n') ? NORMAL_STATE : LINE_COMMENT;
            }
        },
        /**
         * Potential start of block comment
         */
        START_BLOCK_COMMENT {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                return (inputChar == '*') ? BLOCK_COMMENT : NORMAL_STATE.nextState(inputChar);
            }
        },
        /**
         * Block comment
         */
        BLOCK_COMMENT {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '*') ? END_BLOCK_COMMENT : BLOCK_COMMENT;
            }
        },
        /**
         * Potential block comment end
         */
        END_BLOCK_COMMENT {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '/') ? NORMAL_STATE : BLOCK_COMMENT;
            }
        },
        /**
         * Potential Q-literal
         */
        POSSIBLE_Q_LITERAL_ENTER {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '\'') ? Q_LITERAL_START : NORMAL_STATE;
            }
        },
        /**
         * Start of Q escape, next character will be the alternate end character. Further processing needs to be done
         * separately.
         */
        Q_LITERAL_START {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                throw new FBSQLParseException("Q-literal handling needs to be performed separately");
            }
        },
        /**
         * Processing the "disabled escape" (<code>&#123;\...\&#125;</code>).
         * <p>
         * This state is not used during parsing, but only used for reporting a state name in exception messages.
         * </p>
         */
        DISABLED_ESCAPE {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                throw new FBSQLParseException("Disabled escape handling needs to be performed separately");
            }
        };

        /**
         * Decides on the next ParserState based on the input character.
         *
         * @param inputChar
         *         Input character
         * @return Next state
         * @throws FBSQLParseException
         *         For incorrect character for current state during parsing
         */
        protected abstract ParserState nextState(char inputChar) throws FBSQLParseException;
    }

    private static final class QLiteralParser {

        private final char endChar;
        private boolean possibleEnd = false;

        QLiteralParser(char startChar) {
            this.endChar = endChar(startChar);
        }

        boolean isQLiteralEnd(char ch) {
            if (possibleEnd && ch == '\'') return true;
            possibleEnd = ch == endChar;
            return false;
        }

        private static char endChar(char startChar) {
            return switch (startChar) {
                case '(' -> ')';
                case '{' -> '}';
                case '[' -> ']';
                case '<' -> '>';
                default -> startChar;
            };
        }

    }

    private static final class CharAccess {

        private final String sql;
        private final int length;
        // Position of the next character to be returned
        private int pos;

        CharAccess(String sql) {
            this(sql, 0);
        }

        CharAccess(String sql, int startPosition) {
            if (startPosition < 0) {
                throw new IllegalArgumentException("startPosition must be >= 0, was " + startPosition);
            }
            this.sql = sql;
            length = sql.length();
            pos = startPosition;
        }

        boolean hasNext() {
            return pos < length;
        }

        /**
         * Skip the next character.
         * <p>
         * Attempts to position beyond the end of the contained string will position at the end of string.
         * </p>
         */
        void skipNext() {
            pos = Math.min(pos + 1, length);
        }

        void checkPosition(ParserState state) throws FBSQLParseException {
            if (pos >= length) {
                throw new FBSQLParseException("Unexpected end of string at parser state " + state);
            }
        }

        void checkPosition(ParserState state, ParserState outerState) throws FBSQLParseException {
            if (pos >= length) {
                throw new FBSQLParseException(
                        "Unexpected end of string at parser state " + state + " at " + outerState);
            }
        }

        char next(ParserState state) throws FBSQLParseException {
            checkPosition(state);
            return sql.charAt(pos++);
        }

        char next(ParserState state, ParserState outerState) throws FBSQLParseException {
            checkPosition(state, outerState);
            return sql.charAt(pos++);
        }

        char peekNext(ParserState state) throws FBSQLParseException {
            checkPosition(state);
            return sql.charAt(pos);
        }

        /**
         * Position of the last returned character, or the position immediately before the next character.
         * <p>
         * The return value can be {@code -1} if {@link #CharAccess(String)} was called, or
         * {@link #CharAccess(String, int)} with {@code startPosition = 0}, and {@link #next(ParserState)} or
         * {@link #next(ParserState, ParserState)} was never called.
         * </p>
         *
         * @return position of the last character returned by {@code next}
         */
        int position() {
            return pos - 1;
        }

    }

}

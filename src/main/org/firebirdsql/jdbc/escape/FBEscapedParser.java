/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import java.sql.SQLException;
import java.text.BreakIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;

/**
 * The class <code>FBEscapedParser</code> parses the SQL and converts escaped
 * syntax to native form.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
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

    /**
     * Regular expression to check for existence of JDBC escapes, is used to
     * stop processing the entire SQL statement if it does not contain any of
     * the escape introducers.
     */
    private static final Pattern CHECK_ESCAPE_PATTERN = Pattern.compile(
            "\\{(?:(?:\\?\\s*=\\s*)?call|d|ts?|escape|fn|oj|limit)\\s",
            Pattern.CASE_INSENSITIVE);

    private static final String LIMIT_OFFSET_CLAUSE = " offset ";

    private final EscapeParserMode mode;

    /**
     * Creates a parser for JDBC escaped strings.
     *
     * @param mode
     *         One of {@link EscapeParserMode#USE_BUILT_IN} or
     *         {@link EscapeParserMode#USE_STANDARD_UDF}
     */
    public FBEscapedParser(EscapeParserMode mode) {
        this.mode = mode;
    }

    /**
     * Check if the target SQL contains at least one of the escaped syntax
     * commands. This method performs a simple regex match, so it may
     * report that SQL contains escaped syntax when the <code>"{"</code> is
     * followed by the escaped syntax command in regular string constants that
     * are passed as parameters. In this case {@link #parse(String)} will
     * perform complete SQL parsing.
     *
     * @param sql
     *         to test
     * @return <code>true</code> if the <code>sql</code> is suspected to contain
     * escaped syntax.
     */
    private boolean checkForEscapes(String sql) {
        return CHECK_ESCAPE_PATTERN.matcher(sql).find();
    }

    /**
     * Converts escaped parts in the passed SQL to native representation.
     *
     * @param sql
     *         to parse
     * @return native form of the <code>sql</code>.
     */
    public String parse(final String sql) throws SQLException {
        if (!checkForEscapes(sql)) return sql;

        ParserState state = ParserState.INITIAL_STATE;
        // Note initialising to 8 as that is the minimum size in Oracle Java, and we (usually) need less than the default of 16
        final Deque<StringBuilder> bufferStack = new ArrayDeque<>(8);
        final int sqlLength = sql.length();
        StringBuilder buffer = new StringBuilder(sqlLength);

        for (int i = 0; i < sqlLength; i++) {
            char currentChar = sql.charAt(i);
            state = state.nextState(currentChar);
            switch (state) {
            case INITIAL_STATE:
                // Ignore leading whitespace
                continue;
            case NORMAL_STATE:
            case LITERAL_STATE:
            case START_LINE_COMMENT:
            case LINE_COMMENT:
            case START_BLOCK_COMMENT:
            case BLOCK_COMMENT:
            case END_BLOCK_COMMENT:
            case POSSIBLE_Q_LITERAL_ENTER:
                buffer.append(currentChar);
                break;
            case ESCAPE_ENTER_STATE:
                bufferStack.push(buffer);
                buffer = new StringBuilder();
                break;
            case ESCAPE_EXIT_STATE:
                if (bufferStack.isEmpty()) {
                    throw new FBSQLParseException("Unbalanced JDBC escape, too many '}'");
                }
                String escapeText = buffer.toString();
                buffer = bufferStack.pop();
                escapeToNative(buffer, escapeText);
                break;
            case Q_LITERAL_START:
                buffer.append(currentChar);
                if (++i >= sqlLength) {
                    throw new FBSQLParseException("Unexpected end of string at parser state " + state);
                }
                final char alternateStartChar = sql.charAt(i);
                buffer.append(alternateStartChar);
                final char alternateEndChar = qLiteralEndChar(alternateStartChar);
                for (i++; i <sqlLength; i++) {
                    currentChar = sql.charAt(i);
                    buffer.append(currentChar);
                    if (currentChar == alternateEndChar
                            && i + 1 < sqlLength && sql.charAt(i + 1) == '\'') {
                        state = ParserState.Q_LITERAL_END;
                        break;
                    }
                }
                if (i == sqlLength) {
                    throw new FBSQLParseException("Unexpected end of string at parser state " + state);
                }
                break;
            default:
                throw new FBSQLParseException("Unexpected parser state " + state);
            }
        }
        if (bufferStack.isEmpty()) {
            return buffer.toString();
        } else {
            throw new FBSQLParseException("Unbalanced JDBC escape, too many '{'");
        }
    }

    private static char qLiteralEndChar(char startChar) {
        switch (startChar) {
        case '(':
            return ')';
        case '{':
            return '}';
        case '[':
            return ']';
        case '<':
            return '>';
        default:
            return startChar;
        }
    }

    private void processEscaped(final String escaped, final StringBuilder keyword, final StringBuilder payload) {
        assert (keyword.length() == 0 && payload.length() == 0) : "StringBuilders keyword and payload should be empty";

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
    private void escapeToNative(final StringBuilder target, final String escaped) throws SQLException {
        final StringBuilder keyword = new StringBuilder();
        final StringBuilder payload = new StringBuilder(Math.max(16, escaped.length()));

        processEscaped(escaped, keyword, payload);

        //Handle keywords.
        final String keywordStr = keyword.toString().toLowerCase();
        // NOTE: We assume here that all KEYWORD constants are lowercase!
        switch (keywordStr) {
        case ESCAPE_CALL_KEYWORD:
            // TODO Should we call convertProcedureCall? It leads to inefficient double parsing
            convertProcedureCall(target, "{" + keyword + ' ' + payload + '}');
            break;
        case ESCAPE_CALL_KEYWORD3:
            // TODO Should we call convertProcedureCall? It leads to inefficient double parsing
            convertProcedureCall(target, "{" + ESCAPE_CALL_KEYWORD3 + payload + '}');
            break;
        case ESCAPE_DATE_KEYWORD:
            toDateString(target, payload);
            break;
        case ESCAPE_ESCAPE_KEYWORD:
            convertEscapeString(target, payload);
            break;
        case ESCAPE_FUNCTION_KEYWORD:
            convertEscapedFunction(target, payload);
            break;
        case ESCAPE_OUTERJOIN_KEYWORD:
            convertOuterJoin(target, payload);
            break;
        case ESCAPE_TIME_KEYWORD:
            toTimeString(target, payload);
            break;
        case ESCAPE_TIMESTAMP_KEYWORD:
            toTimestampString(target, payload);
            break;
        case ESCAPE_LIMIT_KEYWORD:
            convertLimitString(target, payload);
            break;
        default:
            throw new FBSQLParseException("Unknown keyword " + keywordStr + " for escaped syntax.");
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
    private void toDateString(final StringBuilder target, final CharSequence dateStr) {
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
    private void toTimeString(final StringBuilder target, final CharSequence timeStr) {
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
    private void toTimestampString(final StringBuilder target, final CharSequence timestampStr) {
        // use shorthand timestamp cast (using just the string will not work in all contexts)
        target.append("TIMESTAMP ").append(timestampStr);
    }

    /**
     * This methods converts the escaped procedure call syntax into the native
     * procedure call.
     *
     * @param target
     *         Target StringBuilder to append native procedure call to.
     * @param procedureCall
     *         part of {call proc_name(...)} without curly braces and "call"
     *         word.
     */
    private void convertProcedureCall(final StringBuilder target, final String procedureCall) throws SQLException {
        FBEscapedCallParser tempParser = new FBEscapedCallParser(mode);
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
    private void convertOuterJoin(final StringBuilder target, final CharSequence outerJoin) {
        target.append(outerJoin);
    }

    /**
     * Convert the <code>"{escape '...'}"</code> call into the corresponding
     * escape clause for Firebird.
     *
     * @param escapeString
     *         escape string to convert
     */
    private void convertEscapeString(final StringBuilder target, final CharSequence escapeString) {
        target.append("ESCAPE ").append(escapeString);
    }

    /**
     * Convert the
     * <code>"{limit &lt;rows&gt; [offset &lt;rows_offset&gt;]}"</code> call
     * into the corresponding rows clause for Firebird.
     * <p>
     * NOTE: We assume that the {limit ...} escape occurs in the right place to
     * work for a
     * <code><a href="http://www.firebirdsql.org/file/documentation/reference_manuals/reference_material/html/langrefupd25-select.html#langrefupd25-select-rows">ROWS</a></code>
     * clause in Firebird.
     * </p>
     * <p>
     * This implementation supports a parameter for the value of &lt;rows&gt;,
     * but not for &lt;rows_offset&gt;.
     * </p>
     *
     * @param limitClause
     *         Limit clause
     */
    private void convertLimitString(final StringBuilder target, final CharSequence limitClause)
            throws FBSQLParseException {
        final String limitEscape = limitClause.toString().toLowerCase();
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
    private void convertEscapedFunction(final StringBuilder target, final CharSequence escapedFunction)
            throws FBSQLParseException {
        final String templateResult = FBEscapedFunctionHelper.convertTemplate(escapedFunction.toString(), mode);
        target.append(templateResult != null ? templateResult : escapedFunction);
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
                switch (inputChar) {
                case '\'':
                    return LITERAL_STATE;
                case '{':
                    return ESCAPE_ENTER_STATE;
                case '}':
                    return ESCAPE_EXIT_STATE;
                case '-':
                    return START_LINE_COMMENT;
                case '/':
                    return START_BLOCK_COMMENT;
                case 'q':
                case 'Q':
                    return POSSIBLE_Q_LITERAL_ENTER;
                default:
                    return NORMAL_STATE;
                }
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
         * Start of JDBC escape ({ character encountered).
         */
        ESCAPE_ENTER_STATE {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                switch (inputChar) {
                case '?': // start of {?= call ...}
                case 'c': // start of {call ...}
                case 'd': // start of {d ...}
                case 't': // start of {t ...} or {ts ...}
                case 'e': // start of {escape ...}
                case 'f': // start of {fn ...}
                case 'o': // start of {oj ...}
                case 'l': // start of {limit ...}
                    return NORMAL_STATE;
                default:
                    throw new FBSQLParseException("Unexpected first character inside JDBC escape: " + inputChar);
                }
            }
        },
        /**
         * End of JDBC escape (} character encountered)
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
        Q_LITERAL_END {
            @Override
            protected ParserState nextState(char inputChar) throws FBSQLParseException {
                if (inputChar != '\'') {
                    throw new FBSQLParseException("Invalid char " + inputChar + " for state Q_LITERAL_END");
                }
                return NORMAL_STATE;
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

    public enum EscapeParserMode {
        /**
         * Use built-in functions if available.
         * <p>
         * This may still result in a UDF function being used if the UDF matches
         * naming and arguments of the function (or function template)
         * </p>
         */
        USE_BUILT_IN,
        /**
         * Attempt to use UDF if there is no explicit function template defined.
         * <p>
         * This may still result in a built-in function being used if there is
         * an explicit function template, and/or if there is no UDF with the
         * name, but there is a built-in with the same name and parameter order.
         * </p>
         */
        USE_STANDARD_UDF
    }
}

/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.text.BreakIterator;
import java.text.MessageFormat;
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

    /**
     * Use built-in functions if available.
     * <p>
     * This may still result in a UDF function beign used if the UDF matches
     * naming and arguments of the function (or function template)
     * </p>
     */
    public static final int USE_BUILT_IN = 0;

    /**
     * Attempt to use UDF if there is no explicit function template defined.
     * <p>
     * This may still result in a built-in function being used if there is an
     * explicit function template, and/or if there is no UDF with the name, but
     * there is a built-in with the same name and parameter order.
     * </p>
     */
    public static final int USE_STANDARD_UDF = 1;

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
     * the substrings.
     */
    private static final Pattern CHECK_ESCAPE_PATTERN = Pattern.compile(
            "\\{(?:(?:\\?\\s*=\\s*)?call|d|ts?|escape|fn|oj|limit)\\s", 
            Pattern.CASE_INSENSITIVE);

    private static final String LIMIT_OFFSET_CLAUSE = " offset ";

    private final int mode;

    /**
     * Creates a parser for JDBC escaped strings.
     * 
     * @param mode
     *            One of {@link FBEscapedParser#USE_BUILT_IN} or
     *            {@link FBEscapedParser#USE_STANDARD_UDF}
     */
    public FBEscapedParser(int mode) {
        assert (mode == USE_BUILT_IN || mode == USE_STANDARD_UDF);
        this.mode = mode;
    }

    /**
     * Check if the target SQL contains at least one of the escaped syntax
     * commands. This method performs simple substring matching, so it may
     * report that SQL contains escaped syntax when the <code>"{"</code> is
     * followed by the escaped syntax command in regular string constants that
     * are passed as parameters. In this case {@link #parse(String)} will
     * perform complete SQL parsing.
     * 
     * @param sql
     *            to test
     * @return <code>true</code> if the <code>sql</code> is suspected to contain
     *         escaped syntax.
     */
    private boolean checkForEscapes(String sql) {
        return CHECK_ESCAPE_PATTERN.matcher(sql).find();
    }

    /**
     * Converts escaped parts in the passed SQL to native representation.
     * 
     * @param sql
     *            to parse
     * @return native form of the <code>sql</code>.
     */
    public String parse(final String sql) throws FBSQLException {
        ParserState state = ParserState.NORMAL_STATE;
        int nestedEscaped = 0;

        if (!checkForEscapes(sql)) return sql;

        final StringBuilder buffer = new StringBuilder();
        final StringBuilder escape = new StringBuilder();

        for (int i = 0, n = sql.length(); i < n; i++) {
            char currentChar = sql.charAt(i);
            state = state.nextState(currentChar);

            switch (state) {
            case NORMAL_STATE:
            case LITERAL_STATE:
                buffer.append(currentChar);
                break;
            case ESCAPE_ENTER_STATE:
                nestedEscaped++;
                break;
            case ESCAPE_STATE:
                escape.append(currentChar);
                break;
            case ESCAPE_EXIT_STATE:
                nestedEscaped--;
                if (nestedEscaped == 0) {
                    buffer.append(escapeToNative(escape.toString()));
                    escape.setLength(0);
                    state = ParserState.NORMAL_STATE;
                } else {
                    state = ParserState.ESCAPE_STATE;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected parser state " + state);
            }
        }
        return buffer.toString();
    }

    private void processEscaped(final String escaped, final StringBuilder keyword, final StringBuilder payload) {
        if (keyword.length() != 0) keyword.setLength(0);
        if (payload.length() != 0) payload.setLength(0);

        // Extract the keyword from the escaped syntax.
        final BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(escaped);
        final int keyStart = iterator.first();
        final int keyEnd = iterator.next();
        keyword.append(escaped.substring(keyStart, keyEnd));
        payload.append(escaped.substring(keyEnd, escaped.length()));
    }

    /**
     * This method checks the passed parameter to conform the escaped syntax,
     * checks for the unknown keywords and re-formats result according to the
     * Firebird SQL syntax.
     * 
     * @param escaped
     *            the part of escaped SQL between the '{' and '}'.
     * @return the native representation of the SQL code.
     */
    private String escapeToNative(final String escaped) throws FBSQLException {
        final StringBuilder keyword = new StringBuilder();
        final StringBuilder payload = new StringBuilder();

        processEscaped(escaped, keyword, payload);

        //Handle keywords.
        final String keywordStr = keyword.toString().toLowerCase();
        // NOTE: We assume here that all KEYWORD constants are lowercase!
        if (keywordStr.equals(ESCAPE_CALL_KEYWORD)) {
            StringBuilder call = new StringBuilder();
            call.append('{')
                .append(keyword)
                .append(' ')
                .append(payload)
                .append('}');
            return convertProcedureCall(call.toString());
        } else if (keywordStr.equals(ESCAPE_CALL_KEYWORD3)) {
            StringBuilder call = new StringBuilder();
            call.append('{')
                .append(ESCAPE_CALL_KEYWORD3)
                .append(payload)
                .append('}');
            return convertProcedureCall(call.toString());
        } else if (keywordStr.equals(ESCAPE_DATE_KEYWORD))
            return toDateString(payload.toString());
        else if (keywordStr.equals(ESCAPE_ESCAPE_KEYWORD))
            return convertEscapeString(payload.toString());
        else if (keywordStr.equals(ESCAPE_FUNCTION_KEYWORD))
            return convertEscapedFunction(payload.toString());
        else if (keywordStr.equals(ESCAPE_OUTERJOIN_KEYWORD))
            return convertOuterJoin(payload.toString());
        else if (keywordStr.equals(ESCAPE_TIME_KEYWORD))
            return toTimeString(payload.toString());
        else if (keywordStr.equals(ESCAPE_TIMESTAMP_KEYWORD))
            return toTimestampString(payload.toString());
        else if (keywordStr.equals(ESCAPE_LIMIT_KEYWORD))
            return convertLimitString(payload.toString());
        else
            throw new FBSQLParseException("Unknown keyword " + keywordStr + " for escaped syntax.");
    }

    /**
     * This method converts the 'yyyy-mm-dd' date format into the Firebird
     * understandable format.
     * 
     * @param dateStr
     *            the date in the 'yyyy-mm-dd' format.
     * @return Firebird understandable date format.
     */
    private String toDateString(final String dateStr) throws FBSQLParseException {
        // use shorthand date cast (using just the string will not work in all contexts)
        return "DATE " + dateStr;
    }

    /**
     * This method converts the 'hh:mm:ss' time format into the Firebird
     * understandable format.
     * 
     * @param timeStr
     *            the date in the 'hh:mm:ss' format.
     * @return Firebird understandable date format.
     */
    private String toTimeString(final String timeStr) throws FBSQLParseException {
        // use shorthand time cast (using just the string will not work in all contexts)
        return "TIME " + timeStr;
    }

    /**
     * This method converts the 'yyyy-mm-dd hh:mm:ss' timestamp format into the
     * Firebird understandable format.
     * 
     * @param timestampStr
     *            the date in the 'yyyy-mm-dd hh:mm:ss' format.
     * @return Firebird understandable date format.
     */
    private String toTimestampString(final String timestampStr) throws FBSQLParseException {
        // use shorthand timestamp cast (using just the string will not work in all contexts)
        return "TIMESTAMP " + timestampStr;
    }

    /**
     * This methods converts the escaped procedure call syntax into the native
     * procedure call.
     * 
     * @param procedureCall
     *            part of {call proc_name(...)} without curly braces and "call"
     *            word.
     * @return native procedure call.
     */
    private String convertProcedureCall(final String procedureCall) throws FBSQLException {
        FBEscapedCallParser tempParser = new FBEscapedCallParser(mode);
        FBProcedureCall call = tempParser.parseCall(procedureCall);
        return call.getSQL(false);
    }

    /**
     * This method converts the escaped outer join call syntax into the native
     * outer join. Actually, we do not change anything here, since Firebird's
     * syntax is the same.
     */
    private String convertOuterJoin(final String outerJoin) throws FBSQLParseException {
        return outerJoin;
    }

    /**
     * Convert the <code>"{escape '...'}"</code> call into the corresponding
     * escape clause for Firebird.
     * 
     * @param escapeString
     *            escape string to convert
     * @return converted code.
     */
    private String convertEscapeString(final String escapeString) {
        return "ESCAPE " + escapeString;
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
     *            Limit clause
     * @return converted code
     */
    private String convertLimitString(final String limitClause) throws FBSQLParseException {
        final int offsetStart = limitClause.toLowerCase().indexOf(LIMIT_OFFSET_CLAUSE);
        if (offsetStart == -1) {
            return "ROWS " + limitClause;
        } else {
            final String rows = limitClause.substring(0, offsetStart).trim();
            final String offset = limitClause.substring(offsetStart + LIMIT_OFFSET_CLAUSE.length()).trim();
            if (offset.indexOf('?') != -1) {
                throw new FBSQLParseException("Extended limit escape ({limit <rows> offset <offset_rows>} does not support parameters for <offset_rows>");
            }
            return MessageFormat.format("ROWS {0} TO {0} + {1}", offset, rows);
        }
    }

    /**
     * This method converts escaped function to a server function call. Actually
     * we do not change anything here, we hope that all UDF are defined.
     * 
     * @param escapedFunction
     *            escaped function call
     * @return server-side function call.
     * @throws FBSQLParseException
     *             if something was wrong.
     */
    private String convertEscapedFunction(final String escapedFunction) throws FBSQLParseException {
        final String templateResult = FBEscapedFunctionHelper.convertTemplate(escapedFunction, mode);
        return templateResult != null ? templateResult : escapedFunction;
    }

    private enum ParserState {
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
            protected ParserState nextState(char inputChar) {
                if (inputChar == '}') {
                    throw new IllegalStateException("Did not expect end of JDBC escape at this point");
                }
                return ESCAPE_STATE;
            }
        },
        /**
         * Inside JDBC escape
         */
        ESCAPE_STATE {
            @Override
            protected ParserState nextState(char inputChar) {
                return (inputChar == '}') ? ESCAPE_EXIT_STATE : ESCAPE_STATE;
            }
        },
        /**
         * End of JDBC escape (} character encountered)
         */
        ESCAPE_EXIT_STATE {
            @Override
            protected ParserState nextState(char inputChar) {
                throw new IllegalStateException("nextState(char) should never be called on ESCAPE_EXIT_STATE");
            }
        };

        /**
         * Decides on the next ParserState based on the input character.
         * 
         * @param inputChar
         *            Input character
         * @return Next state
         */
        protected abstract ParserState nextState(char inputChar);
    }
}

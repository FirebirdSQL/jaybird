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

/**
 * The class <code>FBEscapedParser</code> parses the SQL and converts escaped
 * syntax to native form.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class FBEscapedParser {

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
     * Currently we have three states, normal when we simply copy characters
     * from source to destination, escape state, when we extract the escaped
     * syntax and  literal, when we do copy characters from  source to
     * destination but we cannot enter the escape state.
     *
     * Currently these states seems to be exclusive, but later meanings may be
     * extended, so we use the bit scale to map the state.
     */
    protected static final int UNDEFINED_STATE = 0;
    protected static final int NORMAL_STATE = 1;
    protected static final int LITERAL_STATE = 2;
    protected static final int ESCAPE_STATE = 4;

    /*
     Stored procedure calls support both following syntax:
        {call procedure_name[(arg1, arg2, ...)]}
     or
        {?= call procedure_name[(arg1, arg2, ...)]}
     */
    public static final String ESCAPE_CALL_KEYWORD = "call";
    public static final String ESCAPE_CALL_KEYWORD3 = "?";
    public static final String ESCAPE_DATE_KEYWORD = "d";
    public static final String ESCAPE_TIME_KEYWORD = "t";
    public static final String ESCAPE_TIMESTAMP_KEYWORD = "ts";
    public static final String ESCAPE_FUNCTION_KEYWORD = "fn";
    public static final String ESCAPE_ESCAPE_KEYWORD = "escape";
    public static final String ESCAPE_OUTERJOIN_KEYWORD = "oj";
    public static final String ESCAPE_LIMIT_KEYWORD = "limit";

    /*
     * These constants are necessary to speed up checking the
     * SQL statements by avoiding the complete parsing if the
     * SQL statement does not contain any of the substrings.
     */
    protected static final String CHECK_CALL_1 = "{call";
    protected static final String CHECK_CALL_2 = "{?";
    protected static final String CHECK_DATE = "{d";
    protected static final String CHECK_TIME = "{t";
    protected static final String CHECK_TIMESTAMP = "{ts";
    protected static final String CHECK_FUNCTION = "{fn";
    protected static final String CHECK_ESCAPE = "{escape";
    protected static final String CHECK_OUTERJOIN = "{oj";
    protected static final String CHECK_LIMIT = "{limit";
    
    protected static final String LIMIT_OFFSET_CLAUSE = " offset ";

    private int state = NORMAL_STATE;
    private int lastState = NORMAL_STATE;
    private int nestedEscaped = 0;
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
     * Returns the current state.
     */
    protected int getLastState() {
        return lastState;
    }

    /**
     * Returns the current state.
     */
    protected int getState() {
        return state;
    }

    /**
     * Sets the current state.
     * 
     * @param newState
     *            state to enter.
     * @throws <code>java.lang.IllegalStateException</code> if the system cannot
     *         enter the desired state.
     */
    protected void setState(final int newState) throws IllegalStateException {
        final int tempState = getLastState();
        lastState = getState();
        switch (newState) {
        case NORMAL_STATE:
        case LITERAL_STATE:
        case ESCAPE_STATE:
            state = newState;
            break;
        default:
            lastState = tempState;
            throw new IllegalStateException("State " + newState + " is unknown.");
        }
    }

    /**
     * Returns if the system is in state <code>state</code>.
     * 
     * @param state
     *            state we're testing
     * @return <code>true</code> if the system is in state <code>state</code>.
     */
    protected boolean isInState(final int state) {
        return getState() == state;
    }

    /**
     * Returns if the system was in state <code>state</code>.
     * 
     * @param state
     *            state we're testing
     * @return <code>true</code> if the system was in state <code>state</code>.
     */
    protected boolean wasInState(final int state) {
        return getLastState() == state;
    }

    /**
     * Test the character to be the state switching character and switches the
     * state if necessary.
     * 
     * @param testChar
     *            character to test
     */
    protected void switchState(final char testChar) {
        switch (testChar) {
        case '\'':
            if (isInState(NORMAL_STATE))
                setState(LITERAL_STATE);
            else if (isInState(LITERAL_STATE))
            	setState(NORMAL_STATE);
            break;
        case '{':
            if (isInState(NORMAL_STATE))
            	setState(ESCAPE_STATE);
            nestedEscaped++;
            break;
        case '}':
            if (isInState(ESCAPE_STATE)) {
                nestedEscaped--;
                if (nestedEscaped == 0)
                	setState(NORMAL_STATE);
            }
            break;
        }
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
    protected boolean checkForEscapes(String sql) {
        sql = sql.toLowerCase();

//@formatter:off
        return  sql.indexOf(CHECK_CALL_1) != -1 ||
                sql.indexOf(CHECK_CALL_2) != -1 ||
                sql.indexOf(CHECK_DATE) != -1 ||
                sql.indexOf(CHECK_ESCAPE) != -1 ||
                sql.indexOf(CHECK_FUNCTION) != -1 ||
                sql.indexOf(CHECK_OUTERJOIN) != -1 ||
                sql.indexOf(CHECK_TIME) != -1 ||
                sql.indexOf(CHECK_TIMESTAMP) != -1 ||
                sql.indexOf(CHECK_LIMIT) != -1;
//@formatter:on
    }

    /**
     * Converts escaped parts in the passed SQL to native representation.
     * 
     * @param sql
     *            to parse
     * @return native form of the <code>sql</code>.
     */
    public String parse(final String sql) throws FBSQLException {
        lastState = NORMAL_STATE;
        state = NORMAL_STATE;
        nestedEscaped = 0;

        if (!checkForEscapes(sql)) return sql;

        final char[] sqlbuff = sql.toCharArray();
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder escape = new StringBuilder();

        for (int i = 0; i < sqlbuff.length; i++) {
            switchState(sqlbuff[i]);

            if (isInState(NORMAL_STATE) && (wasInState(NORMAL_STATE) || wasInState(LITERAL_STATE)))
                buffer.append(sqlbuff[i]);
            else if (isInState(NORMAL_STATE) && wasInState(ESCAPE_STATE)) {
                // escape now is in form "{...." without trailing '}'...
                buffer.append(escapeToNative(escape.substring(1, escape.length())));
                escape.setLength(0);
                setState(NORMAL_STATE);
            } else if (isInState(ESCAPE_STATE))
                escape.append(sqlbuff[i]);
            else if (isInState(LITERAL_STATE)) buffer.append(sqlbuff[i]);
        }
        return buffer.toString();
    }

    protected void processEscaped(final String escaped, final StringBuilder keyword, final StringBuilder payload) {
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
    protected String escapeToNative(final String escaped) throws FBSQLException {
        final StringBuilder keyword = new StringBuilder();
        final StringBuilder payload = new StringBuilder();

        processEscaped(escaped, keyword, payload);

        //Handle keywords.
        final String keywordStr = keyword.toString();
        if (keywordStr.equalsIgnoreCase(ESCAPE_CALL_KEYWORD)) {
            StringBuilder call = new StringBuilder();
            call.append('{')
                .append(keyword)
                .append(' ')
                .append(payload)
                .append('}');
            return convertProcedureCall(call.toString());
        } else if (keywordStr.equalsIgnoreCase(ESCAPE_CALL_KEYWORD3)) {
            StringBuilder call = new StringBuilder();
            call.append('{')
                .append(ESCAPE_CALL_KEYWORD3)
                .append(payload)
                .append('}');
            return convertProcedureCall(call.toString());
        } else if (keywordStr.equalsIgnoreCase(ESCAPE_DATE_KEYWORD))
            return toDateString(payload.toString().trim());
        else if (keywordStr.equalsIgnoreCase(ESCAPE_ESCAPE_KEYWORD))
            return convertEscapeString(payload.toString().trim());
        else if (keywordStr.equalsIgnoreCase(ESCAPE_FUNCTION_KEYWORD))
            return convertEscapedFunction(payload.toString().trim());
        else if (keywordStr.equalsIgnoreCase(ESCAPE_OUTERJOIN_KEYWORD))
            return convertOuterJoin(payload.toString().trim());
        else if (keywordStr.equalsIgnoreCase(ESCAPE_TIME_KEYWORD))
            return toTimeString(payload.toString().trim());
        else if (keywordStr.equalsIgnoreCase(ESCAPE_TIMESTAMP_KEYWORD))
            return toTimestampString(payload.toString().trim());
        else if (keywordStr.equalsIgnoreCase(ESCAPE_LIMIT_KEYWORD))
            return convertLimitString(payload.toString().trim());
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
    protected String toDateString(final String dateStr) throws FBSQLParseException {
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
    protected String toTimeString(final String timeStr) throws FBSQLParseException {
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
    protected String toTimestampString(final String timestampStr) throws FBSQLParseException {
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
    protected String convertProcedureCall(final String procedureCall) throws FBSQLException {
        FBEscapedCallParser tempParser = new FBEscapedCallParser(mode);
        FBProcedureCall call = tempParser.parseCall(procedureCall);
        return call.getSQL(false);
    }

    /**
     * This method converts the escaped outer join call syntax into the native
     * outer join. Actually, we do not change anything here, since Firebird's
     * syntax is the same.
     */
    protected String convertOuterJoin(final String outerJoin) throws FBSQLParseException {
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
    protected String convertEscapeString(final String escapeString) {
        return "ESCAPE " + escapeString;
    }

    /**
     * Convert the <code>"{limit &lt;rows&gt; [offset &lt;rows_offset&gt;]}"</code> call into the corresponding rows
     * clause for Firebird.
     * <p>
     * NOTE: We assume that the {limit ...} escape occurs in the right place to
     * work for a
     * <code><a href="http://www.firebirdsql.org/file/documentation/reference_manuals/reference_material/html/langrefupd25-select.html#langrefupd25-select-rows">ROWS</a></code>
     * clause in Firebird.
     * </p>
     * <p>
     * This implementation supports a parameter for the value of &lt;rows&gt;, but not for &lt;rows_offset&gt;.
     * </p>
     * 
     * @param limitClause
     *            Limit clause
     * @return converted code
     */
    protected String convertLimitString(final String limitClause) throws FBSQLParseException {
        final int offsetStart = limitClause.toLowerCase().indexOf(LIMIT_OFFSET_CLAUSE.toLowerCase());
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
    protected String convertEscapedFunction(final String escapedFunction) throws FBSQLParseException {
        final String templateResult = FBEscapedFunctionHelper.convertTemplate(escapedFunction, mode);
        return templateResult != null ? templateResult : escapedFunction;
    }

    public static boolean supportsStoredProcedures() {
        return true;
    }

    public static boolean supportsLikeEscapeClause() {
        return true;
    }
}

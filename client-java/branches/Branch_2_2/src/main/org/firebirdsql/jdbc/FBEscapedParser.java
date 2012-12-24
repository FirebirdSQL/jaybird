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


/**
 * The class <code>FBEscapedParser</code>  parses the SQL 
 * and converts escaped syntax to native form.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBEscapedParser {
    
    public static final int USE_BUILT_IN = 0;
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
    public static final String ESCAPE_OUTERJOIN_KEYWORS = "oj";

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

    private int state = NORMAL_STATE;
    private int lastState = NORMAL_STATE;
    private int nestedEscaped = 0;

    private int mode;
    
    public FBEscapedParser(int mode) {
        this.mode = mode;
    }
    
    /**
     * Returns the current state.
     */
    protected int getLastState() { return lastState; }

    /**
     * Returns the current state.
     */
    protected int getState() { return state; }

    /**
     * Sets the current state.
     * @param state to enter.
     * @throws <code>java.lang.IllegalStateException</code> if the system
     * cannot enter the desired state.
     */
    protected void setState(int state) throws IllegalStateException {
        int tempState = getLastState();
        lastState = getState();
        if (state == NORMAL_STATE)
            this.state = NORMAL_STATE;
        else
        if (state == LITERAL_STATE)
            this.state = LITERAL_STATE;
        else
        if (state == ESCAPE_STATE)
            this.state = ESCAPE_STATE;
        else {
            lastState = tempState;
            throw new IllegalStateException("State " + state + " is unknown.");
        }
    }

    /**
     * Returns if the system is in state <code>state</code>.
     * @param state we're testing
     * @return <code>true</code> if the system is in state <code>state</code>.
     */
    protected boolean isInState(int state) { return getState() == state; }

    /**
     * Returns if the system was in state <code>state</code>.
     * @param state we're testing
     * @return <code>true</code> if the system was in state <code>state</code>.
     */
    protected boolean wasInState(int state) { return getLastState() == state; }

    /**
     * Test the character to be the state switching character and switches
     * the state if necessary.
     * @param testChar character to test
     */
    protected void switchState(char testChar) {
            switch (testChar) {
                case '\'' : {
                    if (isInState(NORMAL_STATE))
                        setState(LITERAL_STATE);
                    else
                    if (isInState(LITERAL_STATE))
                        setState(NORMAL_STATE);
                    break;
                }
                case '{' : {
                    if (isInState(NORMAL_STATE))
                        setState(ESCAPE_STATE);
                    nestedEscaped++;
                    break;
                }
                case '}' : {
                    if (isInState(ESCAPE_STATE))
                        nestedEscaped--;
                    if (nestedEscaped == 0)
                        setState(NORMAL_STATE);
                    break;
                }
            }
    }

    /**
     * Check if the target SQL contains at least one of the escaped syntax
     * commands. This method performs simple substring matching, so it may
     * report that SQL contains escaped syntax when the <code>"{"</code>
     * is followed by the escaped syntax command in regular string constants
     * that are passed as parameters. In this case {@link #parse(String)} will
     * perform complete SQL parsing.
     *
     * @param sql to test
     * @return <code>true</code> if the <code>sql</code> is suspected to contain
     * escaped syntax.
     */
    protected boolean checkForEscapes(String sql) {
        sql = sql.toLowerCase();
        
        return  sql.indexOf(CHECK_CALL_1) != -1 ||
                sql.indexOf(CHECK_CALL_2) != -1 ||
                sql.indexOf(CHECK_DATE) != -1 ||
                sql.indexOf(CHECK_ESCAPE) != -1 ||
                sql.indexOf(CHECK_FUNCTION) != -1 ||
                sql.indexOf(CHECK_OUTERJOIN) != -1 ||
                sql.indexOf(CHECK_TIME) != -1 ||
                sql.indexOf(CHECK_TIMESTAMP) != -1;
    }

    /**
     * Converts escaped parts in the passed SQL to native representation.
     * @param sql to parse
     * @return native form of the <code>sql</code>.
     */
    public String parse(String sql) throws FBSQLException {
        
        lastState = NORMAL_STATE;
        state = NORMAL_STATE;
        nestedEscaped = 0;

        if (!checkForEscapes(sql))
            return sql;

        char[] sqlbuff = sql.toCharArray();
        StringBuffer buffer = new StringBuffer();
        StringBuffer escape = new StringBuffer();

        for(int i = 0; i < sqlbuff.length; i++) {

            switchState(sqlbuff[i]);

            if (isInState(NORMAL_STATE) &&
                    (wasInState(NORMAL_STATE) || wasInState(LITERAL_STATE)))
                buffer.append(sqlbuff[i]);
            else
            if (isInState(NORMAL_STATE) && wasInState(ESCAPE_STATE)) {
                // escape now is in form "{...." without trailing '}'...
                buffer.append(escapeToNative(escape.substring(1, escape.length())));
                escape = new StringBuffer();
                setState(NORMAL_STATE);
            } else
            if (isInState(ESCAPE_STATE))
                escape.append(sqlbuff[i]);
            else
            if (isInState(LITERAL_STATE))
                buffer.append(sqlbuff[i]);
        }
        return buffer.toString();
    }

    protected void processEscaped(String escaped, StringBuffer keyword,
        StringBuffer payload)
    {
        if (keyword.length() != 0) keyword.delete(0, keyword.length());
        if (payload.length() != 0) payload.delete(0, payload.length());
        /*
         * Extract the keyword from the escaped syntax.
         */
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(escaped);
        int keyStart = iterator.first();
        int keyEnd = iterator.next();
        keyword.append(escaped.substring(keyStart, keyEnd));
        payload.append(escaped.substring(keyEnd, escaped.length()));
    }

    /**
     * This method checks the passed parameter to conform the escaped syntax,
     * checks for the unknown keywords and re-formats result according to the
     * Firebird SQL syntax.
     * @param escaped the part of escaped SQL between the '{' and '}'.
     * @return the native representation of the SQL code.
     */
    protected String escapeToNative(String escaped) throws FBSQLException {
        StringBuffer keyword = new StringBuffer();
        StringBuffer payload = new StringBuffer();

        processEscaped(escaped, keyword, payload);

        /*
         * Handle keywords.
         */
        if (keyword.toString().equalsIgnoreCase(ESCAPE_CALL_KEYWORD)){
            
            StringBuffer call = new StringBuffer();
            call
                .append('{')
                .append(keyword)
                .append(' ')
                .append(payload)
                .append('}');
            
            return convertProcedureCall(call.toString());
        
        } else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_CALL_KEYWORD3)) {
            
            StringBuffer call = new StringBuffer();
            
            call
                .append('{')
                .append(ESCAPE_CALL_KEYWORD3)
                .append(payload)
                .append('}');
            
            return convertProcedureCall(call.toString());
        } else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_DATE_KEYWORD))
            return toDateString(payload.toString().trim());
        else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_ESCAPE_KEYWORD))
            return convertEscapeString(payload.toString().trim());
        else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_FUNCTION_KEYWORD))
            return convertEscapedFunction(payload.toString().trim());
        else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_OUTERJOIN_KEYWORS))
            return convertOuterJoin(payload.toString().trim());
        else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_TIME_KEYWORD))
            return toTimeString(payload.toString().trim());
        else
        if (keyword.toString().equalsIgnoreCase(ESCAPE_TIMESTAMP_KEYWORD))
            return toTimestampString(payload.toString().trim());
        else
            throw new FBSQLParseException(
                "Unknown keyword " + keyword + " for escaped syntax.");
    }

    /**
     * This method converts the 'yyyy-mm-dd' date format into the
     * Firebird understandable format.
     * @param dateStr the date in the 'yyyy-mm-dd' format.
     * @return Firebird understandable date format.
     */
    protected String toDateString(String dateStr) throws FBSQLParseException {
        /*
         * We assume that Firebird can handle the 'yyyy-mm-dd' date format.
         */
        return dateStr;
    }

    /**
     * This method converts the 'hh:mm:ss' time format into the
     * Firebird understandable format.
     * @param timeStr the date in the 'hh:mm:ss' format.
     * @return Firebird understandable date format.
     */
    protected String toTimeString(String timeStr) throws FBSQLParseException {
        /*
         * We assume that Firebird can handle the 'hh:mm:ss' date format.
         */
        return timeStr;
    }

    /**
     * This method converts the 'yyyy-mm-dd hh:mm:ss' timestamp format into the
     * Firebird understandable format.
     * @param timestampStr the date in the 'yyyy-mm-dd hh:mm:ss' format.
     * @return Firebird understandable date format.
     */
    protected String toTimestampString(String timestampStr)
        throws FBSQLParseException
    {
        /*
         * We assume that Firebird can handle the 'dd.mm.yyyy hh:mm:ss' date format.
         */
        return timestampStr;
    }

    /**
     * This methods converts the escaped procedure call syntax into the
     * native procedure call.
     * @param procedureCall part of {call proc_name(...)} without curly braces
     * and "call" word.
     * @return native procedure call.
     */
    protected String convertProcedureCall(String procedureCall)
        throws FBSQLException
    {
        FBEscapedCallParser tempParser = new FBEscapedCallParser(mode);
        FBProcedureCall call = tempParser.parseCall(procedureCall);
        return call.getSQL(false);
    }

    /**
     * This method converts the escaped outer join call syntax into the
     * native outer join. Actually, we do not change anything here, since
     * Firebird's syntax is the same.
     */
    protected String convertOuterJoin(String outerJoin)
        throws FBSQLParseException
    {
        return outerJoin;
    }

    /**
     * Convert the <code>"{escape '...'}"</code> call into the corresponding
     * escape clause for Firebird.
     * 
     * @param escapeString escape string to convert
     * 
     * @return converted code.
     */
    protected String convertEscapeString(String escapeString) {
        return "ESCAPE " + escapeString;
    }
    
    /**
     * This method converts escaped function to a server function call. Actually
     * we do not change anything here, we hope that all UDF are defined.
     * 
     * @param escapedFunction escaped function call
     * @return server-side function call.
     * 
     * @throws FBSQLParseException if something was wrong.
     */
    protected String convertEscapedFunction(String escapedFunction)
        throws FBSQLParseException
    {
        String templateResult = 
            FBEscapedFunctionHelper.convertTemplate(escapedFunction, mode);
        
        if (templateResult != null)
            return templateResult;
        else
            return escapedFunction;
    }

    public static boolean supportsStoredProcedures() {
        return true;
    }

    public static boolean supportsLikeEscapeClause() {
        return false;
    }
}

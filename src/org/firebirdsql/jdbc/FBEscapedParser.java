/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Original developer Roman Rokytskyy
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

/*
 * CVS modification log:
 * $Log$
 */

package org.firebirdsql.jdbc;

/**
 * This class parses the SQL and converts escaped syntax to native form.
 */
public class FBEscapedParser {
    /*
     * Currently we have three states, normal when we simply copy charactes
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

    public static final String ESCAPE_CALL_KEYWORD = "call";
    public static final String ESCAPE_DATE_KEYWORD = "d";
    public static final String ESCAPE_TIME_KEYWORD = "t";
    public static final String ESCAPE_TIMESTAMP_KEYWORD = "ts";
    public static final String ESCAPE_FUNCTION_KEYWORD = "fn";
    public static final String ESCAPE_ESCAPE_KEYWORD = "escape";
    public static final String ESCAPE_OUTERJOIN_KEYWORS = "oj";

    private int state = NORMAL_STATE;
    private int lastState = NORMAL_STATE;

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
                    break;
                }
                case '}' : {
                    if (isInState(ESCAPE_STATE))
                        setState(NORMAL_STATE);
                    break;
                }
            }
    }

    /**
     * Converts escaped parts in the passed SQL to native representation.
     * @param sql to parse
     * @return native form of the <code>sql</code>.
     */
    public String parse(String sql) throws FBSQLParseException {
        String buffer = "";
        String escape = "";
        for(int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            switchState(ch);
            if (isInState(NORMAL_STATE) &&
                    (wasInState(NORMAL_STATE) || wasInState(LITERAL_STATE)))
                buffer += ch;
            else
            if (isInState(NORMAL_STATE) && wasInState(ESCAPE_STATE)) {
                // escape now is in form "{...." without trailing '}'...
                buffer += escapeToNative(escape.substring(1, escape.length()));
                escape = "";
                setState(NORMAL_STATE);
            } else
            if (isInState(ESCAPE_STATE))
                escape += ch;
            else
            if (isInState(LITERAL_STATE))
                buffer += ch;
        }
        return buffer;
    }

    /**
     * This method checks the passed parameter to conform the escaped syntax,
     * checks for the unknown keywords and re-formats result according to the
     * Firebird SQL syntax.
     * @param escaped the part of escaped SQL between the '{' and '}'.
     * @return the native representation of the SQL code.
     */
    protected String escapeToNative(String escaped) throws FBSQLParseException {
        String keyword = "";
        String payload = "";

        /*
         * Extract the keyword from the escaped syntax.
         */
        java.text.BreakIterator iterator =
            java.text.BreakIterator.getWordInstance();
        iterator.setText(escaped);
        int keyStart = iterator.first();
        int keyEnd = iterator.next();
        keyword = escaped.substring(keyStart, keyEnd);
        payload = escaped.substring(keyEnd, escaped.length());

        /*
         * Handle keywords.
         */
        if (keyword.equalsIgnoreCase(ESCAPE_CALL_KEYWORD))
            throw new FBSQLParseException(
                "Escaped procedure calls are not supported. " +
                "Use native EXECUTE PROCEDURE <proc_name> or " +
                " SELECT * FROM <proc_name> calls instead.");
        else
        if (keyword.equalsIgnoreCase(ESCAPE_DATE_KEYWORD))
            return toDateString(payload.trim());
        else
        if (keyword.equalsIgnoreCase(ESCAPE_ESCAPE_KEYWORD))
            throw new FBSQLParseException(
                "Escaped escapes are not supported.");
        else
        if (keyword.equalsIgnoreCase(ESCAPE_FUNCTION_KEYWORD))
            throw new FBSQLParseException(
                "Escaped functions are not supported.");
        else
        if (keyword.equalsIgnoreCase(ESCAPE_OUTERJOIN_KEYWORS))
            throw new FBSQLParseException(
                "Escaped outer joins are not supported.");
        else
        if (keyword.equalsIgnoreCase(ESCAPE_TIME_KEYWORD))
            return toTimeString(payload.trim());
        else
        if (keyword.equalsIgnoreCase(ESCAPE_TIMESTAMP_KEYWORD))
            return toTimestampString(payload.trim());
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
     * @param dateStr the date in the 'hh:mm:ss' format.
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
     * @param dateStr the date in the 'yyyy-mm-dd hh:mm:ss' format.
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

}
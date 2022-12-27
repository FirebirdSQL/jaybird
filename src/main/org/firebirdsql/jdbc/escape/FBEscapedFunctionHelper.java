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

import org.firebirdsql.util.InternalApi;

import java.util.*;

/**
 * Helper class for escaped functions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@InternalApi
public class FBEscapedFunctionHelper {

    /**
     * This map contains mapping between JDBC function names and Firebird ones.
     * Mapping to null means: execute as is (might fail if there is no built-in or UDF)
     */
    private static final Map<String, SQLFunction> FUNCTION_MAP;

    /**
     * Supported numeric functions
     */
    private static final Set<String> SUPPORTED_NUMERIC_FUNCTIONS;

    /**
     * Supported string functions
     */
    private static final Set<String> SUPPORTED_STRING_FUNCTIONS;

    /**
     * Supported time and date functions
     */
    private static final Set<String> SUPPORTED_TIME_DATE_FUNCTIONS;

    /**
     * Supported system functions
     */
    private static final Set<String> SUPPORTED_SYSTEM_FUNCTIONS;

    static {
        final Map<String, SQLFunction> functionMap = new HashMap<>(71);
        /* Numeric Functions */
        Map<String, SQLFunction> numericFunctionMap = getNumericFunctions();
        SUPPORTED_NUMERIC_FUNCTIONS = Collections.unmodifiableSet(new HashSet<>(numericFunctionMap.keySet()));
        functionMap.putAll(numericFunctionMap);

        /* String Functions */
        Map<String, SQLFunction> stringFunctionMap = getStringFunctions();
        SUPPORTED_STRING_FUNCTIONS = Collections.unmodifiableSet(new HashSet<>(stringFunctionMap.keySet()));
        functionMap.putAll(stringFunctionMap);

        /* Time and Date Functions */
        Map<String, SQLFunction> timeDateFunctionMap = getTimeDateFunctions();
        SUPPORTED_TIME_DATE_FUNCTIONS = Collections.unmodifiableSet(new HashSet<>(timeDateFunctionMap.keySet()));
        functionMap.putAll(timeDateFunctionMap);

        /* System Functions */
        Map<String, SQLFunction> systemFunctionMap = getSystemFunctions();
        SUPPORTED_SYSTEM_FUNCTIONS = Collections.unmodifiableSet(new HashSet<>(systemFunctionMap.keySet()));
        functionMap.putAll(systemFunctionMap);

        /* Conversion Functions */
        functionMap.put("CONVERT", new ConvertFunction());

        // Unsupported functions defined in appendix D that might accidentally work due to UDFs
        // Numerics
        functionMap.put("RAND", null);

        // String
        functionMap.put("DIFFERENCE", null);
        functionMap.put("SOUNDEX", null);

        // Time and date
        // ...none

        // System
        // ...none

        FUNCTION_MAP = Collections.unmodifiableMap(functionMap);
    }

    private static Map<String, SQLFunction> getNumericFunctions() {
        Map<String, SQLFunction> functionMap = new HashMap<>(32);
        functionMap.put("ABS", null);
        functionMap.put("ACOS", null);
        functionMap.put("ASIN", null);
        functionMap.put("ATAN", null);
        functionMap.put("ATAN2", null);
        functionMap.put("CEILING", null);
        functionMap.put("COS", null);
        functionMap.put("COT", null);
        functionMap.put("DEGREES", new PatternSQLFunction("(({0})*180.0/PI())"));
        functionMap.put("EXP", null);
        functionMap.put("FLOOR", null);
        functionMap.put("LOG", new PatternSQLFunction("LN({0})"));
        functionMap.put("LOG10", null);
        functionMap.put("MOD", null);
        functionMap.put("PI", null);
        functionMap.put("POWER", null);
        functionMap.put("RADIANS", new PatternSQLFunction("(({0})*PI()/180.0)"));
        functionMap.put("ROUND", null);
        functionMap.put("SIGN", null);
        functionMap.put("SIN", null);
        functionMap.put("SQRT", null);
        functionMap.put("TAN", null);
        functionMap.put("TRUNCATE", new PatternSQLFunction("TRUNC({0},{1})"));

        return functionMap;
    }

    private static Map<String, SQLFunction> getStringFunctions() {
        Map<String, SQLFunction> functionMap = new HashMap<>(32);
        functionMap.put("ASCII", new PatternSQLFunction("ASCII_VAL({0})"));
        functionMap.put("CHAR", new PatternSQLFunction("ASCII_CHAR({0})"));
        CharacterLengthFunction characterLengthFunction = new CharacterLengthFunction();
        functionMap.put("CHAR_LENGTH", characterLengthFunction);
        functionMap.put("CHARACTER_LENGTH", characterLengthFunction);
        functionMap.put("CONCAT", new PatternSQLFunction("({0}||{1})"));
        functionMap.put("INSERT", new PatternSQLFunction("OVERLAY({0} PLACING {3} FROM {1} FOR {2})"));
        functionMap.put("LCASE", new PatternSQLFunction("LOWER({0})"));
        functionMap.put("LEFT", null);
        functionMap.put("LENGTH", new LengthFunction());
        functionMap.put("LOCATE", new LocateFunction());
        functionMap.put("LTRIM", new PatternSQLFunction("TRIM(LEADING FROM {0})"));
        functionMap.put("OCTET_LENGTH", null);
        // NOTE We're only supporting CHARACTERS optional parameter (OCTETS unclear or technically not possible)
        functionMap.put("POSITION", new PositionFunction());
        // Doubling of single quotes due to MessageFormat requirements
        functionMap.put("REPEAT", new PatternSQLFunction("RPAD('''',{1},{0})"));
        functionMap.put("REPLACE", null);
        functionMap.put("RIGHT", null);
        functionMap.put("RTRIM", new PatternSQLFunction("TRIM(TRAILING FROM {0})"));
        // Doubling of single quotes due to MessageFormat requirements
        functionMap.put("SPACE", new PatternSQLFunction("RPAD('''',{0})"));
        // NOTE We're not discerning between optional CHARACTERS / OCTETS parameter (technically not possible?)
        functionMap.put("SUBSTRING", new PatternSQLFunction("SUBSTRING({0} FROM {1} FOR {2})"));
        functionMap.put("UCASE", new PatternSQLFunction("UPPER({0})"));

        return functionMap;
    }

    private static Map<String, SQLFunction> getTimeDateFunctions() {
        Map<String, SQLFunction> functionMap = new HashMap<>(32);
        ConstantSQLFunction currentDate = new ConstantSQLFunction("CURRENT_DATE");
        functionMap.put("CURRENT_DATE", currentDate);
        ConstantSQLFunction currentTime = new ConstantSQLFunction("CURRENT_TIME");
        functionMap.put("CURRENT_TIME", currentTime);
        ConstantSQLFunction currentTimestamp = new ConstantSQLFunction("CURRENT_TIMESTAMP");
        functionMap.put("CURRENT_TIMESTAMP", currentTimestamp);
        functionMap.put("CURDATE", currentDate);
        functionMap.put("CURTIME", currentTime);
        functionMap.put("DAYNAME", new PatternSQLFunction("trim(decode(extract(weekday from {0}), "
                + "0, ''Sunday'', "
                + "1, ''Monday'', "
                + "2, ''Tuesday'', "
                + "3, ''Wednesday'', "
                + "4, ''Thursday'', "
                + "5, ''Friday'', "
                + "6, ''Saturday''))"));
        functionMap.put("DAYOFMONTH", new PatternSQLFunction("EXTRACT(DAY FROM {0})"));
        functionMap.put("DAYOFWEEK", new PatternSQLFunction("EXTRACT(WEEKDAY FROM {0})+1"));
        functionMap.put("DAYOFYEAR", new PatternSQLFunction("EXTRACT(YEARDAY FROM {0})+1"));
        functionMap.put("EXTRACT", null);
        functionMap.put("HOUR", new PatternSQLFunction("EXTRACT(HOUR FROM {0})"));
        functionMap.put("MINUTE", new PatternSQLFunction("EXTRACT(MINUTE FROM {0})"));
        functionMap.put("MONTH", new PatternSQLFunction("EXTRACT(MONTH FROM {0})"));
        functionMap.put("MONTHNAME", new PatternSQLFunction("trim(decode(extract(month from {0}), "
                + "1, ''January'', "
                + "2, ''February'', "
                + "3, ''March'', "
                + "4, ''April'', "
                + "5, ''May'', "
                + "6, ''June'', "
                + "7, ''July'', "
                + "8, ''August'', "
                + "9, ''September'', "
                + "10, ''October'', "
                + "11, ''November'', "
                + "12, ''December''))"));
        functionMap.put("NOW", currentTimestamp);
        functionMap.put("QUARTER", new PatternSQLFunction("(1+(EXTRACT(MONTH FROM {0})-1)/3)"));
        functionMap.put("SECOND", new PatternSQLFunction("EXTRACT(SECOND FROM {0})"));
        functionMap.put("TIMESTAMPADD", new TimestampAddFunction());
        functionMap.put("TIMESTAMPDIFF", new TimestampDiffFunction());
        functionMap.put("WEEK", new PatternSQLFunction("EXTRACT(WEEK FROM {0})"));
        functionMap.put("YEAR", new PatternSQLFunction("EXTRACT(YEAR FROM {0})"));
        
        return functionMap;
    }

    private static Map<String, SQLFunction> getSystemFunctions() {
        Map<String, SQLFunction> functionMap = new HashMap<>(4, 1.0f);
        functionMap.put("DATABASE", new ConstantSQLFunction("RDB$GET_CONTEXT('SYSTEM', 'DB_NAME')"));
        functionMap.put("IFNULL", new PatternSQLFunction("COALESCE({0}, {1})"));
        functionMap.put("USER", new ConstantSQLFunction("USER"));

        return functionMap;
    }

    /**
     * Simple syntax check if function is specified in form "name(...)".
     *
     * @param functionCall
     *         string representing function call.
     * @throws FBSQLParseException
     *         if simple syntax check failed.
     */
    private static void checkSyntax(String functionCall) throws FBSQLParseException {
        // NOTE: Some function calls don't require parenthesis eg CURRENT_TIMESTAMP
        int parenthesisStart = functionCall.indexOf('(');
        if (parenthesisStart != -1 && functionCall.charAt(functionCall.length() - 1) != ')')
            throw new FBSQLParseException("No closing parenthesis found, not a function call.");
    }

    /**
     * Extract function name from the function call.
     *
     * @param functionCall
     *         escaped function call.
     * @return name of the function.
     * @throws FBSQLParseException
     *         if parse error occurs.
     */
    public static String parseFunction(String functionCall) throws FBSQLParseException {
        functionCall = functionCall.trim();
        checkSyntax(functionCall);
        int parenthesisStart = functionCall.indexOf('(');

        return parenthesisStart != -1 ? functionCall.substring(0, parenthesisStart) : functionCall;
    }

    /**
     * Extract function arguments from the function call. This method parses
     * escaped function call string and extracts function parameters from it.
     *
     * @param functionCall
     *         escaped function call.
     * @return list of parameters of the function.
     * @throws FBSQLParseException
     *         if parse error occurs.
     */
    public static List<String> parseArguments(String functionCall) throws FBSQLParseException {
        functionCall = functionCall.trim();
        checkSyntax(functionCall);

        final int parenthesisStart = functionCall.indexOf('(');
        if (parenthesisStart == -1) {
            return Collections.emptyList();
        }
        final String paramsString = functionCall.substring(parenthesisStart + 1, functionCall.length() - 1);

        final List<String> params = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        boolean inDoubleQuotes = false;
        // ignore initial whitespace
        boolean coalesceSpace = true;
        int nestedParentheses = 0;

        for (int i = 0, n = paramsString.length(); i < n; i++) {
            char currentChar = paramsString.charAt(i);
            // we coalesce spaces, tabs and new lines into a single space if
            // we are not in a string literal
            if (Character.isWhitespace(currentChar)) {
                if (inQuotes || inDoubleQuotes) {
                    sb.append(currentChar);
                } else if (!coalesceSpace) {
                    sb.append(' ');
                    coalesceSpace = true;
                }
                continue;
            }
            switch (currentChar) {
            case '\'':
                sb.append(currentChar);
                if (!inDoubleQuotes)
                    inQuotes = !inQuotes;
                coalesceSpace = false;
                break;
            case '"':
                sb.append(currentChar);
                if (!inQuotes)
                    inDoubleQuotes = !inDoubleQuotes;
                coalesceSpace = false;
                break;
            case '(':
                if (!(inQuotes || inDoubleQuotes)) {
                    nestedParentheses++;
                }
                sb.append('(');
                coalesceSpace = false;
                break;
            case ')':
                if (!(inQuotes || inDoubleQuotes)) {
                    nestedParentheses--;
                    if (nestedParentheses < 0) {
                        throw new FBSQLParseException("Unbalanced parentheses in parameters at position " + i);
                    }
                }
                sb.append(')');
                coalesceSpace = false;
                break;
            // comma is considered parameter separator
            // if it is not within the string literal or within parentheses 
            case ',':
                if (inQuotes || inDoubleQuotes || nestedParentheses > 0) {
                    sb.append(currentChar);
                } else {
                    params.add(sb.toString());
                    sb.setLength(0);
                    // Ignore whitespace after parameter
                    coalesceSpace = true;
                }
                break;
            // by default we add chars to the buffer  
            default:
                sb.append(currentChar);
                coalesceSpace = false;
            }
        }

        // add last parameter if present
        if (sb.length() > 0)
            params.add(sb.toString());

        // after processing all parameters all string literals should be closed
        if (inQuotes || inDoubleQuotes) {
            throw new FBSQLParseException("String literal is not properly closed.");
        }
        if (nestedParentheses != 0) {
            throw new FBSQLParseException("Unbalanced parentheses in parameters.");
        }

        return params;
    }

    /**
     * Convert escaped function call using function template.
     *
     * @param functionCall
     *         escaped function call.
     * @return server-side representation of the function call or <code>null</code>
     * if no template found.
     * @throws FBSQLParseException
     *         if escaped function call has incorrect syntax.
     */
    public static String convertTemplate(final String functionCall) throws FBSQLParseException {
        final String functionName = parseFunction(functionCall).toUpperCase(Locale.ROOT);
        final String[] params = parseArguments(functionCall).toArray(new String[0]);

        if (!FUNCTION_MAP.containsKey(functionName)) {
            /* See 13.4.1 of JDBC 4.1 spec:
             * "The escape syntax for scalar functions must only be used to invoke the scalar
             * functions defined in Appendix D "Scalar Functions". The escape syntax is not
             * intended to be used to invoke user-defined or vendor specific scalar functions."
             */
            throw new FBSQLParseException("Unsupported JDBC function escape: " + functionName);
        }

        final SQLFunction firebirdTemplate = FUNCTION_MAP.get(functionName);

        if (firebirdTemplate != null) {
            return firebirdTemplate.apply(params);
        }

        return null;
    }

    /**
     * @return Set of JDBC numeric functions supported (as defined in appendix C.1 of JDBC 4.3)
     */
    public static Set<String> getSupportedNumericFunctions() {
        return SUPPORTED_NUMERIC_FUNCTIONS;
    }

    /**
     * @return Set of JDBC string functions supported (as defined in appendix C.2 of JDBC 4.3)
     */
    public static Set<String> getSupportedStringFunctions() {
        return SUPPORTED_STRING_FUNCTIONS;
    }

    /**
     * @return Set of JDBC time and date functions supported (as defined in appendix C.3 of JDBC 4.3)
     */
    public static Set<String> getSupportedTimeDateFunctions() {
        return SUPPORTED_TIME_DATE_FUNCTIONS;
    }

    /**
     * @return Set of JDBC system functions supported (as defined in appendix C.4 of JDBC 4.3)
     */
    public static Set<String> getSupportedSystemFunctions() {
        return SUPPORTED_SYSTEM_FUNCTIONS;
    }
}

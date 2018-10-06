/*
 * Firebird Open Source JavaEE connector - JDBC driver
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

import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Helper class for escaped functions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
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
        functionMap.put("DAYNAME", null); // TODO Implement with DECODE or CASE?
        functionMap.put("MONTHNAME", null); // TODO Implement with DECODE or CASE?

        // System
        functionMap.put("DATABASE", null); // TODO Implement with RDB$GET_CONTEXT

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
        functionMap.put("DAYOFMONTH", new PatternSQLFunction("EXTRACT(DAY FROM {0})"));
        functionMap.put("DAYOFWEEK", new PatternSQLFunction("EXTRACT(WEEKDAY FROM {0})+1"));
        functionMap.put("DAYOFYEAR", new PatternSQLFunction("EXTRACT(YEARDAY FROM {0})+1"));
        functionMap.put("EXTRACT", null);
        functionMap.put("HOUR", new PatternSQLFunction("EXTRACT(HOUR FROM {0})"));
        functionMap.put("MINUTE", new PatternSQLFunction("EXTRACT(MINUTE FROM {0})"));
        functionMap.put("MONTH", new PatternSQLFunction("EXTRACT(MONTH FROM {0})"));
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
        Map<String, SQLFunction> functionMap = new HashMap<>(2, 1.0f);
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
    public static String convertTemplate(final String functionCall, final EscapeParserMode mode) throws FBSQLParseException {
        final String functionName = parseFunction(functionCall).toUpperCase();
        final String[] params = parseArguments(functionCall).toArray(new String[0]);

        if (!FUNCTION_MAP.containsKey(functionName)) {
            /* See 13.4.1 of JDBC 4.1 spec:
             * "The escape syntax for scalar functions must only be used to invoke the scalar
             * functions defined in Appendix D "Scalar Functions". The escape syntax is not
             * intended to be used to invoke user-defined or vendor specific scalar functions."
             */
            // TODO Consider throwing SQLFeatureNotSupported or a different SQLException
            throw new FBSQLParseException("Unsupported JDBC function escape: " + functionName);
        }

        final SQLFunction firebirdTemplate = FUNCTION_MAP.get(functionName);

        if (firebirdTemplate != null) {
            return firebirdTemplate.apply(params);
        }

        if (mode == EscapeParserMode.USE_STANDARD_UDF) {
            return convertUsingStandardUDF(functionName, params);
        }

        return null;
    }

    /*
     * Functions below are conversion routines of the escaped function calls
     * into the standard UDF library functions. The conversion function must
     * have the name equal to the function name in the escaped syntax in the
     * lower case and should take array of strings as parameter and it may throw
     * the FBSQLParseException and must be declared as static and have public
     * visibility. It should return a string of the converted function call.
     */

    // TODO Replace with PatternSQLFunction?

    private static String convertUsingStandardUDF(String name, String[] params) throws FBSQLParseException {
        try {
            name = name.toLowerCase();
            Method method = FBEscapedFunctionHelper.class.getMethod(name, new Class[] { String[].class });
            return (String) method.invoke(null, new Object[] { params });
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new FBSQLParseException("Error when converting function "
                    + name + ". Error " + ex.getClass().getName() +
                    " : " + ex.getMessage());
        }
    }

    /*
     * Mathematical functions
     */

    /**
     * Produce a function call for the <code>abs</code> UDF function.
     * The syntax of the <code>abs</code> function is
     * <code>{fn abs(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String abs(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function abs : " + params.length);

        return "abs(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>acos</code> UDF function.
     * The syntax of the <code>acos</code> function is
     * <code>{fn acos(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String acos(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function acos : " + params.length);

        return "acos(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>asin</code> UDF function.
     * The syntax of the <code>asin</code> function is
     * <code>{fn asin(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String asin(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function asin : " + params.length);

        return "asin(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>atan</code> UDF function.
     * The syntax of the <code>atan</code> function is
     * <code>{fn atan(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String atan(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function atan : " + params.length);

        return "atan(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>atan2</code> UDF function.
     * The syntax of the <code>atan2</code> function is
     * <code>{fn atan2(float1, float2)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String atan2(String[] params) throws FBSQLParseException {
        if (params.length != 2)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function atan2 : " + params.length);

        return "atan2(" + params[0] + ", " + params[1] + ")";
    }

    /**
     * Produce a function call for the <code>ceiling</code> UDF function.
     * The syntax of the <code>ceiling</code> function is
     * <code>{fn ceiling(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String ceiling(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function ceiling : " + params.length);

        return "ceiling(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>cos</code> UDF function.
     * The syntax of the <code>cos</code> function is
     * <code>{fn cos(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String cos(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function cos : " + params.length);

        return "cos(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>cot</code> UDF function.
     * The syntax of the <code>cot</code> function is
     * <code>{fn cot(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String cot(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function cot : " + params.length);

        return "cot(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>floor</code> UDF function.
     * The syntax of the <code>floor</code> function is
     * <code>{fn floor(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String floor(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function floor : " + params.length);

        return "floor(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>log10</code> UDF function.
     * The syntax of the <code>log10</code> function is
     * <code>{fn log10(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String log10(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function log10 : " + params.length);

        return "log10(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>mod</code> UDF function.
     * The syntax of the <code>mod</code> function is
     * <code>{fn mod(integer1, integer2)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String mod(String[] params) throws FBSQLParseException {
        if (params.length != 2)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function mod : " + params.length);

        return "mod(" + params[0] + ", " + params[1] + ")";
    }

    /**
     * Produce a function call for the <code>pi</code> UDF function.
     * The syntax of the <code>pi</code> function is <code>{fn pi()}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String pi(String[] params) throws FBSQLParseException {
        if (params.length != 0)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function pi : " + params.length);

        return "pi()";
    }

    /**
     * Produce a function call for the <code>rand</code> UDF function.
     * The syntax for the <code>rand</code> function is
     * <code>{fn rand()}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String rand(String[] params) throws FBSQLParseException {
        if (params.length != 0)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function rand : " + params.length);

        return "rand()";
    }

    /**
     * Produce a function call for the <code>sign</code> UDF function.
     * The syntax for the <code>sign</code> function is
     * <code>{fn sign(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String sign(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function sign : " + params.length);

        return "sign(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>sin</code> UDF function.
     * The syntax for the <code>sin</code> function is
     * <code>{fn sin(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String sin(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function sin : " + params.length);

        return "sin(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>sqrt</code> UDF function.
     * The syntax for the <code>sqrt</code> function is
     * <code>{fn sqrt(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String sqrt(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function sqrt : " + params.length);

        return "sqrt(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>tan</tan> UDF function.
     * The syntax for the <code>tan</code> function is
     * <code>{fn tan(float)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String tan(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function tan : " + params.length);

        return "tan(" + params[0] + ")";
    }

    /**
     * @return Set of JDBC numeric functions supported (as defined in appendix D.1 of JDBC 4.1)
     */
    public static Set<String> getSupportedNumericFunctions() {
        return SUPPORTED_NUMERIC_FUNCTIONS;
    }

    /**
     * @return Set of JDBC string functions supported (as defined in appendix D.2 of JDBC 4.1)
     */
    public static Set<String> getSupportedStringFunctions() {
        return SUPPORTED_STRING_FUNCTIONS;
    }

    /**
     * @return Set of JDBC time and date functions supported (as defined in appendix D.3 of JDBC 4.1)
     */
    public static Set<String> getSupportedTimeDateFunctions() {
        return SUPPORTED_TIME_DATE_FUNCTIONS;
    }

    /**
     * @return Set of JDBC system functions supported (as defined in appendix D.4 of JDBC 4.1)
     */
    public static Set<String> getSupportedSystemFunctions() {
        return SUPPORTED_SYSTEM_FUNCTIONS;
    }
}

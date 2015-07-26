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
package org.firebirdsql.jdbc.escape;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;

/**
 * Helper class for escaped functions.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBEscapedFunctionHelper {
    
    /**
     * This map contains mapping between JDBC function names and Firebird ones.
     * Mapping to null means: execute as is (might fail if there is no built-in or UDF)
     */
    private static final Map<String, String> FUNCTION_MAP;
    
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
        Map<String, String> functionMap = new HashMap<String, String>();
        /* Numeric Functions */
        functionMap.put("ABS", null);
        functionMap.put("ACOS", null);
        functionMap.put("ASIN", null);
        functionMap.put("ATAN", null);
        functionMap.put("ATAN2", null);
        functionMap.put("CEILING", null);
        functionMap.put("COS", null);
        functionMap.put("COT", null);
        functionMap.put("EXP", null);
        functionMap.put("FLOOR", null);
        functionMap.put("LOG", "LN({0})");
        functionMap.put("LOG10", null);
        functionMap.put("MOD", null);
        functionMap.put("PI", null);
        functionMap.put("POWER", null);
        functionMap.put("ROUND", null);
        functionMap.put("SIGN", null);
        functionMap.put("SIN", null);
        functionMap.put("SQRT", null);
        functionMap.put("TAN", null);
        functionMap.put("TRUNCATE", "TRUNC({0},{1})");
        
        SUPPORTED_NUMERIC_FUNCTIONS = Collections.unmodifiableSet(new HashSet<String>(functionMap.keySet()));
        
        /* String Functions */
        functionMap.put("ASCII", "ASCII_VAL({0})");
        functionMap.put("CHAR", "ASCII_CHAR({0})");
        // TODO support difference between CHARACTER and OCTETS optional param
        functionMap.put("CHAR_LENGTH", "CHAR_LENGTH({0})");
        functionMap.put("CHARACTER_LENGTH", "CHAR_LENGTH({0})");
        functionMap.put("CONCAT", "{0}||{1}");
        functionMap.put("INSERT", "OVERLAY({0} PLACING {3} FROM {1} FOR {2})");
        functionMap.put("LCASE", "LOWER({0})");
        functionMap.put("LEFT", null);
        // TODO support difference between CHARACTER and OCTETS optional param
        functionMap.put("LENGTH", "CHAR_LENGTH(TRIM(TRAILING FROM {0}))");
        // TODO Support variant without start position (required for JavaEE compliance see 6.2 of JDBC 4.1 spec
        functionMap.put("LOCATE", "POSITION({0},{1},{2})");
        functionMap.put("LTRIM", "TRIM(LEADING FROM {0})");
        functionMap.put("OCTET_LENGTH", null);
        functionMap.put("POSITION", null);
        functionMap.put("REPEAT", "RPAD('''',{1},{0})");
        functionMap.put("REPLACE", null);
        functionMap.put("RIGHT", null);
        functionMap.put("RTRIM", "TRIM(TRAILING FROM {0})");
        functionMap.put("SPACE", "RPAD('''',{0})");
        functionMap.put("SUBSTRING", "SUBSTRING({0} FROM {1} FOR {2})");
        functionMap.put("UCASE", "UPPER({0})");
        
        Set<String> supportedStringFunctions = new HashSet<String>(functionMap.keySet());
        supportedStringFunctions.removeAll(SUPPORTED_NUMERIC_FUNCTIONS);
        SUPPORTED_STRING_FUNCTIONS = Collections.unmodifiableSet(new HashSet<String>(supportedStringFunctions));
        
        /* Time and Date Functions */
        functionMap.put("CURRENT_DATE", "CURRENT_DATE");
        functionMap.put("CURRENT_TIME", "CURRENT_TIME");
        functionMap.put("CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP");
        functionMap.put("CURDATE", "CURRENT_DATE");
        functionMap.put("CURTIME", "CURRENT_TIME");
        functionMap.put("DAYOFMONTH", "EXTRACT(DAY FROM {0})");
        functionMap.put("DAYOFWEEK", "EXTRACT(WEEKDAY FROM {0})+1");
        functionMap.put("DAYOFYEAR", "EXTRACT(YEARDAY FROM {0})+1");
        functionMap.put("EXTRACT", null);
        functionMap.put("HOUR", "EXTRACT(HOUR FROM {0})");
        functionMap.put("MINUTE", "EXTRACT(MINUTE FROM {0})");
        functionMap.put("MONTH", "EXTRACT(MONTH FROM {0})");
        functionMap.put("NOW", "CURRENT_TIMESTAMP");
        functionMap.put("SECOND", "EXTRACT(SECOND FROM {0})");
        functionMap.put("TIMESTAMPADD", null);
        functionMap.put("TIMESTAMPDIFF", null);
        functionMap.put("WEEK", "EXTRACT(WEEK FROM {0})");
        functionMap.put("YEAR", "EXTRACT(YEAR FROM {0})");
        
        Set<String> supportedTimeDateFunctions = new HashSet<String>(functionMap.keySet());
        supportedTimeDateFunctions.removeAll(SUPPORTED_NUMERIC_FUNCTIONS);
        supportedTimeDateFunctions.removeAll(SUPPORTED_STRING_FUNCTIONS);
        SUPPORTED_TIME_DATE_FUNCTIONS = Collections.unmodifiableSet(new HashSet<String>(supportedTimeDateFunctions));
        
        /* System Functions */
        functionMap.put("IFNULL", "COALESCE({0}, {1})");
        functionMap.put("USER", "USER");
        
        Set<String> supportedSystemFunctions = new HashSet<String>(functionMap.keySet());
        supportedSystemFunctions.removeAll(SUPPORTED_NUMERIC_FUNCTIONS);
        supportedSystemFunctions.removeAll(SUPPORTED_STRING_FUNCTIONS);
        supportedSystemFunctions.removeAll(SUPPORTED_TIME_DATE_FUNCTIONS);
        SUPPORTED_SYSTEM_FUNCTIONS = Collections.unmodifiableSet(new HashSet<String>(supportedSystemFunctions));
        
        /* Conversion Functions */
        // TODO This does not support the conversion with SQL_ prefix in appendix D
        // TODO Should work without specifying size on CHAR and VARCHAR
        functionMap.put("CONVERT", "CAST({0} AS {1})");
        
        // Unsupported functions defined in appendix D that might accidentally work due to UDFs
        // Numerics
        functionMap.put("DEGREES", null);
        functionMap.put("RADIANS", null);
        functionMap.put("RAND", null);
        
        // String
        functionMap.put("DIFFERENCE", null);
        functionMap.put("SOUNDEX", null);
        
        // Time and date
        functionMap.put("DAYNAME", null);
        functionMap.put("MONTHNAME", null);
        functionMap.put("QUARTER", null);
        
        // System
        functionMap.put("DATABASE", null);
        
        FUNCTION_MAP = Collections.unmodifiableMap(functionMap);
    }
    
    /**
     * Simple syntax check if function is specified in form "name(...)".
     * 
     * @param functionCall string representing function call.
     * 
     * @throws FBSQLParseException if simple syntax check failed.
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
     * @param functionCall escaped function call.
     * 
     * @return name of the function.
     * 
     * @throws FBSQLParseException if parse error occurs.
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
     * @param functionCall escaped function call.
     * 
     * @return list of parameters of the function.
     * 
     * @throws FBSQLParseException if parse error occurs.
     */
    public static List<String> parseArguments(String functionCall) throws FBSQLParseException {
        functionCall = functionCall.trim();
        checkSyntax(functionCall);

        final int parenthesisStart = functionCall.indexOf('(');
        if (parenthesisStart == -1) {
            return Collections.emptyList();
        }
        final String paramsString = functionCall.substring(parenthesisStart + 1, functionCall.length() - 1);

        final List<String> params = new ArrayList<String>();
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
     * @param functionCall escaped function call.
     * 
     * @return server-side representation of the function call or <code>null</code>
     * if no template found.
     *  
     * @throws FBSQLParseException if escaped function call has incorrect syntax.
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
        
        final String firebirdTemplate = FUNCTION_MAP.get(functionName);

        if (firebirdTemplate != null) 
            return MessageFormat.format(firebirdTemplate, (Object[]) params);
        
        if (mode == EscapeParserMode.USE_STANDARD_UDF)
            return convertUsingStandardUDF(functionName, params);
            
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
    
    private static String convertUsingStandardUDF(String name, String[] params) throws FBSQLParseException {
        try {
            name = name.toLowerCase();
            Method method = FBEscapedFunctionHelper.class.getMethod(name, new Class[] { String[].class});
            return (String)method.invoke(null, new Object[]{params});
        } catch(NoSuchMethodException ex) {
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

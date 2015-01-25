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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

/**
 * Helper class for escaped functions.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBEscapedFunctionHelper {
    
    /*
     * This map contains mapping between JDBC function names and Firebird ones.
     */
    private static final HashMap FUNCTION_MAP = new HashMap();
    static {
        
        /* Numeric Functions */
        FUNCTION_MAP.put("ABS", null);
        FUNCTION_MAP.put("ACOS", null);
        FUNCTION_MAP.put("ASIN", null);
        FUNCTION_MAP.put("ATAN", null);
        FUNCTION_MAP.put("ATAN2", null);
        FUNCTION_MAP.put("CEILING", null);
        FUNCTION_MAP.put("COS", null);
        FUNCTION_MAP.put("COT", null);
        FUNCTION_MAP.put("DEGREES", null);
        FUNCTION_MAP.put("EXP", null);
        FUNCTION_MAP.put("FLOOR", null);
        FUNCTION_MAP.put("LOG", null);
        FUNCTION_MAP.put("LOG10", null);
        FUNCTION_MAP.put("MOD", null);
        FUNCTION_MAP.put("PI", null);
        FUNCTION_MAP.put("POWER", null);
        FUNCTION_MAP.put("RADIANS", null);
        FUNCTION_MAP.put("RAND", null);
        FUNCTION_MAP.put("ROUND", null);
        FUNCTION_MAP.put("SIGN", null);
        FUNCTION_MAP.put("SIN", null);
        FUNCTION_MAP.put("SQRT", null);
        FUNCTION_MAP.put("TAN", null);
        FUNCTION_MAP.put("TRUNCATE", null);
        
        /* String Functions */
        FUNCTION_MAP.put("ASCII", null);
        FUNCTION_MAP.put("CHAR", null);
        FUNCTION_MAP.put("CONCAT", "{0}||{1}");
        FUNCTION_MAP.put("DIFFERENCE", null);
        FUNCTION_MAP.put("INSERT", null);
        FUNCTION_MAP.put("LCASE", null);
        FUNCTION_MAP.put("LEFT", "SUBSTRING({0} FROM 1 FOR {1}");
        FUNCTION_MAP.put("LENGTH", "CHARACTER_LENGTH({0})");
        FUNCTION_MAP.put("LOCATE", null);
        FUNCTION_MAP.put("LTRIM", null);
        FUNCTION_MAP.put("REPEAT", null);
        FUNCTION_MAP.put("REPLACE", null);
        FUNCTION_MAP.put("RIGHT", null);
        FUNCTION_MAP.put("RTRIM", null);
        FUNCTION_MAP.put("SOUNDEX", null);
        FUNCTION_MAP.put("SPACE", null);
        FUNCTION_MAP.put("SUBSTRING", "SUBSTRING({0} FROM {1} FOR {2})");
        FUNCTION_MAP.put("UCASE", "UPPER({0})");
        
        /* Time and Date Functions */
        FUNCTION_MAP.put("CURDATE", "CURRENT_DATE");
        FUNCTION_MAP.put("CURTIME", "CURRENT_TIME");
        FUNCTION_MAP.put("DAYNAME", null);
        FUNCTION_MAP.put("DAYOFMONTH", "EXTRACT(DAY FROM {0})");
        FUNCTION_MAP.put("DAYOFWEEK", null);
        FUNCTION_MAP.put("DAYOFYEAR", null );
        FUNCTION_MAP.put("HOUR", "EXTRACT(HOUR FROM {0})");
        FUNCTION_MAP.put("MINUTE", "EXTRACT(MINUTE FROM {0})");
        FUNCTION_MAP.put("MONTH", "EXTRACT(MONTH FROM {0})");
        FUNCTION_MAP.put("MONTHNAME", null);
        FUNCTION_MAP.put("NOW", "CURRENT_TIMESTAMP");
        FUNCTION_MAP.put("QUARTER", null);
        FUNCTION_MAP.put("SECOND", "EXTRACT(SECOND FROM {0})");
        FUNCTION_MAP.put("TIMESTAMPADD", null);
        FUNCTION_MAP.put("TIMESTAMPDIFF", null);
        FUNCTION_MAP.put("WEEK", null);
        FUNCTION_MAP.put("YEAR", "EXTRACT(YEAR FROM {0})");
        
        /* System Functions */
        FUNCTION_MAP.put("DATABASE", null);
        FUNCTION_MAP.put("IFNULL", "COALESCE({0}, {1})");
        
        /* Conversion Functions */
        FUNCTION_MAP.put("CONVERT", "CAST({0} AS {1})");
    }
    
    
    
    /**
     * Simple syntax check if function is specified in form "name(...)".
     * 
     * @param functionCall string representing function call.
     * 
     * @throws FBSQLParseException if simple syntax check failed.
     */
    private static void checkSyntax(String functionCall) throws FBSQLParseException {
        int parenthesisStart = functionCall.indexOf('(');
        
        if (parenthesisStart == -1)
            throw new FBSQLParseException("No opening parenthesis found, " +
                    "not a function call.");
        
        if (functionCall.charAt(functionCall.length() - 1) != ')')
            throw new FBSQLParseException("No closing parenthesis found, " +
                    "not a function call.");
        
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
        
        return functionCall.substring(0, parenthesisStart);
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
    public static List parseArguments(String functionCall) throws FBSQLParseException {
        functionCall = functionCall.trim();
        
        checkSyntax(functionCall);
        
        int parenthesisStart = functionCall.indexOf('(');
        
        String paramsString = functionCall.substring(
            parenthesisStart + 1, functionCall.length() - 1);
            
        List params = new ArrayList();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        boolean inDoubleQuotes = false;
        
        char[] chars = paramsString.toCharArray();
        
        for(int i = 0; i < chars.length ; i++) {
            switch(chars[i]) {
                case '\'' :
                    sb.append(chars[i]);
                    if (!inDoubleQuotes) 
                        inQuotes = !inQuotes;
                    break;
                    
                case '"' :
                    sb.append(chars[i]);
                    if (!inQuotes) 
                        inDoubleQuotes = !inDoubleQuotes;
                    break;
                    
                // we ignore spaces, tabs and new lines if
                // we are not in the string literal
                case ' ' :
                case '\t' :
                case '\n' :
                case '\r' :
                    if (inQuotes || inDoubleQuotes) 
                        sb.append(chars[i]);
                        
                    break;
                    
                // comma is considered parameter separator
                // if it is not within the string literal 
                case ',' :
                    if (inQuotes || inDoubleQuotes)
                        sb.append(chars[i]);
                    else {
                        params.add(sb.toString());
                        sb = new StringBuffer();
                    }
                    break;
                  
                // by default we add chars to the buffer  
                default : 
                    sb.append(chars[i]);
            }
        }
        
        // add last parameter if present
        if (sb.length() > 0)
            params.add(sb.toString());
        
        // after processing all parameters all string literals should be closed
        if (inQuotes || inDoubleQuotes)
            throw new FBSQLParseException(
                "String literal is not properly closed.");
        
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
    public static String convertTemplate(String functionCall, int mode) throws FBSQLParseException {
        String name = parseFunction(functionCall);
        String[] params = (String[])parseArguments(functionCall).toArray(new String[0]);
        
        String firebirdTemplate = (String)FUNCTION_MAP.get(name.toUpperCase());

        if (firebirdTemplate != null) 
            return MessageFormat.format(firebirdTemplate, (Object[])params);
        
        if (mode == FBEscapedParser.USE_STANDARD_UDF)
            return convertUsingStandardUDF(name, params);
            
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
            
            // workaround for the {fn char()} function, since we cannot use
            // "char" as name of the function - it is reserved word. 
            if ("char".equals(name))
                name = "_char";
            
            Method method = FBEscapedFunctionHelper.class.getMethod(
                name.toLowerCase(), new Class[] { String[].class});
            
            return (String)method.invoke(null, new Object[]{params});
            
        } catch(NoSuchMethodException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            throw new FBSQLParseException("Error when converting function " 
                + name + ". Error " + ex.getClass().getName() + 
                " : " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new FBSQLParseException("Error when converting function " 
                + name + ". Error " + ex.getClass().getName() + 
                " : " + ex.getMessage());
        } catch (InvocationTargetException ex) {
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
     * Produce a function call for the <code>log</code> UDF function. 
     * The syntax of the <code>log</code> function is 
     * <code>{fn log(number)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String log(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function log : " + params.length);
        
        return "ln(" + params[0] + ")";
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
    
    
    /*
     * String functions.
     */
    
    
    /**
     * Produce a function call for the <code>ascii</code> UDF function.
     * The syntax of the <code>ascii</code> function is
     * <code>{fn ascii(string)}</code>
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String ascii(String[] params) throws FBSQLParseException {
        if (params.length != 1 )
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function ascii : " + params.length);
        
        if (params[0] == null || params[0].length() < 1)
            throw new FBSQLParseException("Parameter must not be " +
                    "empty or null");
        
        return "ascii_val(" + params[0].charAt(0) + ")";
    }
    
    /**
     * Produce a function call for the <code>char</code> UDF function.
     * The syntax of the <code>char</code> function is
     * <code>{fn char(integer)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String _char(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function char : " + params.length);
        
        return "char(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>lcase</code> UDF function.
     * The syntax of the <code>lcase</code> function is
     * <code>{fn lcase(string)}</code>
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String lcase(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function lcase : " + params.length);
        
        return "lower(" + params[0] + ")";
    }

    /**
     * Produce a function call for the <code>length</code> UDF function.
     * The syntax of the <code>length</code> function is
     * <code>{fn length(string)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String length(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function length : " + params.length);
        
        return "strlen(" + params[0] + ")";
    }
    
    /**
     * Produce a function call for the <code>ltrim</code> UDF function.
     * The syntax of the <code>ltrim</code> function is
     * <code>{fn ltrim(string)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String ltrim(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function ltrim : " + params.length);
        
        return "ltrim(" + params[0] + ")";
    }
    
    /**
     * Produce a function call for the <code>rtrim</code> UDF function.
     * The syntax of the <code>rtrim</code> function is
     * <code>{fn rtrim(string)}</code>.
     *
     * @param params The parameters to be used in the call
     * @throws FBSQLParseException if there is an error with the parameters
     */
    public static String rtrim(String[] params) throws FBSQLParseException {
        if (params.length != 1)
            throw new FBSQLParseException("Incorrect number of " +
                    "parameters of function rtrim : " + params.length);
        
        return "rtrim(" + params[0] + ")";
    }
    
}

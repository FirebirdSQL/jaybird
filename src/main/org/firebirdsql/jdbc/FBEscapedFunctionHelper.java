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

import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

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
        FUNCTION_MAP.put("LCASE", "LOWER({0})");
        FUNCTION_MAP.put("LEFT", null);
        FUNCTION_MAP.put("LENGTH", null);
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
        FUNCTION_MAP.put("CURDATE", null);
        FUNCTION_MAP.put("CURTIME", null);
        FUNCTION_MAP.put("DAYNAME", null);
        FUNCTION_MAP.put("DAYOFMONTH", null);
        FUNCTION_MAP.put("DAYOFWEEK", null);
        FUNCTION_MAP.put("DAYOFYEAR", null);
        FUNCTION_MAP.put("HOUR", null);
        FUNCTION_MAP.put("MINUTE", null);
        FUNCTION_MAP.put("MONTH", null);
        FUNCTION_MAP.put("MONTHNAME", null);
        FUNCTION_MAP.put("NOW", null);
        FUNCTION_MAP.put("QUARTER", null);
        FUNCTION_MAP.put("SECOND", null);
        FUNCTION_MAP.put("TIMESTAMPADD", null);
        FUNCTION_MAP.put("TIMESTAMPDIFF", null);
        FUNCTION_MAP.put("WEEK", null);
        FUNCTION_MAP.put("YEAR", null);
        
        /* System Functions */
        FUNCTION_MAP.put("DATABASE", null);
        FUNCTION_MAP.put("IFNULL", "COALESCE({0}, {1})");
        
        /* Conversion Functions */
        FUNCTION_MAP.put("CONVERT", null);
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
    public static String convertTemplate(String functionCall) throws FBSQLParseException {
        String name = parseFunction(functionCall);
        List params = parseArguments(functionCall);
        
        String firebirdTemplate = (String)FUNCTION_MAP.get(name.toUpperCase());

        if (firebirdTemplate == null) 
            return null;
        
        return MessageFormat.format(firebirdTemplate, params.toArray());
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for escaped functions.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBEscapedFunctionHelper {
    
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
        
        int parenthesisStart = functionCall.indexOf('(');
        
        if (parenthesisStart == -1)
            throw new FBSQLParseException("No opening parenthesis found, " +
                "not a function call.");
                
        if (functionCall.charAt(functionCall.length() - 1) != ')')
            throw new FBSQLParseException("No closing parenthesis found, " +
                "not a function call.");
                
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
                    if (!inDoubleQuotes) 
                        inQuotes = !inQuotes;
                    break;
                    
                case '"' :
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
                        break;
                    }
                  
                // by default we add chars to the buffer  
                default : 
                    sb.append(chars[i]);
            }
        }
        
        // after processing all parameters all string literals should be closed
        if (inQuotes || inDoubleQuotes)
            throw new FBSQLParseException(
                "String literal is not properly closed.");
        
        return params;
    }
}

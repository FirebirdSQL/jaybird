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

/**
 * Parser for escaped procedure call.
 */
public class FBEscapedCallParser {
    
    private static final int UNDEFINED_STATE = 0;
    private static final int NORMAL_STATE = 1;
    private static final int LITERAL_STATE = 2;
    private static final int BRACE_STATE = 4;
    private static final int CURLY_BRACE_STATE = 8;
    private static final int SPACE_STATE = 16;
    private static final int COMMA_STATE = 32;
    
    private boolean parameterTerminated;
    
    private int state = NORMAL_STATE;
    private int lastState = NORMAL_STATE;

    private int paramPosition;
    private int paramCount;
    
    private boolean isFirstOutParam;
    private boolean isNameProcessed;
    private boolean isExecuteWordProcessed;
    private boolean isProcedureWordProcessed;
    private boolean isCallWordProcessed;
    
    private int openBraceCount;

    private FBProcedureCall procedureCall;
    private FBEscapedParser escapedParser = new FBEscapedParser();
    
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
    protected void setState(int state) {
        this.state = state;
    }

    /**
     * Returns if the system is in state <code>state</code>.
     * @param state we're testing
     * @return <code>true</code> if the system is in state <code>state</code>.
     */
    protected boolean isInState(int state) { return this.state == state; }
    
    /**
     * Test the character to be the state switching character and switches
     * the state if necessary.
     * @param testChar character to test
     */
    protected void switchState(char testChar) throws FBSQLParseException {
        parameterTerminated = false;
        
        switch (testChar) {
            case '\'' : 
                if (isInState(NORMAL_STATE))
                    setState(LITERAL_STATE);
                else
                if (isInState(LITERAL_STATE))
                    setState(NORMAL_STATE);

                break;
                
            case ' ' :
            case '\t' :
            case '\r' :
            case '\n' :
            case '\f' :
                if (!isInState(LITERAL_STATE))
                    setState(SPACE_STATE);

                break;
                
            case ',' : 
                if (!isInState(LITERAL_STATE) && !isInState(BRACE_STATE))
                    setState(COMMA_STATE);
                
                break;
                
            case '(' :
            case ')' :
                if (isInState(LITERAL_STATE))
                    break;
                
                setState(BRACE_STATE);
                
                break;
                
            case '{' :
            case '}' :
                if (!isInState(LITERAL_STATE))
                    setState(CURLY_BRACE_STATE);
                
                break;

            default :
                if (!isInState(LITERAL_STATE) && !isInState(BRACE_STATE))
                setState(NORMAL_STATE);
        }
    }
    
    
    /**
     * Clean the SQL statement. This method removes leading and trailing spaces
     * and removes leading and trailing curly braces if any.
     * 
     * @param sql SQL statement to clean up.
     * 
     * @return cleaned up statement.
     * 
     * @throws FBSQLParseException if cleanup resulted in empty statement.
     */
    private String cleanUpCall(String sql) throws FBSQLParseException {
        StringBuffer cleanupBuffer = new StringBuffer(sql);
        
        // remove spaces at the beginning
        while(cleanupBuffer.length() > 0 && 
                Character.isSpaceChar(cleanupBuffer.charAt(0)))
            cleanupBuffer.deleteCharAt(0);
        
        // remove spaces at the end
        while(cleanupBuffer.length() > 0 && 
                Character.isSpaceChar(cleanupBuffer.charAt(cleanupBuffer.length() - 1)))
            cleanupBuffer.deleteCharAt(cleanupBuffer.length() - 1);
        
        if (cleanupBuffer.length() == 0)
            throw new FBSQLParseException(
                    "Escaped call statement was empty.");
        
        if (cleanupBuffer.charAt(0) == '{')
        	cleanupBuffer.deleteCharAt(0);
        
        if (cleanupBuffer.charAt(cleanupBuffer.length() - 1) == '}')
            cleanupBuffer.deleteCharAt(cleanupBuffer.length() - 1);
        
        return cleanupBuffer.toString();
    }
    
    /**
     * Check if either "call" keyword or "EXECUTE PROCEDURE" keyword processed.
     * 
     * @return <code>true</code> if either one or another keyword were processed.
     */
    private boolean isCallKeywordProcessed() {
    	return isCallWordProcessed || 
            (isExecuteWordProcessed && isProcedureWordProcessed);
    }
    
    /**
     * Converts escaped parts in the passed SQL to native representation.
     * @param sql to parse
     * 
     * @return native form of the <code>sql</code>.
     */
    public FBProcedureCall parseCall(String sql) throws FBSQLException {
        
    	sql = cleanUpCall(sql);
        
        procedureCall = new FBProcedureCall();
        
        isExecuteWordProcessed = false;
        isProcedureWordProcessed = false;
        isCallWordProcessed = false;
        isNameProcessed = false;
        
        isFirstOutParam = false;
        
        paramCount = 0;
        paramPosition = 0;
        
        setState(NORMAL_STATE);
        
        char[] sqlbuff = sql.toCharArray();
        StringBuffer buffer = new StringBuffer();
        StringBuffer escape = new StringBuffer();
        
         for(int i = 0; i < sqlbuff.length; i++) {
             switchState(sqlbuff[i]);

             if (isInState(NORMAL_STATE)) {
                 
                 // if we have an equal sign, most likely {? = call ...}
                 // syntax is used (there's hardly any place for this sign
                 // in procedure parameters). but to be sure, we check if 
                 // no brace is open and if buffer contains only '?'
                 if (sqlbuff[i] == '=') {
                     
                     if (openBraceCount <= 0) {
                     
                         String token = buffer.toString().trim();
    
                         if ("?".equals(token) && !isFirstOutParam && !isNameProcessed) {
                             
                             FBProcedureParam param = 
                                 procedureCall.addParam(paramPosition, token);
                             
                             paramCount++;
                             param.setIndex(paramCount);
                             
                             isFirstOutParam = true;
                             paramPosition++;
                             
                             buffer = new StringBuffer();
                             continue;
                         }
                     }
                 }
                     
                 buffer.append(sqlbuff[i]);
                 
             } else
             if (isInState(SPACE_STATE)) {
                 
                 if (buffer.length() == 0) {
                     setState(NORMAL_STATE);
                     continue;
                 }
                 
                 if (openBraceCount > 0) {
                     buffer.append(sqlbuff[i]);
                     setState(NORMAL_STATE);
                     continue;
                 }
                 
                 String token = buffer.toString().trim();

                 // if procedure name was not yet processed, process
                 // the token; we look for the sequence EXECUTE PROCEDURE <name>
                 // otherwise go into normal state to enable next transitions.
                 if (!isNameProcessed) {
                     boolean tokenProcessed = processToken(token);
                     if (tokenProcessed)
                     	buffer = new StringBuffer();
                 } else {
                     buffer.append(sqlbuff[i]);
                     setState(NORMAL_STATE);
                 }
                 
             } else
             if (isInState(BRACE_STATE)) {
                 
                 // if we have an opening brace and we already processed
                 // EXECUTE PROCEDURE words, but still do not have procedure
                 // name set, we can be sure that buffer contains procedure 
                 // name.
                 
                 boolean isProcedureName = 
                     sqlbuff[i] == '(' &&
                     isCallKeywordProcessed() &&
                     !isNameProcessed;
                 
                 if (isProcedureName) {
                     String token = buffer.toString().trim();
                     
                     if ("".equals(token))
                         throw new FBSQLParseException(
                             "Procedure name is empty.");
                     
                     procedureCall.setName(token);
                     isNameProcessed = true;
                     
                     buffer = new StringBuffer();
                     
                 } else {
                     buffer.append(sqlbuff[i]);
                 
                     if (sqlbuff[i] == '(')
                         openBraceCount++;
                     else
                         openBraceCount--;
                 }
                 
                 setState(NORMAL_STATE);
                 
             } else
             if (isInState(CURLY_BRACE_STATE)) {
                
                buffer.append(sqlbuff[i]);
                setState(NORMAL_STATE);
                
             } else
             if (isInState(COMMA_STATE)) {
                 
                 if (openBraceCount > 0) {
                     buffer.append(sqlbuff[i]);
                     continue;
                 }
                 
                 String param = processParam(buffer.toString());
                 buffer = new StringBuffer();
                 
                 FBProcedureParam callParam = 
                     procedureCall.addParam(paramPosition, param);
                 
                 if (callParam.isParam()) {
                     paramCount++;
                     callParam.setIndex(paramCount);
                 }
                 
                 paramPosition++;
                 
                 setState(NORMAL_STATE);
                 
             } else
             if (isInState(LITERAL_STATE))
                 buffer.append(sqlbuff[i]);
         }

         // remove spaces at the end
         while(Character.isSpaceChar(buffer.charAt(0)))
             buffer.deleteCharAt(0);

         // if buffer ends with ')', remove it
         // it should match an opening brace right after the procedure
         // name, and we assume that all syntax check was already done.
         if (buffer.charAt(buffer.length() - 1) == ')')
             buffer.deleteCharAt(buffer.length() - 1);
         
         // if there's something in the buffer, treat it as last param
         if (buffer.length() > 0) {
             FBProcedureParam callParam = 
                 procedureCall.addParam(paramPosition, buffer.toString());
             
             if (callParam.isParam()) {
                 paramCount++;
                 callParam.setIndex(paramCount);
             }
         }
         
        return procedureCall;
    }
    
    /**
     * Process token. This method detects procedure call keywords and sets 
     * appropriate flags. Also it detects procedure name and sets appropriate 
     * filed in the procedure call object.
     * 
     * @param token token to process.
     * 
     * @return <code>true</code> if token was understood and processed.
     */
    protected boolean processToken(String token) {
        if ("EXECUTE".equalsIgnoreCase(token) && 
                !isExecuteWordProcessed && !isProcedureWordProcessed && !isNameProcessed) {
            isExecuteWordProcessed = true;
            return true;
        }
        
        if ("PROCEDURE".equalsIgnoreCase(token) &&
                isExecuteWordProcessed && !isProcedureWordProcessed && !isNameProcessed) {
            isProcedureWordProcessed = true;
            return true;
        }
        
        if ("call".equalsIgnoreCase(token) && !isCallWordProcessed && !isNameProcessed) {
        	isCallWordProcessed = true;
            return true;
        }
        
        if (isCallWordProcessed && !isNameProcessed) {
            procedureCall.setName(token);
            isNameProcessed = true;
            return true;
        }
        
        return false;
    }
    
    /**
     * Pre-process parameter. This method checks if there is escaped call inside
     * and converts it to the native one.
     * 
     * @param param parameter to process.
     * 
     * @return processed parameter.
     * 
     * @throws FBSQLParseException if parameter cannot be correctly parsed.
     */
    protected String processParam(String param) throws FBSQLException {
        return escapedParser.parse(param);
    }
}

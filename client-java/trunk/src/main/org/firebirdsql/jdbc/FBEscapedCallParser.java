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
    
    private int openBraceCount;

    private FBProcedureCall procedureCall;
    
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
     * Converts escaped parts in the passed SQL to native representation.
     * @param sql to parse
     * 
     * @return native form of the <code>sql</code>.
     */
    public FBProcedureCall parseCall(String sql) throws FBSQLParseException {
        
        procedureCall = new FBProcedureCall();
        
        isExecuteWordProcessed = false;
        isProcedureWordProcessed = false;
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
                     
                     if (openBraceCount > 0) {
                         buffer.append(sqlbuff[i]);
                         continue;
                     }
                     
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
                     processToken(token);
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
                     isExecuteWordProcessed &&
                     isProcedureWordProcessed &&
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
             if (isInState(COMMA_STATE)) {
                 
                 if (openBraceCount > 0) {
                     buffer.append(sqlbuff[i]);
                     continue;
                 }
                 
                 String param = buffer.toString();
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
    
    protected void processToken(String token) throws FBSQLParseException {
        if ("EXECUTE".equalsIgnoreCase(token) && 
                  !isExecuteWordProcessed && !isProcedureWordProcessed && !isNameProcessed) 
            isExecuteWordProcessed = true;
        else
        if ("PROCEDURE".equalsIgnoreCase(token) &&
                isExecuteWordProcessed && !isProcedureWordProcessed && !isNameProcessed)
            isProcedureWordProcessed = true;
        else
        if (isExecuteWordProcessed && isProcedureWordProcessed && !isNameProcessed) {
            procedureCall.setName(token);
            isNameProcessed = true;
        }
    }
}

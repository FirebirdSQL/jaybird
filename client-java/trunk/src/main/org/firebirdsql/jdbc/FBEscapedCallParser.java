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
    private static final int BRACE_STATE = 3;
    private static final int SPACE_STATE = 5;
    private static final int COMMA_STATE = 6;
    
    private boolean parameterTerminated;
    
    private int state = NORMAL_STATE;
    private int nestedEscaped = 0;

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
    protected boolean isInState(int state) { return getState() == state; }

    /**
     * Test the character to be the state switching character and switches
     * the state if necessary.
     * @param testChar character to test
     */
    protected void switchState(char testChar) {
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
                if (!isInState(LITERAL_STATE))
                    setState(COMMA_STATE);
                
                break;
                
            case '(' :
            case ')' : 
                if (!isInState(LITERAL_STATE))
                    setState(BRACE_STATE);
                
                break;
                
            default :
                if (!isInState(LITERAL_STATE))
                setState(NORMAL_STATE);
        }
    }
    
    
    /**
     * Converts escaped parts in the passed SQL to native representation.
     * @param sql to parse
     * @return native form of the <code>sql</code>.
     */
    public FBProcedureCall parseCall(String sql) throws FBSQLParseException {
        
        FBProcedureCall procedureCall = new FBProcedureCall();
        int paramPosition = 0;
        int paramCount = 0;

        char[] sqlbuff = sql.toCharArray();
        StringBuffer buffer = new StringBuffer();
        StringBuffer escape = new StringBuffer();
        
        boolean isNameProcessed = false;
        boolean isExecuteWordProcessed = false;
        boolean isProcedureWordProcessed = false;

        for(int i = 0; i < sqlbuff.length; i++) {

            switchState(sqlbuff[i]);

            if (isInState(NORMAL_STATE))
                buffer.append(sqlbuff[i]);
            else
            if (isInState(SPACE_STATE) || isInState(BRACE_STATE)) {
                if (!isNameProcessed) {
                    
                    String token = buffer.toString();
                    
                    if ("EXECUTE".equalsIgnoreCase(token)) {
                        if (!isExecuteWordProcessed && !isProcedureWordProcessed)
                            isExecuteWordProcessed = true;
                        else
                            throw new FBSQLParseException(
                                    "Syntax error. EXECUTE token is not " +
                                    "on the first place.");
                    } else
                    if ("PROCEDURE".equalsIgnoreCase(token)) {
                        if (isExecuteWordProcessed && !isProcedureWordProcessed)
                            isProcedureWordProcessed = true;
                        else
                            throw new FBSQLParseException(
                                    "Syntax error. PROCEDURE token is not " +
                                    "following EXECUTE token.");
                    } else
                    if (isExecuteWordProcessed && isProcedureWordProcessed) {
                        procedureCall.setName(token);
                        isNameProcessed = true;
                    } else
                        throw new FBSQLParseException(
                                "Syntax error.");
                    
                    buffer = new StringBuffer();
                } else {
                    // buffer.append(sqlbuff[i]);
                    setState(NORMAL_STATE);
                }
            } else
            if (isInState(COMMA_STATE)) {
                String param = buffer.toString();
                buffer = new StringBuffer();
                FBProcedureParam callParam = 
                    procedureCall.addParam(paramPosition, param);
                
                if (callParam.isParam()) {
                    paramCount++;
                    callParam.setIndex(paramCount);
                }
                
                paramPosition++;
            } else
            if (isInState(LITERAL_STATE))
                buffer.append(sqlbuff[i]);
        }
        
        while(Character.isSpaceChar(buffer.charAt(0)))
            buffer.deleteCharAt(0);
        
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
    
}

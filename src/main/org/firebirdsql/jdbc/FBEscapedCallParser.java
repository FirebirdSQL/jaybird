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
 * Parser for escaped procedure call.
 */
public class FBEscapedCallParser {
    
    /**
     * Represents procedure call parameter.
     */
    public static class FBProcedureCallParam {
        private String value = "?";
        private int position;
        
        public FBProcedureCallParam() {
            
        }
        
        public FBProcedureCallParam(int position, String value) {
            this.position = position;
            this.value = value;
        }
        
        public void setPosition(int position) {
            this.position = position;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public int getPosition() {
            return position;
        }
        
        public String getValue() {
            return value;
        }
        
        public boolean isConstant() {
            return !"?".equals(value);
        }
        
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof FBProcedureCallParam)) return false;
            
            FBProcedureCallParam that = (FBProcedureCallParam)obj;
            
            return this.position == that.position &&
                   this.value != null ? this.value.equals(that.value) : 
                                        that.value == null;
        }
    }
    
    /**
     * Represents procedure call.
     */
    public static class FBProcedureCall {
        private String name;
        private List inputParams = new ArrayList();
        private List outputParams = new ArrayList();

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public List getInputParams() {
            return inputParams;
        }
        
        public List getOutputParams() {
            return outputParams;
        }
        
        public void addInputParam(FBProcedureCallParam param) {
            inputParams.add(param);
        }
        
        public void addOutputParam(FBProcedureCallParam param) {
            outputParams.add(param);
        }
        
        public void addParam(int position, String param) {
            param = param.trim();
            
            boolean isInputParam = true;
            if (param.length() > 3) {
                String possibleOutIndicator = param.substring(0, 3);
                if ("OUT".equalsIgnoreCase(possibleOutIndicator) &&
                    Character.isSpaceChar(param.charAt(3))) 
                {
                    isInputParam = false;
                    param = param.substring(3).trim();
                }
            }
            
            if (param.length() > 2) {
                String possibleInIndicator = param.substring(0, 2);
                if ("IN".equalsIgnoreCase(possibleInIndicator) &&
                    Character.isSpaceChar(param.charAt(2))) 
                {
                    param = param.substring(2).trim();
                }
                
            }
            
            FBProcedureCallParam callParam = new FBProcedureCallParam();
            callParam.setPosition(position);
            callParam.setValue(param);
            
            if (isInputParam)
                addInputParam(callParam);
            else
                addOutputParam(callParam);
            
        }
        
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof FBProcedureCall)) return false;
            
            FBProcedureCall that = (FBProcedureCall)obj;
            
            boolean result = this.name != null ? 
                this.name.equals(that.name) : that.name == null;
            
            result &= this.inputParams.equals(that.inputParams);
            result &= this.outputParams.equals(that.outputParams);
            
            return result;
        }
    }
    
    private static final int UNDEFINED_STATE = 0;
    private static final int NORMAL_STATE = 1;
    private static final int LITERAL_STATE = 2;
    private static final int SPACE_STATE = 5;
    private static final int COMMA_STATE = 6;
    
    private boolean parameterTerminated;
    private boolean nameProcessed;
    
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
        int paramPosition = 1;

        char[] sqlbuff = sql.toCharArray();
        StringBuffer buffer = new StringBuffer();
        StringBuffer escape = new StringBuffer();

        for(int i = 0; i < sqlbuff.length; i++) {

            switchState(sqlbuff[i]);

            if (isInState(NORMAL_STATE))
                buffer.append(sqlbuff[i]);
            else
            if (isInState(SPACE_STATE)) {
                if (!nameProcessed) {
                    procedureCall.setName(buffer.toString());
                    nameProcessed = true;
                    buffer = new StringBuffer();
                } else {
                    buffer.append(sqlbuff[i]);
                    setState(NORMAL_STATE);
                }
            } else
            if (isInState(COMMA_STATE)) {
                String param = buffer.toString();
                buffer = new StringBuffer();
                procedureCall.addParam(paramPosition, param);
                
                paramPosition++;
            } else
            if (isInState(LITERAL_STATE))
                buffer.append(sqlbuff[i]);
        }
        
        while(Character.isSpaceChar(buffer.charAt(0)))
            buffer.deleteCharAt(0);
        
        if (buffer.length() > 0) {
            procedureCall.addParam(paramPosition, buffer.toString());
        }
        
        return procedureCall;
    }
    
}

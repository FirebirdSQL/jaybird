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

import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * Represents procedure call.
 */
public class FBProcedureCall {

    private String name;
    private Vector inputParams = new Vector();
    private Vector outputParams = new Vector();

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FBProcedureParam getParam(int index) {
        int counter = 0;
        
        Iterator iter = inputParams.iterator();
        while(iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam)iter.next();
            
            if (param != null && param.getIndex() == index) 
                return param;
        }
        
        return NullParam.NULL_PARAM;
    }
    
    public List getInputParams() {
        return inputParams;
    }
    
    public List getOutputParams() {
        return outputParams;
    }
    
    public void addInputParam(FBProcedureParam param) {
        inputParams.add(param);
    }
    
    public void addOutputParam(FBProcedureParam param) {
        outputParams.add(param);
    }
    
    public FBProcedureParam addParam(int position, String param) {
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
        
        FBProcedureParam callParam = 
            new FBProcedureParam(position, param);
        
        Vector params;
        
        if (isInputParam)
            params = inputParams;
        else
            params = outputParams;

        params.setSize(position + 1);
        params.set(position, callParam);
        
        return callParam;
    }
    
    public void registerOutParam(int index, int type) throws SQLException {
        FBProcedureParam param = getParam(index);
        
//        if (param == null) {
//            param = (FBProcedureParam)outputParams.get(position);
//        } else {
            outputParams.ensureCapacity(param.getPosition() + 1);
            outputParams.set(param.getPosition(), param);
            inputParams.remove(param.getPosition());
//        }
        
        if (param == null)
            throw new FBSQLException(
                    "Cannot find parameter with the specified position.",
                    FBSQLException.SQL_STATE_INVALID_COLUMN);
        
        param.setType(type);
    }
    
    public String getSQL() {
        StringBuffer sb = new StringBuffer();
        sb.append(AbstractCallableStatement.NATIVE_CALL_COMMAND);
        sb.append(" ");
        sb.append(name);
        sb.append("(");
        
        boolean firstParam = true;
        Iterator iter = inputParams.iterator();
        while(iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam)iter.next();
            if (param != null) {
                if (!firstParam)
                    sb.append(", ");
                else
                    firstParam = false;
                
                if (param.isParam())
                    sb.append('?');
                else
                    sb.append(param.getValue());
            }
        }
        sb.append(")");
        
        return sb.toString();
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
    
    private static final class NullParam extends FBProcedureParam {
        
        private static final NullParam NULL_PARAM = new NullParam();
        
        public void setValue(Object value) throws SQLException {
            throw new FBSQLException(
                    "You cannot set value of an non-existing parameter.",
                    FBSQLException.SQL_STATE_INVALID_ARG_VALUE);
        }

}
}
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

/**
 * Represents procedure call.
 */
public class FBProcedureCall {
    
    private static final boolean OLD_CALLABLE_STATEMENT_COMPATIBILITY = true;

    private String name;
    private Vector inputParams = new Vector();
    private Vector outputParams = new Vector();

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get input parameter by the specified index.
     * 
     * @param index index for which parameter has to be returned.
     * 
     * @return instance of {@link FBProcedureParam}.
     */
    public FBProcedureParam getInputParam(int index) {
        return getParam(inputParams, index);
    }
    
    public FBProcedureParam getOutputParam(int index) {
        return getParam(outputParams, index);    
    }
    
    /**
     * Get parameter with the specified index from the specified collection.
     * 
     * @param params collection containing parameters.
     * @param index index for which parameter has to be found.
     * 
     * @return instance of {@link FBProcedureParam}.
     */
    private FBProcedureParam getParam(Collection params, int index) {
        int counter = 0;
        
        Iterator iter = params.iterator();
        while(iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam)iter.next();
            
            if (param != null && param.getIndex() == index) 
                return param;
        }
        
        return NullParam.NULL_PARAM;
    }
    
    /**
     * Map output parameter index to a column number of corresponding result 
     * set.
     * 
     * @param index index to map.
     * 
     * @return mapped column number or <code>-1</code> if no output parameter
     * with the specified index found.
     */
    public int mapOutParamIndexToPosition(int index) throws FBSQLException {
    	int position = -1;
        
        Iterator iter = outputParams.iterator();
        while(iter.hasNext()) {
        	FBProcedureParam param = (FBProcedureParam)iter.next();
            
            if (param != null && param.isParam()) {
               position++;
               
               if (param.getIndex() == index)
               	    return position + 1;
            }
        }
        
        // hack: if we did not find the right parameter we return
        // an index that was asked if we run in compatibilty mode
        // 
        // we should switch it off as soon as people convert applications
        if (position == -1 && OLD_CALLABLE_STATEMENT_COMPATIBILITY)
            return index;
        else 
        if (position == -1)
            throw new FBSQLException("Specified parameter does not exist.", 
                    FBSQLException.SQL_STATE_INVALID_COLUMN);
        else
        	return position;
    }
    
    public List getInputParams() {
    	return inputParams;
    }
    
    public void addInputParam(FBProcedureParam param) {
    	inputParams.add(param);
    }
    
    public void addOutputParam(FBProcedureParam param) {
        if (outputParams.size() < param.getPosition() + 1)
        	outputParams.setSize(param.getPosition() + 1);
        
    	outputParams.set(param.getPosition(), param);
    }
    
    /**
     * Add call parameter. This method adds new parameter to the procedure call
     * and tries to automatically place the parameter into the right collection
     * if it contains a hint whether it is input or output parameter.
     * 
     * @param position position of the parameter in the procedure call.
     * @param param contents of the parameter.
     * 
     * @return instance of the {@link FBProcedureParam} that was created to
     * represent this parameter.
     */
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

        if (params.size() < position + 1)
        	params.setSize(position + 1);
        
        params.set(position, callParam);
        
        return callParam;
    }
    
    /**
     * Register output parameter. This method marks parameter with the specified
     * index as output. Parameters marked as output cannot be used as input 
     * parameters.
     * 
     * @param index index of the parameter to mark as output.
     * @param type SQL type of the parameter.
     * 
     * @throws SQLException if something went wrong.
     */
    public void registerOutParam(int index, int type) throws SQLException {
        FBProcedureParam param = getInputParam(index);
        
        if (param == null || param == NullParam.NULL_PARAM)
            param = getOutputParam(index);
        else {
            if (outputParams.size() < param.getPosition() + 1)
            	outputParams.setSize(param.getPosition() + 1);
            
            outputParams.set(param.getPosition(), param);
            inputParams.remove(param.getPosition());
        }
        
        if (param == null || param == NullParam.NULL_PARAM)
            throw new FBSQLException(
                    "Cannot find parameter with the specified position.",
                    FBSQLException.SQL_STATE_INVALID_COLUMN);
        
        param.setType(type);
    }
    
    /**
     * Get native SQL for the specified procedure call.
     * 
     * @return native SQL that can be executed by the database server.
     */
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
                
                sb.append(param.getParamValue());
            }
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    /**
     * Check if <code>obj</code> is equal to this instance.
     * 
     * @return <code>true</code> iff <code>obj</code> is instance of this class
     * representing the same procedure with the same parameters.
     */
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
    
    /**
     * This class defines procedure parameter that does not have any value
     * and value of which cannot be set. It is created in order to avoid NPE
     * when {@link FBProcedureCall#getInputParam(int)} does not find correct
     * parameter.
     */
    private static final class NullParam extends FBProcedureParam {
        
        private static final NullParam NULL_PARAM = new NullParam();
        
        public void setValue(Object value) throws SQLException {
            throw new FBSQLException(
                    "You cannot set value of an non-existing parameter.",
                    FBSQLException.SQL_STATE_INVALID_ARG_VALUE);
        }

    }
}
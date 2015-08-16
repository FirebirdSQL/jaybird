/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Represents procedure call.
 */
public class FBProcedureCall implements Cloneable {

    public Object clone() {
        try {
            FBProcedureCall newProcedureCall = (FBProcedureCall) super.clone();

            //Copy each input and output parameter.
            newProcedureCall.inputParams = cloneParameters(inputParams);
            newProcedureCall.outputParams = cloneParameters(outputParams);

            return newProcedureCall;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    private static Vector<FBProcedureParam> cloneParameters(final Vector<FBProcedureParam> parameters) {
        final Vector<FBProcedureParam> clonedParameters = new Vector<>(parameters.size());
        for (FBProcedureParam param : parameters) {
            clonedParameters.add(param != null ? (FBProcedureParam) param.clone() : null);
        }
        return clonedParameters;
    }

    /**
     * <code>true</code> if the old callable statement compatibility mode should
     * be used, otherwise - <code>false</code>. Current value - <code>true</code>.
     */
    public static final boolean OLD_CALLABLE_STATEMENT_COMPATIBILITY = true;

    private String name;
    // TODO Replace Vector with a List
    private Vector<FBProcedureParam> inputParams = new Vector<>();
    private Vector<FBProcedureParam> outputParams = new Vector<>();

    /**
     * Get the name of the procedure to be called.
     *
     * @return The procedure name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the procedure to be called.
     *
     * @param name The name of the procedure
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get input parameter by the specified index.
     *
     * @param index index for which parameter has to be returned, first index is 1
     *
     * @return instance of {@link FBProcedureParam}.
     */
    public FBProcedureParam getInputParam(int index) {
        FBProcedureParam result = getParam(inputParams, index);

        if (result == null || result == NullParam.NULL_PARAM) {
            result = getParam(outputParams, index);

            // ensure that vector has right size
            // note, index starts with 1
            if (inputParams.size() < index) {
                inputParams.setSize(index);
            }

            inputParams.set(index - 1, result);
        }

        return result;
    }

    /**
     * Get the output parameter at the specified index.
     *
     * @param index The index of the parameter, first index is 1
     * @return The parameter at the given index
     */
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
    private FBProcedureParam getParam(Collection<FBProcedureParam> params, int index) {
        for (FBProcedureParam param : params) {
            if (param != null && param.getIndex() == index) {
                return param;
            }
        }

        return NullParam.NULL_PARAM;
    }

    /**
     * Map output parameter index to a column number of corresponding result
     * set.
     *
     * @param index index to map.
     *
     * @return mapped column number or <code>index</code> if no output parameter
     * with the specified index found (assuming that {@link #OLD_CALLABLE_STATEMENT_COMPATIBILITY}
     * constant is set to <code>true</code>, otherwise throws exception).
     *
     * @throws SQLException if compatibility mode is switched off and no
     * parameter was found (see {@link #OLD_CALLABLE_STATEMENT_COMPATIBILITY}
     * constant).
     */
    public int mapOutParamIndexToPosition(int index) throws SQLException {
        return mapOutParamIndexToPosition(index, OLD_CALLABLE_STATEMENT_COMPATIBILITY);
    }

    /**
     * Map output parameter index to a column number of corresponding result
     * set.
     *
     * @param index index to map.
     * @param compatibilityMode <code>true</code> if we should run in old compatibility mode.
     *
     * @return mapped column number or <code>index</code> if no output parameter
     * with the specified index found and <code>compatibilityMode</code> is set.
     *
     * @throws SQLException if compatibility mode is switched off and no
     * parameter was found.
     */
    public int mapOutParamIndexToPosition(int index, boolean compatibilityMode) throws SQLException {
        int position = -1;

        for (FBProcedureParam param : outputParams) {
            if (param != null && param.isParam()) {
                position++;

                if (param.getIndex() == index) {
                    return position + 1;
                }
            }
        }

        // hack: if we did not find the right parameter we return
        // an index that was asked if we run in compatibility mode
        // 
        // we should switch it off as soon as people convert applications
        if (compatibilityMode)
            return index;
        else
            throw new FBSQLException("Specified parameter does not exist.",
                    FBSQLException.SQL_STATE_INVALID_COLUMN);
    }


    /**
     * Get the list of input parameters for this procecedure call.
     *
     * @return A list of all input parameters
     */
    public List<FBProcedureParam> getInputParams() {
        return inputParams;
    }

    /**
     * Get a list of output parameters for this procedure call.
     *
     * @return A list of all output parameters
     */
    public List<FBProcedureParam> getOutputParams() {
        return outputParams;
    }

    /**
     * Add an input parameter to this procedure call.
     *
     * @param param The parameter to be added
     */
    public void addInputParam(FBProcedureParam param) {
        if (inputParams.size() < param.getPosition() + 1) {
            inputParams.setSize(param.getPosition() + 1);
        }

        inputParams.set(param.getPosition(), param);
    }

    /**
     * Add an output parameter to this procedure call.
     *
     * @param param
     *         The parameter to be added
     */
    public void addOutputParam(FBProcedureParam param) {
        if (outputParams.size() < param.getPosition() + 1) {
            outputParams.setSize(param.getPosition() + 1);
        }

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
            if ("OUT".equalsIgnoreCase(possibleOutIndicator) && Character.isSpaceChar(param.charAt(3))) {
                isInputParam = false;
                param = param.substring(3).trim();
            }
        }

        if (param.length() > 2) {
            String possibleInIndicator = param.substring(0, 2);
            if ("IN".equalsIgnoreCase(possibleInIndicator) && Character.isSpaceChar(param.charAt(2))) {
                param = param.substring(2).trim();
            }
        }

        FBProcedureParam callParam = new FBProcedureParam(position, param);

        final Vector<FBProcedureParam> params = isInputParam ? inputParams : outputParams;

        if (params.size() < position + 1) {
            params.setSize(position + 1);
        }

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

        if (param == null || param == NullParam.NULL_PARAM) {
            param = getOutputParam(index);
        } else {
            if (outputParams.size() < param.getPosition() + 1) {
                outputParams.setSize(param.getPosition() + 1);
            }

            outputParams.set(param.getPosition(), param);

            if (!param.isValueSet()) {
                inputParams.set(param.getPosition(), null);
            }
        }

        if (param == null || param == NullParam.NULL_PARAM) {
            throw new SQLException("Cannot find parameter with the specified position.",
                    FBSQLException.SQL_STATE_INVALID_COLUMN);
        }

        param.setType(type);
    }

    /**
     * Get native SQL for the specified procedure call.
     *
     * @return native SQL that can be executed by the database server.
     */
    public String getSQL(boolean select) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append(select
                ? AbstractCallableStatement.NATIVE_SELECT_COMMAND
                : AbstractCallableStatement.NATIVE_CALL_COMMAND);

        sb.append(" ");
        sb.append(name);

        StringBuilder paramsBuffer = new StringBuilder();

        boolean firstParam = true;
        for (FBProcedureParam param : inputParams) {
            if (param == null)
                continue;

            // if parameter does not have set value, and is not registered
            // as output parameter, throw an exception, otherwise, continue
            // to the next one.
            if (!param.isValueSet()) {
                if (param.isParam() &&
                        outputParams.size() > 0 &&
                        outputParams.get(param.getPosition()) == null) {
                    throw new FBSQLException("Value of parameter " + param.getIndex() + " not set and " +
                            "it was not registered as output parameter.", FBSQLException.SQL_STATE_WRONG_PARAM_NUM);
                }
            }

            if (!firstParam) {
                paramsBuffer.append(", ");
            } else {
                firstParam = false;
            }

            paramsBuffer.append(param.getParamValue());
        }

        if (paramsBuffer.length() > 0)
            sb.append('(').append(paramsBuffer).append(')');

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

        FBProcedureCall that = (FBProcedureCall) obj;

        boolean result = this.name != null ?
                this.name.equals(that.name) : that.name == null;

        result &= this.inputParams.equals(that.inputParams);
        result &= this.outputParams.equals(that.outputParams);

        return result;
    }

    public int hashCode() {
        int hashCode = 547;
        hashCode = 37 * hashCode + (name != null ? name.hashCode() : 0);
        hashCode = 37 * hashCode + inputParams.hashCode();
        hashCode = 37 * hashCode + outputParams.hashCode();
        return hashCode;
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
            throw new FBSQLException("You cannot set value of an non-existing parameter.",
                    FBSQLException.SQL_STATE_INVALID_ARG_VALUE);
        }

    }
}
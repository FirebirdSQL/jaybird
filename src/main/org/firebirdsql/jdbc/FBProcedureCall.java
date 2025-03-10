// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005-2006 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.util.CollectionUtils;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.firebirdsql.jdbc.SQLStateConstants.*;

/**
 * Represents procedure call.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 */
@InternalApi
@NullMarked
public class FBProcedureCall {

    private static final String NATIVE_CALL_COMMAND = "EXECUTE PROCEDURE ";
    private static final String NATIVE_SELECT_COMMAND = "SELECT * FROM ";

    private @Nullable String name;
    private List<@Nullable FBProcedureParam> inputParams = new ArrayList<>();
    private List<@Nullable FBProcedureParam> outputParams = new ArrayList<>();

    public FBProcedureCall() {
    }

    private FBProcedureCall(FBProcedureCall source) {
        name = source.name;
        inputParams = cloneParameters(source.inputParams);
        outputParams = cloneParameters(source.outputParams);
    }

    public static FBProcedureCall copyOf(FBProcedureCall source) {
        return new FBProcedureCall(source);
    }

    private static List<@Nullable FBProcedureParam> cloneParameters(final List<@Nullable FBProcedureParam> parameters) {
        final List<@Nullable FBProcedureParam> clonedParameters = new ArrayList<>(parameters.size());
        for (FBProcedureParam param : parameters) {
            clonedParameters.add(param != null ? (FBProcedureParam) param.clone() : null);
        }
        return clonedParameters;
    }

    /**
     * Get the name of the procedure to be called.
     *
     * @return The procedure name
     */
    public @Nullable String getName() {
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

        if (result == NullParam.NULL_PARAM) {
            result = getParam(outputParams, index);

            // ensure that vector has right size
            // note, index starts with 1
            CollectionUtils.growToSize(inputParams, index);
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
    private static FBProcedureParam getParam(Collection<@Nullable FBProcedureParam> params, int index) {
        for (FBProcedureParam param : params) {
            if (param != null && param.getIndex() == index) {
                return param;
            }
        }

        return NullParam.NULL_PARAM;
    }

    /**
     * Map output parameter index to a column number of the corresponding result set.
     *
     * @param index
     *         index to map
     * @return mapped column number or {@code index} if no output parameter with the specified index is found
     * @throws SQLException
     *         in current implementation: never, throws clause retained for compatibility and possibly future uses
     */
    @SuppressWarnings("RedundantThrows")
    public int mapOutParamIndexToPosition(int index) throws SQLException {
        int position = 0;

        for (FBProcedureParam param : outputParams) {
            if (param != null && param.isParam()) {
                position++;

                if (param.getIndex() == index) {
                    return position;
                }
            }
        }
        // For historic compatibility reasons we return the original requested index if there is no mapping
        return index;
    }

    /**
     * Get the list of input parameters for this procedure call.
     *
     * @return A list of all input parameters
     */
    public List<@Nullable FBProcedureParam> getInputParams() {
        return inputParams;
    }

    /**
     * Get a list of output parameters for this procedure call.
     *
     * @return A list of all output parameters
     */
    public List<@Nullable FBProcedureParam> getOutputParams() {
        return outputParams;
    }

    /**
     * Add an input parameter to this procedure call.
     *
     * @param param The parameter to be added
     */
    public void addInputParam(FBProcedureParam param) {
        addParam(inputParams, param);
    }

    /**
     * Add an output parameter to this procedure call.
     *
     * @param param
     *         The parameter to be added
     */
    public void addOutputParam(FBProcedureParam param) {
        addParam(outputParams, param);
    }

    private static void addParam(List<@Nullable FBProcedureParam> params, FBProcedureParam param) {
        CollectionUtils.growToSize(params, param.getPosition() + 1);
        params.set(param.getPosition(), param);
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

        if (param.length() > 4) {
            String possibleOutIndicator = param.substring(0, 3);
            if ("OUT".equalsIgnoreCase(possibleOutIndicator) && Character.isSpaceChar(param.charAt(3))) {
                isInputParam = false;
                param = param.substring(4).trim();
            }
        }

        if (isInputParam && param.length() > 4) {
            String possibleInIndicator = param.substring(0, 2);
            if ("IN".equalsIgnoreCase(possibleInIndicator) && Character.isSpaceChar(param.charAt(2))) {
                param = param.substring(3).trim();
            }
        }

        FBProcedureParam callParam = new FBProcedureParam(position, param);
        if (isInputParam) {
            addInputParam(callParam);
        } else {
            addOutputParam(callParam);
        }

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

        if (param == NullParam.NULL_PARAM) {
            param = getOutputParam(index);
        } else {
            addOutputParam(param);

            if (!param.isValueSet()) {
                inputParams.set(param.getPosition(), null);
            }
        }

        if (param == NullParam.NULL_PARAM) {
            throw new SQLException("Cannot find parameter with the specified position",
                    SQL_STATE_INVALID_DESC_FIELD_ID);
        }

        param.setType(type);
    }

    /**
     * Get native SQL for the specified procedure call.
     *
     * @return native SQL that can be executed by the database server.
     */
    public String getSQL(boolean select) throws SQLException {
        StringBuilder sb = new StringBuilder(select ? NATIVE_SELECT_COMMAND : NATIVE_CALL_COMMAND);
        sb.append(name);

        boolean firstParam = true;
        sb.append('(');
        for (FBProcedureParam param : inputParams) {
            if (param == null) {
                continue;
            }

            if (!firstParam) {
                sb.append(',');
            } else {
                firstParam = false;
            }

            sb.append(param.getParamValue());
        }

        if (firstParam) {
            sb.setLength(sb.length() - 1);
        } else {
            sb.append(')');
        }

        return sb.toString();
    }

    /**
     * Checks if all parameters have been set.
     *
     * @throws SQLException When some parameters don't have values, and are not registered as an out parameter.
     */
    public void checkParameters() throws SQLException {
        for (FBProcedureParam param : inputParams) {
            // if parameter does not have set value, and is not registered as output parameter, throw an exception,
            // otherwise, continue to the next one.
            if (param != null && !param.isValueSet() && param.isParam() && !outputParams.isEmpty()
                    && outputParams.get(param.getPosition()) == null) {
                throw new SQLException("Value of parameter %d not set and it was not registered as output parameter"
                        .formatted(param.getIndex()), SQL_STATE_WRONG_PARAM_NUM);
            }
        }
    }

    /**
     * Check if <code>obj</code> is equal to this instance.
     *
     * @return <code>true</code> iff <code>obj</code> is instance of this class
     * representing the same procedure with the same parameters.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof FBProcedureCall other
                && Objects.equals(name, other.name)
                && inputParams.equals(other.inputParams)
                && outputParams.equals(other.outputParams);
    }

    public int hashCode() {
        return Objects.hash(name, inputParams, outputParams);
    }

    /**
     * This class defines a procedure parameter that does not have any value and value of which cannot be set. It was
     * created to avoid NPE when {@link FBProcedureCall#getInputParam(int)} does not find the correct parameter.
     */
    private static final class NullParam extends FBProcedureParam {

        private static final NullParam NULL_PARAM = new NullParam();

        private NullParam() {
            super(-1, "NULL");
        }

        @Override
        public void setValue(@Nullable Object value) throws SQLException {
            throw new SQLException("You cannot set value of a non-existing parameter", SQL_STATE_ATT_CANNOT_SET_NOW);
        }

        @Override
        public void setIndex(int index) {
            throw new UnsupportedOperationException("You cannot set index of a non-existing parameter");
        }

        @Override
        public void setType(int type) {
            throw new UnsupportedOperationException("You cannot set type of a non-existing parameter");
        }
    }
}
// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005-2006 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_PARAM_TYPE;

/**
 * Represents procedure call parameter.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 */
@InternalApi
@NullMarked
public class FBProcedureParam implements Cloneable {

    private final boolean isParam;
    private @Nullable Object value;
    private final String paramValue;
    private final int position;
    private int type;
    private int index = -1;
    private boolean valueSet;

    /**
     * Create a new {@code FBProcedureParam} instance.
     *
     * @param position
     *         The position at which this parameter is situated in the call
     * @param paramValue
     *         The value for this parameter
     */
    public FBProcedureParam(int position, String paramValue) {
        this.position = position;
        boolean isLiteral = paramValue.startsWith("'") && paramValue.endsWith("'");
        this.isParam = !isLiteral && paramValue.indexOf('?') >= 0;
        this.paramValue = paramValue.trim();
    }

    /**
     * Check if this parameter is a variable input parameter
     *
     * @return {@code true} if this is an input parameter, {@code false} otherwise
     */
    public final boolean isParam() {
        return isParam;
    }

    /**
     * Get the position of this parameter
     *
     * @return The index of this parameter (first index is 1)
     */
    public final int getPosition() {
        return position;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the variable value of this parameter
     *
     * @return The parameter value
     */
    public final String getParamValue() {
        return paramValue;
    }

    /**
     * Set the value for this parameter
     */
    public @Nullable Object getValue() {
        return value;
    }

    /**
     * Set the variable value of this parameter
     *
     * @param value
     *         The value to be set
     * @throws SQLException
     *         if this parameter contains a constant value
     */
    public void setValue(@Nullable Object value) throws SQLException {
        if (!isParam) {
            throw new SQLException("Cannot set parameter, since it is constant.", SQL_STATE_INVALID_PARAM_TYPE);
        }

        this.value = value;
        this.valueSet = true;
    }

    /**
     * Check if the value of this parameter has been set
     *
     * @return {@code true} if the value has been set, {@code false} otherwise
     */
    public boolean isValueSet() {
        return valueSet;
    }

    /**
     * Get the SQL type of this parameter.
     *
     * @return The SQL type of this parameter
     */
    public int getType() {
        return type;
    }

    /**
     * Set the SQL type of this parameter
     *
     * @param type
     *         The SQL type of this parameter
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FBProcedureParam other)) return false;
        // TODO this condition is flawed, but somehow current code relies on it
        return this.position == other.position &&
                this.value != null ? this.value.equals(other.value) :
                other.value == null;
    }

    @Override
    public int hashCode() {
        int hashCode = 887;
        hashCode = 31 * hashCode + position;
        hashCode = 31 * hashCode + (value != null ? value.hashCode() : 0);
        return hashCode;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("clone() unexpectedly not supported", e);
        }
    }
}
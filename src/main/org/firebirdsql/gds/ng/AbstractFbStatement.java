/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.listeners.RowListener;
import org.firebirdsql.gds.ng.listeners.RowListenerDispatcher;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public abstract class AbstractFbStatement implements FbStatement {

    private final Object syncObject = new Object();
    protected final RowListenerDispatcher rowListenerDispatcher = new RowListenerDispatcher();
    private volatile boolean allRowsFetched = false;
    private volatile StatementState state = StatementState.CLOSED;
    private volatile StatementType type = StatementType.NONE;
    private volatile RowDescriptor parameterDescriptor;
    private volatile RowDescriptor fieldDescriptor;

    /**
     * Plan information items
     */
    private static final byte[] DESCRIBE_PLAN_INFO_ITEMS = new byte[]{
            ISCConstants.isc_info_sql_get_plan
    };

    /**
     * Records affected items
     * TODO: Compare with current implementation
     */
    private static final byte[] ROWS_AFFECTED_INFO_ITEMS = new byte[]{
            ISCConstants.isc_info_sql_records
    };

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public void close() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (getState() == StatementState.CLOSED) return;
            // TODO do additional checks (see also old implementation and .NET)
            try {
                free(ISCConstants.DSQL_drop);
            } finally {
                rowListenerDispatcher.removeAllListeners();
                setState(StatementState.CLOSED);
                setType(StatementType.NONE);
            }
        }
    }

    /**
     * StatementState values indicating that cursor is closed
     * TODO Should also include ALLOCATED and PREPARED?
     */
    protected static final Set<StatementState> STATE_CURSOR_CLOSED =
            Collections.unmodifiableSet(EnumSet.of(StatementState.CLOSED, StatementState.IDLE));

    /**
     * StatementType values that can have a cursor
     */
    protected static final Set<StatementType> TYPE_HAS_CURSOR =
            Collections.unmodifiableSet(EnumSet.of(StatementType.SELECT, StatementType.SELECT_FOR_UPDATE, StatementType.STORED_PROCEDURE));

    @Override
    public void closeCursor() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (STATE_CURSOR_CLOSED.contains(getState())) return;
            // TODO do additional checks (see also old implementation and .NET)
            try {
                if (TYPE_HAS_CURSOR.contains(getType())) {
                    free(ISCConstants.DSQL_close);
                }
            } finally {
                // TODO Close in case of exception?
                setState(StatementState.IDLE);
            }
        }
    }

    @Override
    public StatementState getState() {
        return state;
    }

    /**
     * Sets the StatementState.
     *
     * @param state
     *         New state
     */
    protected void setState(StatementState state) {
        // TODO Check valid transition?
        synchronized (getSynchronizationObject()) {
            this.state = state;
        }
    }

    @Override
    public final StatementType getType() {
        return type;
    }

    /**
     * Sets the StatementType
     *
     * @param type
     *         New type
     */
    protected void setType(StatementType type) {
        synchronized (getSynchronizationObject()) {
            this.type = type;
        }
    }

    public byte[] getDescribePlanInfoItems() {
        return DESCRIBE_PLAN_INFO_ITEMS.clone();
    }

    public byte[] getRowsAffectedInfoItems() {
        return ROWS_AFFECTED_INFO_ITEMS.clone();
    }

    /**
     * Queues row data for consumption
     *
     * @param rowData
     *         Row data
     */
    protected final void queueRowData(List<FieldValue> rowData) {
        rowListenerDispatcher.newRow(this, rowData);
    }

    /**
     * Sets the <code>allRowsFetched</code> property.
     * <p>
     * When set to true all registered {@link RowListener} instances are notified for the {@link RowListener#allRowsFetched(FbStatement)}
     * event.
     * </p>
     *
     * @param allRowsFetched
     *         <code>true</code>: all rows fetched, <code>false</code> not all rows fetched.
     */
    protected final void setAllRowsFetched(boolean allRowsFetched) {
        synchronized (getSynchronizationObject()) {
            this.allRowsFetched = allRowsFetched;
        }
        if (allRowsFetched) {
            rowListenerDispatcher.allRowsFetched(this);
        }
    }

    protected final boolean isAllRowsFetched() {
        return allRowsFetched;
    }

    /**
     * Reset statement state, equivalent to calling {@link #reset(boolean)} with <code>false</code>
     */
    protected final void reset() {
        reset(false);
    }

    /**
     * Reset statement state and clear parameter description, equivalent to calling {@link #reset(boolean)} with <code>true</code>
     */
    protected final void resetAll() {
        reset(true);
    }

    /**
     * Resets the statement for next execution. Implementation in derived class must synchronize on {@link #getSynchronizationObject()} and
     * call <code>super.reset(resetAll)</code>
     *
     * @param resetAll
     *         Also reset field and parameter info
     */
    protected void reset(boolean resetAll) {
        synchronized (getSynchronizationObject()) {
            setAllRowsFetched(false);

            if (resetAll) {
                setParameterDescriptor(null);
                setFieldDescriptor(null);
            }
        }
    }

    @Override
    public final RowDescriptor getParameterDescriptor() throws SQLException {
        checkStatementOpen();
        return parameterDescriptor;
    }

    /**
     * Sets the parameter descriptor.
     *
     * @param parameterDescriptor
     *         Parameter descriptor
     */
    protected final void setParameterDescriptor(RowDescriptor parameterDescriptor) {
        synchronized (getSynchronizationObject()) {
            this.parameterDescriptor = parameterDescriptor;
        }
    }

    @Override
    public final RowDescriptor getFieldDescriptor() throws SQLException {
        checkStatementOpen();
        return fieldDescriptor;
    }

    /**
     * Sets the (result set) field descriptor.
     *
     * @param fieldDescriptor
     *         Field descriptor
     */
    protected final void setFieldDescriptor(RowDescriptor fieldDescriptor) {
        synchronized (getSynchronizationObject()) {
            this.fieldDescriptor = fieldDescriptor;
        }
    }

    /**
     * @return The (full) statement info request items.
     * @see #getParameterDescriptionInfoRequestItems()
     */
    public abstract byte[] getStatementInfoRequestItems();

    /**
     * @return The <tt>isc_info_sql_describe_vars</tt> info request items.
     * @see #getStatementInfoRequestItems()
     */
    public abstract byte[] getParameterDescriptionInfoRequestItems();

    /**
     * Request statement info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    public abstract <T> T getSqlInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor) throws SQLException;

    /**
     * Request statement info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @return Response buffer
     * @throws SQLException
     */
    public abstract byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException;

    /**
     * Frees the currently allocated statement (either close the cursor with {@link ISCConstants#DSQL_close} or drop the statement
     * handle using {@link ISCConstants#DSQL_drop}.
     *
     * @param option
     *         Free option
     * @throws SQLException
     */
    protected abstract void free(int option) throws SQLException;

    /**
     * Validates if the number of parameters matches the expected number and types, and if all values have been set.
     *
     * @param parameters
     *         List of parameters
     * @throws SQLException
     *         When the number or type of parameters does not match {@link #getParameterDescriptor()}, or when a parameter has not been set.
     */
    protected void validateParameters(final List<FieldValue> parameters) throws SQLException {
        final RowDescriptor parameterDescriptor = getParameterDescriptor();
        final int expectedSize = parameterDescriptor != null ? parameterDescriptor.getCount() : 0;
        final int actualSize = parameters.size();
        // TODO Externalize sqlstates
        if (actualSize != expectedSize) {
            // TODO use HY021 (inconsistent descriptor information) instead?
            throw new SQLNonTransientException(String.format("Invalid number of parameters, expected %d, got %d",
                    expectedSize, actualSize), "07008"); // invalid descriptor count
        }
        for (int fieldIndex = 0; fieldIndex < actualSize; fieldIndex++) {
            FieldValue fieldValue = parameters.get(fieldIndex);
            if (fieldValue == null || !fieldValue.isInitialized()) {
                // Communicating 1-based index, so it doesn't cause confusion when JDBC user sees this.
                // TODO use HY000 (dynamic parameter value needed) instead?
                throw new SQLTransientException(String.format("Parameter with index %d was not set",
                        fieldIndex + 1), "0700C"); // undefined DATA value
            }
            if (!fieldValue.getFieldDescriptor().equals(parameterDescriptor.getFieldDescriptor(fieldIndex))) {
                // Communicating 1-based index, so it doesn't cause confusion when JDBC user sees this.
                // TODO use HY021 (inconsistent descriptor information) or HY091 (invalid descriptor field identifier) instead?
                // TODO Use isc_field_ref_err?
                throw new SQLNonTransientException(String.format("Parameter with index %d has an unexpected descriptor (expected %s, got %s)",
                        fieldIndex + 1, parameterDescriptor.getFieldDescriptor(fieldIndex), fieldValue.getFieldDescriptor()), "07009"); // invalid descriptor index
            }
        }
    }

    @Override
    public final void addRowListener(RowListener rowListener) {
        // TODO What to do after statement close?
        rowListenerDispatcher.addListener(rowListener);
    }

    @Override
    public final void removeRowListener(RowListener rowListener) {
        rowListenerDispatcher.removeListener(rowListener);
    }

    /**
     * Checks if this statement is not in {@link StatementState#CLOSED}, and throws an <code>SQLException</code> if it is.
     *
     * @throws SQLException
     *         When this statement is closed.
     */
    protected final void checkStatementOpen() throws SQLException {
        if (getState() == StatementState.CLOSED) {
            // TODO Externalize sqlstate
            // TODO See if there is a firebird error code matching this (isc_cursor_not_open is not exactly the same)
            throw new SQLException("Statement closed", "24000");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (getState() != StatementState.CLOSED) close();
        } finally {
            super.finalize();
        }
    }
}

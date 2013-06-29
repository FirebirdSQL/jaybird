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

import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public abstract class AbstractFbStatement implements FbStatement {

    private final Object syncObject = new Object();
    private final AtomicReference<StatementState> state = new AtomicReference<StatementState>(StatementState.CLOSED);
    private final AtomicReference<StatementType> type = new AtomicReference<StatementType>(StatementType.NONE);

    /**
     * Plan information items
     */
    private static final byte[] DESCRIBE_PLAN_INFO_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_get_plan
    };

    /**
     * Records affected items
     * TODO: Compare with current implementation
     */
    private static final byte[] ROWS_AFFECTED_INFO_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_records
    };

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public void close() {
        synchronized (getSynchronizationObject()) {
            if (getState() == StatementState.CLOSED) return;
            // TODO do additional checks (see also old implementation and .NET)
            try {
                free(ISCConstants.DSQL_drop);
            } finally {
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
        return state.get();
    }

    /**
     * Sets the StatementState.
     *
     * @param state
     *         New state
     */
    protected void setState(StatementState state) {
        // TODO Check valid transition?
        this.state.set(state);
    }

    @Override
    public StatementType getType() {
        return type.get();
    }

    /**
     * Sets the StatementType
     *
     * @param type
     *         New type
     */
    protected void setType(StatementType type) {
        this.type.set(type);
    }

    public byte[] getDescribePlanInfoItems() {
        return DESCRIBE_PLAN_INFO_ITEMS.clone();
    }

    public byte[] getRowsAffectedInfoItems() {
        return ROWS_AFFECTED_INFO_ITEMS.clone();
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

    protected abstract void free(int option);
}

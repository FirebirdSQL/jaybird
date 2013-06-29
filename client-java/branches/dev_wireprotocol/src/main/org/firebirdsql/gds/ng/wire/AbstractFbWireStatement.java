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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractFbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.FBSQLException;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class AbstractFbWireStatement extends AbstractFbStatement implements FbWireStatement {

    protected final AtomicBoolean allRowsFetched = new AtomicBoolean(false);
    private final AtomicReference<FbWireTransaction> transaction = new AtomicReference<FbWireTransaction>();
    protected FbWireDatabase database;
    private int handle;
    // TODO Wrap in AtomicReference?
    private RowDescriptor parameters;
    private RowDescriptor fields;

    public AbstractFbWireStatement(FbWireDatabase database) {
        this.database = database;
    }

    protected final XdrInputStream getXdrIn() throws SQLException {
        return database.getXdrIn();
    }

    protected final XdrOutputStream getXdrOut() throws SQLException {
        return database.getXdrOut();
    }

    protected final FbWireDatabase getDatabase() {
        return database;
    }

    @Override
    public FbTransaction getTransaction() throws SQLException {
        return transaction.get();
    }

    @Override
    public void setTransaction(FbTransaction transaction) throws SQLException {
        if (!(transaction instanceof FbWireTransaction)) {
            throw new SQLNonTransientException(String.format("Invalid transaction handle, expected instance of FbWireTransaction, got \"%s\"", transaction.getClass().getName()),
                    FBSQLException.SQL_STATE_GENERAL_ERROR);
        }
        // TODO Needs synchronization?
        // TODO Is there a statement or transaction state where we should not be switching transactions?
        if (transaction == this.transaction.get()) return;
        this.transaction.set((FbWireTransaction) transaction);
        // TODO Implement + add transaction listener
    }

    @Override
    public final RowDescriptor getParameters() throws SQLException {
        return parameters;
    }

    protected final void setParameters(RowDescriptor parameters) {
        this.parameters = parameters;
    }

    @Override
    public final RowDescriptor getFields() throws SQLException {
        return fields;
    }

    protected final void setFields(RowDescriptor fields) {
        this.fields = fields;
    }

    @Override
    public final int getHandle() {
        return handle;
    }

    protected final void setHandle(int handle) {
        this.handle = handle;
    }
}

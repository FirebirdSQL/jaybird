/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.BlobParameterBufferImp;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher;
import org.firebirdsql.gds.ng.listeners.TransactionListener;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * Abstract implementation of {@link org.firebirdsql.gds.ng.FbDatabase} with behavior common to the various
 * implementations.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbDatabase<T extends AbstractConnection<IConnectionProperties, ? extends FbDatabase>>
        extends AbstractFbAttachment<T> implements FbDatabase, TransactionListener {

    private static final System.Logger log = System.getLogger(AbstractFbDatabase.class.getName());

    /**
     * Info-request block for database information.
     * <p>
     * Must match with processing in {@link org.firebirdsql.gds.ng.AbstractFbDatabase.DatabaseInformationProcessor}.
     * </p>
     */
    // @formatter:off
    private static final byte[] DESCRIBE_DATABASE_INFO_BLOCK = new byte[]{
            isc_info_db_sql_dialect,
            isc_info_firebird_version,
            isc_info_ods_version,
            isc_info_ods_minor_version,
            isc_info_end };
    // @formatter:on

    protected final DatabaseListenerDispatcher databaseListenerDispatcher = new DatabaseListenerDispatcher();
    private final Set<FbTransaction> activeTransactions = new HashSet<>();
    private final WarningMessageCallback warningCallback =
            warning -> databaseListenerDispatcher.warningReceived(AbstractFbDatabase.this, warning);
    private final RowDescriptor emptyRowDescriptor;
    private short databaseDialect;
    private int odsMajor;
    private int odsMinor;

    protected AbstractFbDatabase(T connection, DatatypeCoder datatypeCoder) {
        super(connection, datatypeCoder);
        emptyRowDescriptor = RowDescriptor.empty(datatypeCoder);
    }

    /**
     * @return The warning callback for this database.
     */
    public final WarningMessageCallback getDatabaseWarningCallback() {
        return warningCallback;
    }

    /**
     * @return Number of active (not prepared or committed/rolled back) transactions
     */
    public final int getActiveTransactionCount() {
        try (LockCloseable ignored = withLock()) {
            return activeTransactions.size();
        }
    }

    /**
     * Called when a transaction is added by the database.
     * <p>
     * Only this {@link org.firebirdsql.gds.ng.AbstractFbDatabase} instance should call this method.
     * </p>
     */
    protected final void transactionAdded(FbTransaction transaction) {
        try (LockCloseable ignored = withLock()) {
            if (transaction.getState() == TransactionState.ACTIVE) {
                activeTransactions.add(transaction);
            }
            transaction.addTransactionListener(this);
            transaction.addExceptionListener(exceptionListenerDispatcher);
        }
    }

    @Override
    public final short getConnectionDialect() {
        return (short) connection.getAttachProperties().getSqlDialect();
    }

    @Override
    public final short getDatabaseDialect() {
        return databaseDialect;
    }

    /**
     * Sets the dialect of the database.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param dialect
     *         Dialect of the database/connection
     */
    protected final void setDatabaseDialect(short dialect) {
        this.databaseDialect = dialect;
    }

    @Override
    public final void addDatabaseListener(DatabaseListener listener) {
        databaseListenerDispatcher.addListener(listener);
    }

    @Override
    public final void addWeakDatabaseListener(DatabaseListener listener) {
        databaseListenerDispatcher.addWeakListener(listener);
    }

    @Override
    public final void removeDatabaseListener(DatabaseListener listener) {
        databaseListenerDispatcher.removeListener(listener);
    }

    /**
     * Actual implementation of database detach.
     * <p>
     * Implementations of this method should only be called from {@link #close()}, and should <strong>not</strong>
     * notify database listeners of the database {@link DatabaseListener#detaching(FbDatabase)} and
     * {@link DatabaseListener#detached(FbDatabase)} events.
     * </p>
     */
    protected abstract void internalDetach() throws SQLException;

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Calls {@link #checkConnected()} and notifies database listeners of the detaching event, then
     * calls {@link #internalDetach()} and finally notifies database listeners of database detach and removes all
     * listeners.
     * </p>
     */
    @Override
    public final void close() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkConnected();
            int activeTransactionCount = getActiveTransactionCount();
            if (activeTransactionCount > 0) {
                // Throw open transactions as exception, fbclient doesn't disconnect with outstanding (unprepared)
                // transactions
                // In the case of wire protocol we could ignore this and simply close, but that would be
                // inconsistent with fbclient
                throw FbExceptionBuilder.forException(isc_open_trans)
                        .messageParameter(activeTransactionCount)
                        .toSQLException();
            }

            databaseListenerDispatcher.detaching(this);
            try {
                internalDetach();
            } finally {
                databaseListenerDispatcher.detached(this);
                databaseListenerDispatcher.shutdown();
                exceptionListenerDispatcher.shutdown();
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        } finally {
            exceptionListenerDispatcher.shutdown();
        }
    }

    @Override
    public final int getOdsMajor() {
        return odsMajor;
    }

    /**
     * Sets the ODS (On Disk Structure) major version of the database associated
     * with this connection.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param odsMajor
     *         ODS major version
     */
    protected final void setOdsMajor(int odsMajor) {
        this.odsMajor = odsMajor;
    }

    @Override
    public final int getOdsMinor() {
        return odsMinor;
    }

    /**
     * Sets the ODS (On Disk Structure) minor version of the database associated
     * with this connection.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param odsMinor
     *         The ODS minor version
     */
    protected final void setOdsMinor(int odsMinor) {
        this.odsMinor = odsMinor;
    }

    /**
     * @return The (full) statement info request items.
     * @see #getParameterDescriptionInfoRequestItems()
     */
    public final byte[] getStatementInfoRequestItems() {
        return getServerVersionInformation().getStatementInfoRequestItems();
    }

    /**
     * @return The {@code isc_info_sql_describe_vars} info request items.
     * @see #getStatementInfoRequestItems()
     */
    public final byte[] getParameterDescriptionInfoRequestItems() {
        return getServerVersionInformation().getParameterDescriptionInfoRequestItems();
    }

    @Override
    public final <R> R getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<R> infoProcessor)
            throws SQLException {
        final byte[] responseBuffer = getDatabaseInfo(requestItems, bufferLength);
        try {
            return infoProcessor.process(responseBuffer);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    protected byte[] getDescribeDatabaseInfoBlock() {
        return DESCRIBE_DATABASE_INFO_BLOCK;
    }

    protected InfoProcessor<FbDatabase> getDatabaseInformationProcessor() {
        return new DatabaseInformationProcessor();
    }

    @Override
    public final void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        switch (newState) {
        /* Even if the commit or rollback fails, we no longer consider it an active transaction
           Introduced as the commit/rollback might fail in a shutdown database (at least in FB 2.1) and a
           subsequent close wouldn't as there are "active" transactions.
           This is acceptable as commit/rollback failure should be limited to situations were the database
           is either inaccessible and the transaction is likely already rolled back or pending rollback by
           the server, or the transaction was already committed or rolled back.
         */
        case COMMITTING, ROLLING_BACK, PREPARED -> {
            // TODO for PREPARED, "register" transaction as pendingEnd for debugging?
            try (LockCloseable ignored = withLock()) {
                activeTransactions.remove(transaction);
            }
        }
        case COMMITTED, ROLLED_BACK -> {
            try (LockCloseable ignored = withLock()) {
                activeTransactions.remove(transaction);
                transaction.removeTransactionListener(this);
                transaction.removeExceptionListener(exceptionListenerDispatcher);
            }
        }
        default -> {
            // do nothing
        }
        }
    }

    @Override
    public BlobParameterBuffer createBlobParameterBuffer() {
        return new BlobParameterBufferImp();
    }

    @Override
    public TransactionParameterBufferImpl createTransactionParameterBuffer() {
        return new TransactionParameterBufferImpl();
    }

    @Override
    public IConnectionProperties getConnectionProperties() {
        return connection.getAttachProperties().asImmutable();
    }

    @Override
    public final RowDescriptor emptyRowDescriptor() {
        return emptyRowDescriptor;
    }

    /**
     * Encodes the transactionId for use in {@code isc_reconnect_transaction}/{@code op_reconnect}.
     *
     * @param transactionId
     *         transaction id
     * @return byte array (4 bytes for max 31-bit transaction id, 8 bytes for larger transaction id)
     */
    protected byte[] getTransactionIdBuffer(long transactionId) {
        // Note: This uses an atypical encoding (as this is actually a TPB without a type)
        byte[] buf;
        if ((transactionId & 0x7FFF_FFFFL) == transactionId) {
            buf = new byte[4];
            VaxEncoding.encodeVaxIntegerWithoutLength(buf, 0, (int) transactionId);
        } else {
            // assume this is FB 3, because FB 2.5 and lower only have 31 bits tx ids; might fail if this path is
            // triggered on FB 2.5 and lower
            buf = new byte[8];
            VaxEncoding.encodeVaxLongWithoutLength(buf, 0, transactionId);
        }
        return buf;
    }

    private class DatabaseInformationProcessor implements InfoProcessor<FbDatabase> {
        @Override
        public FbDatabase process(byte[] info) throws SQLException {
            if (info.length == 0) {
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                        .messageParameter("database")
                        .toSQLException();
            }
            int i = 0;
            while (info[i] != isc_info_end) {
                int arg = info[i++];
                switch (arg) {
                case isc_info_db_sql_dialect, isc_info_ods_version, isc_info_ods_minor_version -> {
                    int len = iscVaxInteger2(info, i);
                    i += 2;
                    int value = iscVaxInteger(info, i, len);
                    i += len;
                    switch (arg) {
                    case isc_info_db_sql_dialect -> setDatabaseDialect((short) value);
                    case isc_info_ods_version -> setOdsMajor(value);
                    case isc_info_ods_minor_version -> setOdsMinor(value);
                    }
                }
                case isc_info_firebird_version -> {
                    int len = iscVaxInteger2(info, i);
                    i += 2;
                    int expectedIndex = i + len;
                    int versionCount = info[i++] & 0xFF;
                    String[] versionParts = new String[versionCount];
                    for (int versionIndex = 0; versionIndex < versionCount; versionIndex++) {
                        int versionLength = info[i++] & 0xFF;
                        versionParts[versionIndex] = new String(info, i, versionLength, StandardCharsets.UTF_8);
                        i += versionLength;
                    }
                    assert i == expectedIndex : "Parsing version information lead to wrong index";
                    setServerVersion(versionParts);
                }
                case isc_info_truncated -> {
                    log.log(System.Logger.Level.DEBUG, "Received isc_info_truncated");
                    return AbstractFbDatabase.this;
                }
                default -> throw FbExceptionBuilder.forException(isc_infunk).toSQLException();
                }
            }
            return AbstractFbDatabase.this;
        }
    }
}

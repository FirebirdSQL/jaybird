/*
 * $Id$
 * 
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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSServerVersionException;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * Abstract implementation of {@link org.firebirdsql.gds.ng.FbDatabase} with behavior common to the various
 * implementations.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbDatabase implements FbDatabase, TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbDatabase.class);

    /**
     * Info-request block for database information.
     * <p>
     * TODO Move to FbDatabase interface? Will this vary with versions of Firebird?
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

    private final DatabaseListenerDispatcher databaseListenerDispatcher = new DatabaseListenerDispatcher();
    private final AtomicBoolean attached = new AtomicBoolean();
    private final Set<FbTransaction> activeTransactions = Collections.synchronizedSet(new HashSet<FbTransaction>());
    private final WarningMessageCallback warningCallback = new WarningMessageCallback() {
        @Override
        public void processWarning(SQLWarning warning) {
            databaseListenerDispatcher.warningReceived(AbstractFbDatabase.this, warning);
        }
    };
    private final Object syncObject = new Object();
    private short databaseDialect;
    private int odsMajor;
    private int odsMinor;
    private GDSServerVersion serverVersion;
    private ServerVersionInformation serverVersionInformation;

    /**
     * @return The warning callback for this database.
     */
    protected final WarningMessageCallback getDatabaseWarningCallback() {
        return warningCallback;
    }

    /**
     * @return Number of active (not prepared or committed/rolled back) transactions
     */
    public final int getActiveTransactionCount() {
        return activeTransactions.size();
    }

    /**
     * Called when a transaction is added by the database.
     * <p>
     * Only this {@link org.firebirdsql.gds.ng.AbstractFbDatabase} instance should call this method.
     * </p>
     */
    protected final void transactionAdded(FbTransaction transaction) {
        synchronized (activeTransactions) {
            if (transaction.getState() == TransactionState.ACTIVE) {
                activeTransactions.add(transaction);
            }
            transaction.addTransactionListener(this);
        }
    }

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public boolean isAttached() {
        return attached.get();
    }

    /**
     * Called when this database is attached.
     * <p>
     * Only this {@link org.firebirdsql.gds.ng.AbstractFbDatabase} instance should call this method.
     * </p>
     */
    protected final void setAttached() {
        attached.set(true);
    }

    /**
     * Called when this database is detached.
     * <p>
     * Only this {@link org.firebirdsql.gds.ng.AbstractFbDatabase} instance should call this method.
     * </p>
     */
    protected final void setDetached() {
        attached.set(false);
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
    public final void removeDatabaseListener(DatabaseListener listener) {
        databaseListenerDispatcher.removeListener(listener);
    }

    /**
     * Checks if the database is connected, and throws a {@link SQLException} if it isn't connected.
     */
    protected abstract void checkConnected() throws SQLException;

    /**
     * Actual implementation of database detach.
     * <p>
     * Implementations of this method should only be called from {@link #detach()}, and should <strong>not</strong> notify database
     * listeners of the database {@link DatabaseListener#detaching(FbDatabase)} and
     * {@link DatabaseListener#detached(FbDatabase)} events.
     * </p>
     */
    protected abstract void internalDetach() throws SQLException;

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Calls {@link #checkConnected()} and notifies database listeners of the detaching event, then
     * calls {@link #internalDetach()} and finally notifies database listeners of database detach and removes all listeners.
     * </p>
     */
    @Override
    public final void detach() throws SQLException {
        checkConnected();
        synchronized (getSynchronizationObject()) {
            if (getActiveTransactionCount() > 0) {
                // Throw open transactions as exception, fbclient doesn't disconnect with outstanding (unprepared) transactions
                // In the case of wire protocol we could ignore this and simply close, but that would be inconsistent with fbclient
                // TODO: Rollback transactions; or leave that to the caller?
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_open_trans)
                        .messageParameter(getActiveTransactionCount())
                        .toSQLException();
            }

            databaseListenerDispatcher.detaching(this);
            try {
                internalDetach();
            } finally {
                databaseListenerDispatcher.detached(this);
                databaseListenerDispatcher.shutdown();
            }
        }
    }

    /**
     * Performs {@link #detach()} suppressing any exception.
     */
    protected void safelyDetach() {
        try {
            detach();
        } catch (Exception ex) {
            // ignore, but log
            log.debug("Exception on safely detach", ex);
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

    @Override
    public final GDSServerVersion getServerVersion() {
        return serverVersion;
    }

    /**
     * Sets the Firebird version string.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param versionString
     *         Raw version string
     */
    protected final void setServerVersion(String versionString) {
        try {
            serverVersion = GDSServerVersion.parseRawVersion(versionString);
        } catch (GDSServerVersionException e) {
            log.error(String.format("Received unsupported server version \"%s\", replacing with dummy invalid version ", versionString), e);
            serverVersion = GDSServerVersion.INVALID_VERSION;
        }
        serverVersionInformation = ServerVersionInformation.getForVersion(serverVersion);
    }

    /**
     * @return The (full) statement info request items.
     * @see #getParameterDescriptionInfoRequestItems()
     */
    public final byte[] getStatementInfoRequestItems() {
        return serverVersionInformation.getStatementInfoRequestItems();
    }

    /**
     * @return The {@code isc_info_sql_describe_vars} info request items.
     * @see #getStatementInfoRequestItems()
     */
    public final byte[] getParameterDescriptionInfoRequestItems() {
        return serverVersionInformation.getParameterDescriptionInfoRequestItems();
    }

    @Override
    public <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        byte[] responseBuffer = getDatabaseInfo(requestItems, bufferLength);
        return infoProcessor.process(responseBuffer);
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
        case PREPARED:
            activeTransactions.remove(transaction);
            break;
        case COMMITTED:
        case ROLLED_BACK:
            activeTransactions.remove(transaction);
            transaction.removeTransactionListener(this);
            break;
        default:
            // do nothing
            break;
        }
    }

    private class DatabaseInformationProcessor implements InfoProcessor<FbDatabase> {
        @Override
        public FbDatabase process(byte[] info) throws SQLException {
            boolean debug = log.isDebugEnabled();
            if (info.length == 0) {
                throw new SQLException("Response buffer for database information request is empty");
            }
            if (debug)
                log.debug(String.format("DatabaseInformationProcessor.process: first 2 bytes are %04X or: %02X, %02X",
                        iscVaxInteger2(info, 0), info[0], info[1]));
            int value;
            int len;
            int i = 0;
            while (info[i] != ISCConstants.isc_info_end) {
                switch (info[i++]) {
                case ISCConstants.isc_info_db_sql_dialect:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    setDatabaseDialect((short) value);
                    if (debug) log.debug("isc_info_db_sql_dialect:" + value);
                    break;
                case ISCConstants.isc_info_ods_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    setOdsMajor(value);
                    if (debug) log.debug("isc_info_ods_version:" + value);
                    break;
                case ISCConstants.isc_info_ods_minor_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    setOdsMinor(value);
                    if (debug) log.debug("isc_info_ods_minor_version:" + value);
                    break;
                case ISCConstants.isc_info_firebird_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    String firebirdVersion = new String(info, i + 2, len - 2);
                    i += len;
                    setServerVersion(firebirdVersion);
                    if (debug) log.debug("isc_info_firebird_version:" + firebirdVersion);
                    break;
                case ISCConstants.isc_info_truncated:
                    if (debug) log.debug("isc_info_truncated ");
                    return AbstractFbDatabase.this;
                default:
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_infunk).toSQLException();
                }
            }
            return AbstractFbDatabase.this;
        }
    }
}

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

package org.firebirdsql.jca;

import java.io.Serializable;
import java.sql.Connection;

import javax.resource.ResourceException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * The <code>FBTpb</code> class represents the Firebird Transaction Parameter
 * Block (TPB), which contains Firebird-specific information about transaction
 * isolation.
 * 
 * Created: Wed Jun 19 10:12:22 2002
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 */

public class FBTpb implements Serializable {

    private byte[] byteArray = null;

    /**
     * Dirty reads, non-repeatable reads and phantom reads are prevented. This
     * level includes the prohibitions in TRANSACTION_REPEATABLE_READ and
     * further prohibits the situation where one transaction reads all rows that
     * satisfy a WHERE condition, a second transaction inserts a row that
     * satisfies that WHERE condition, and the first transaction rereads for the
     * same condition, retrieving the additional "phantom" row in the second
     * read.
     */
    public static final String TRANSACTION_SERIALIZABLE = "TRANSACTION_SERIALIZABLE";

    /**
     * Dirty reads and non-repeatable reads are prevented; phantom reads can
     * occur. This level prohibits a transaction from reading a row with
     * uncommitted changes in it, and it also prohibits the situation where one
     * transaction reads a row, a second transaction alters the row, and the
     * first transaction rereads the row, getting different values the second
     * time (a "non-repeatable read").
     */
    public static final String TRANSACTION_REPEATABLE_READ = "TRANSACTION_REPEATABLE_READ";

    /**
     * Dirty reads are prevented; non-repeatable reads and phantom reads can
     * occur. This level only prohibits a transaction from reading a row with
     * uncommitted changes in it.
     */
    public static final String TRANSACTION_READ_COMMITTED = "TRANSACTION_READ_COMMITTED";

    // read uncommitted actually not supported
    /**
     * Dirty reads, non-repeatable reads and phantom reads can occur. This level
     * allows a row changed by one transaction to be read by another transaction
     * before any changes in that row have been committed (a "dirty read"). If
     * any of the changes are rolled back, the second transaction will have
     * retrieved an invalid row. <b>This level is not actually supported </b>
     */
    public static final String TRANSACTION_READ_UNCOMMITTED = "TRANSACTION_READ_UNCOMMITTED";

    /**
     * Indicates that transactions are not supported. <b>This level is not
     * supported </b>
     */
    public static final String TRANSACTION_NONE = "TRANSACTION_NONE";

    private final static Integer ISC_TPB_CONSISTENCY = new Integer(
            ISCConstants.isc_tpb_consistency);

    private final static Integer ISC_TPB_CONCURRENCY = new Integer(
            ISCConstants.isc_tpb_concurrency);

    private final static Integer ISC_TPB_READ_COMMITTED = new Integer(
            ISCConstants.isc_tpb_read_committed);

    private final static Integer ISC_TPB_REC_VERSION = new Integer(
            ISCConstants.isc_tpb_rec_version);

    private final static Integer ISC_TPB_NO_REC_VERSION = new Integer(
            ISCConstants.isc_tpb_no_rec_version);

    private final static Integer ISC_TPB_WAIT = new Integer(
            ISCConstants.isc_tpb_wait);

    private final static Integer ISC_TPB_NOWAIT = new Integer(
            ISCConstants.isc_tpb_nowait);

    private final static Integer ISC_TPB_READ = new Integer(
            ISCConstants.isc_tpb_read);

    private final static Integer ISC_TPB_WRITE = new Integer(
            ISCConstants.isc_tpb_write);

    private FBTpbMapper mapper;
    private TransactionParameterBuffer tpb;
    private boolean readOnly;
    private int txIsolation;

    /**
     * Create a new Transaction Parameters Block instance based around a
     * <code>FBTpbMapper</code>.
     * 
     * @param mapper
     *            The <code>FBTpbMapper</code> to be used with this
     *            <code>FBTpb</code>
     */
    public FBTpb(FBTpbMapper mapper) {
        this.mapper = mapper;
        this.tpb = mapper.getDefaultMapping();
        this.txIsolation = Connection.TRANSACTION_READ_COMMITTED;
        this.readOnly = false;
    }

    /**
     * Get the <code>FBTpbMapper</code> around which this <code>FBTpb</code>
     * instance is based.
     * 
     * @return The <code>FBTpbMapper</code> instance currently employed
     */
    public FBTpbMapper getMapper() {
        return mapper;
    }

    /**
     * Set the <code>FBTpbMapper</code> to use with this <code>FBTpb</code>.
     * 
     * @param mapper
     *            The mapper to be used
     * @throws ResourceException
     *             if the <code>FBTpbMapper</code> cannot be set
     */
    public void setMapper(FBTpbMapper mapper) throws FBResourceException {
        this.mapper = mapper;
        this.tpb = mapper.getMapping(txIsolation);
    }

    public boolean equals(Object other) {
        if (other == this) 
            return true; 
        
        if (!(other instanceof FBTpb)) 
            return false;
        
        return tpb.equals(((FBTpb) other).tpb);
    }

    public int hashCode() {
        return tpb.hashCode();
    }

    /**
     * Copy constructor. Create a new <code>FBTpb</code> instance around an
     * existing one.
     * 
     * @param tpb
     *            An existing <code>FBTpb</code> instance
     */
    public FBTpb(FBTpb tpb) {
        setTpb(tpb);
    }

    /**
     * Set the inner <code>FBTpb</code> instance to be used.
     * 
     * @param tpb
     *            The <code>FBTpb</code> to be used
     */
    public void setTpb(FBTpb tpb) {
        this.tpb = tpb.tpb.deepCopy();
        this.txIsolation = tpb.txIsolation;
        this.mapper = tpb.mapper;
        this.readOnly = tpb.readOnly;
    }

    /**
     * Add a Firebird-specific transaction attribute to this TPB. The key must
     * be one of the isc_tpb_* static final fields of {@link ISCConstants}.
     * 
     * @param key
     *            The transaction attribute to add
     */
    public void add(Integer key) {

        // check if value is correct
        switch (key.intValue()) {
            case ISCConstants.isc_tpb_concurrency:
            case ISCConstants.isc_tpb_consistency:
            case ISCConstants.isc_tpb_read_committed:
            case ISCConstants.isc_tpb_rec_version:
            case ISCConstants.isc_tpb_no_rec_version:
            case ISCConstants.isc_tpb_wait:
            case ISCConstants.isc_tpb_nowait:
            case ISCConstants.isc_tpb_lock_read:
            case ISCConstants.isc_tpb_lock_write:
                tpb.addArgument(key.intValue());
                break;

            case ISCConstants.isc_tpb_read:
                setReadOnly(true);
                break;

            case ISCConstants.isc_tpb_write:
                setReadOnly(false);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unrecognized Tpb parameter: " + key);
        }

//        createArray();
    }

    /**
     * Set the JDBC transaction isloation level by name. The name must be the
     * name of one of the TRANSACTION_* static final fields of this class.
     * 
     * @param tin
     *            The transaction isolation level to be set
     * @throws ResourceException
     *             if the given isolation level is invalid
     */
    public void setTransactionIsolationName(String tin)
            throws ResourceException {
        if (TRANSACTION_SERIALIZABLE.equals(tin)) {
            setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } // end of if ()
        else if (TRANSACTION_REPEATABLE_READ.equals(tin)) {
            setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } // end of if ()
        else if (TRANSACTION_READ_COMMITTED.equals(tin)) {
            setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } // end of if ()
        else {
            throw new FBResourceException("Unsupported tx isolation level");
        }
    }

    /**
     * Get the name of the currently set transaction isolation level. The
     * returned value will be one of the TRANSACTION_* static final fields of
     * this class.
     * 
     * @return The name of the current transaction isolation level
     * @throws ResourceException
     *             if the current isolation level cannot be retrieved
     */
    public String getTransactionIsolationName() throws ResourceException {
        switch (getTransactionIsolation()) {
            case Connection.TRANSACTION_SERIALIZABLE:
                return TRANSACTION_SERIALIZABLE;
            case Connection.TRANSACTION_REPEATABLE_READ:
                return TRANSACTION_REPEATABLE_READ;
            case Connection.TRANSACTION_READ_COMMITTED:
                return TRANSACTION_READ_COMMITTED;
            default:
                throw new FBResourceException(
                        "Unknown transaction isolation level");
        }
    }

    /**
     * Attempts to change the transaction isolation level to the one given. The
     * constants defined in the interface <code>Connection</code> are the
     * possible transaction isolation levels.
     * 
     * <P>
     * <B>Note: </B> This method cannot be called while in the middle of a
     * transaction.
     * 
     * @param level
     *            one of the TRANSACTION_* isolation values with the exception
     *            of TRANSACTION_NONE; some databases may not support other
     *            values
     * @throws SQLException
     *             if the isolation level is invalid
     */
    public void setTransactionIsolation(int level) throws ResourceException {

        switch (level) {
            case Connection.TRANSACTION_SERIALIZABLE:
            case Connection.TRANSACTION_REPEATABLE_READ:
            case Connection.TRANSACTION_READ_COMMITTED:

                tpb = mapper.getMapping(level);
                txIsolation = level;

                // apply read-only flag cached locally
                setReadOnly(readOnly);

                break;

            // promote to the higher isolation level,
            // because this one is not supported
            case Connection.TRANSACTION_READ_UNCOMMITTED:

                tpb = mapper.getMapping(Connection.TRANSACTION_READ_COMMITTED);
                txIsolation = Connection.TRANSACTION_READ_COMMITTED;

                // apply read-only flag cached locally
                setReadOnly(readOnly);

                break;

            default:
                throw new FBResourceException(
                        "Unsupported transaction isolation level");
        }
//        createArray();
    }

    /**
     * Gets this Connection's current transaction isolation level.
     * 
     * @return the current TRANSACTION_* mode value
     * @exception SQLException
     *                if a database access error occurs, should not be possible
     *                for this to be thrown
     */
    public int getTransactionIsolation() throws ResourceException {
        return txIsolation;
    }

    /**
     * Get Firebird transaction isolation level.
     * 
     * @return Firebird transaction isolation level.
     * 
     * @deprecated This method should not be used by applications because only
     *             JDBC transaction isolation levels should be used. Also
     *             corresponding setter method is deprecated, so using this
     *             method makes a little sense without it.
     */
    public int getIscTransactionIsolation() {
        
        if (tpb.hasArgument(TransactionParameterBuffer.CONSISTENCY)) 
            return ISCConstants.isc_tpb_consistency;
        
        if (tpb.hasArgument(TransactionParameterBuffer.READ_COMMITTED)) 
            return ISCConstants.isc_tpb_read_committed; 
        
        return ISCConstants.isc_tpb_concurrency;
    }

    /**
     * Set Firebird transaction isolation level.
     * 
     * @param isolation
     *            one of the {@link ISCConstants#isc_tpb_consistency},
     *            {@link ISCConstants#isc_tpb_concurrency}or
     *            {@link ISCConstants#isc_tpb_read_committed}.
     * 
     * @deprecated This method does not handle correctly JDBC-TPB mapping and
     *             should not be called if custom mapping is used.
     */
    public void setIscTransactionIsolation(int isolation) {
        try {
            switch (isolation) {
                case ISCConstants.isc_tpb_read_committed:
                    setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    break;
                case ISCConstants.isc_tpb_concurrency:
                    setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                    break;
                case ISCConstants.isc_tpb_consistency:
                    setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    break;
                default:
                    break;
            }
        } catch (ResourceException rex) {
            // should not happen at all

            throw new IllegalArgumentException(
                    "Specified transaction isolation is not supported.");
        }
    }

    /**
     * Set the read-only flag on this TPB.
     * 
     * @param readOnly
     *            If <code>true</code>, this TPB will be set to read-only,
     *            otherwise it will be be read-write
     */
    public void setReadOnly(boolean readOnly) {
        
        this.readOnly = readOnly;
        
        tpb.removeArgument(TransactionParameterBuffer.READ);
        tpb.removeArgument(TransactionParameterBuffer.WRITE);
        
        tpb.addArgument(readOnly ? TransactionParameterBuffer.READ
                : TransactionParameterBuffer.WRITE);
    }

    /**
     * Determine whether this TPB is set to read-only.
     * 
     * @return <code>true</code> if this TPB is read-only, otherwise false
     */
    public boolean isReadOnly() {
        return tpb.hasArgument(TransactionParameterBuffer.READ);
    }
    
    public TransactionParameterBuffer getTransactionParameterBuffer() {
        return tpb;
    }
}

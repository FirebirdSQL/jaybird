/*
 * $Id$
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

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterBufferHelper;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jca.FBResourceException;
import org.firebirdsql.util.ObjectUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is provides mapping capabilities between standard JDBC
 * transaction isolation level and Firebird Transaction Parameters Block (TPB).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBTpbMapper implements Serializable, Cloneable {

    private static final long serialVersionUID = 1690658870275668176L;

    public static FBTpbMapper getDefaultMapper(GDS gds) {
        return new FBTpbMapper(gds);
    }

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

    /**
     * Convert transaction isolation level into string.
     * 
     * @param isolationLevel transaction isolation level as integer constant.
     * 
     * @return corresponding string representation.
     */
    public static String getTransactionIsolationName(int isolationLevel) {
        switch (isolationLevel) {
        case Connection.TRANSACTION_NONE:
            return TRANSACTION_NONE;

        case Connection.TRANSACTION_READ_UNCOMMITTED:
            return TRANSACTION_READ_UNCOMMITTED;

        case Connection.TRANSACTION_READ_COMMITTED:
            return TRANSACTION_READ_COMMITTED;

        case Connection.TRANSACTION_REPEATABLE_READ:
            return TRANSACTION_REPEATABLE_READ;

        case Connection.TRANSACTION_SERIALIZABLE:
            return TRANSACTION_SERIALIZABLE;

        default:
            throw new IllegalArgumentException("Incorrect transaction isolation level.");
        }
    }

    /**
     * Convert transaction isolation level name into a corresponding constant.
     * 
     * @param isolationName name of the transaction isolation.
     * 
     * @return corresponding constant.
     */
    public static int getTransactionIsolationLevel(String isolationName) {
        if (TRANSACTION_NONE.equals(isolationName))
            return Connection.TRANSACTION_NONE;
        else if (TRANSACTION_READ_UNCOMMITTED.equals(isolationName))
            return Connection.TRANSACTION_READ_UNCOMMITTED;
        else if (TRANSACTION_READ_COMMITTED.equals(isolationName))
            return Connection.TRANSACTION_READ_COMMITTED;
        else if (TRANSACTION_REPEATABLE_READ.equals(isolationName))
            return Connection.TRANSACTION_REPEATABLE_READ;
        else if (TRANSACTION_SERIALIZABLE.equals(isolationName))
            return Connection.TRANSACTION_SERIALIZABLE;
        else
            throw new IllegalArgumentException("Invalid isolation name.");
    }

    // ConcurrentHashMap because changes can - potentially - be made concurrently
    private Map<Integer, TransactionParameterBuffer> mapping = new ConcurrentHashMap<Integer, TransactionParameterBuffer>();
    private int defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;

    /**
     * Create instance of this class with the default mapping of JDBC
     * transaction isolation levels to Firebird TPB.
     */
    public FBTpbMapper(GDS gds) {

        TransactionParameterBuffer serializableTpb = gds.newTransactionParameterBuffer();
        serializableTpb.addArgument(ISCConstants.isc_tpb_write);
        serializableTpb.addArgument(ISCConstants.isc_tpb_wait);
        serializableTpb.addArgument(ISCConstants.isc_tpb_consistency);

        TransactionParameterBuffer repeatableReadTpb = gds.newTransactionParameterBuffer();
        repeatableReadTpb.addArgument(ISCConstants.isc_tpb_write);
        repeatableReadTpb.addArgument(ISCConstants.isc_tpb_wait);
        repeatableReadTpb.addArgument(ISCConstants.isc_tpb_concurrency);

        TransactionParameterBuffer readCommittedTpb = gds.newTransactionParameterBuffer();
        readCommittedTpb.addArgument(ISCConstants.isc_tpb_write);
        readCommittedTpb.addArgument(ISCConstants.isc_tpb_wait);
        readCommittedTpb.addArgument(ISCConstants.isc_tpb_read_committed);
        readCommittedTpb.addArgument(ISCConstants.isc_tpb_rec_version);

        mapping.put(Connection.TRANSACTION_SERIALIZABLE, serializableTpb);
        mapping.put(Connection.TRANSACTION_REPEATABLE_READ, repeatableReadTpb);
        mapping.put(Connection.TRANSACTION_READ_COMMITTED, readCommittedTpb);
    }

    /**
     * Create instance of this class for the specified string mapping.
     * 
     * @param stringMapping mapping of JDBC transaction isolation to Firebird
     * mapping. Keys and values of this map must be strings. Keys can have 
     * following values:
     * <ul>
     * <li><code>"TRANSACTION_SERIALIZABLE"</code>
     * <li><code>"TRANSACTION_REPEATABLE_READ"</code>
     * <li><code>"TRANSACTION_READ_COMMITTED"</code>
     * <li><code>"TRANSACTION_READ_UNCOMMITTED"</code>
     * </ul>
     * Values are specified as comma-separated list of following keywords:
     * <ul>
     * <li><code>"isc_tpb_consistency"</code>
     * <li><code>"isc_tpb_concurrency"</code>
     * <li><code>"isc_tpb_read_committed"</code>
     * <li><code>"isc_tpb_rec_version"</code>
     * <li><code>"isc_tpb_no_rec_version"</code>
     * <li><code>"isc_tpb_wait"</code>
     * <li><code>"isc_tpb_nowait"</code>
     * <li><code>"isc_tpb_read"</code>
     * <li><code>"isc_tpb_write"</code>
     * <li><code>"isc_tpb_lock_read"</code>
     * <li><code>"isc_tpb_lock_write"</code>
     * <li><code>"isc_tpb_shared"</code>
     * <li><code>"isc_tpb_protected"</code>
     * </ul>
     * It is also allowed to strip "isc_tpb_" prefix from above shown constans.
     * Meaning of these constants and possible combinations you can find in a 
     * documentation.
     * 
     * @throws FBResourceException if mapping contains incorrect values.
     */
    public FBTpbMapper(GDS gds, Map<String, String> stringMapping) throws FBResourceException {
        this(gds);
        processMapping(gds, stringMapping);
    }

    /**
     * Process specified string mapping. This method updates default mapping
     * with values specified in a <code>stringMapping</code>.
     * 
     * @param stringMapping mapping to process.
     * 
     * @throws FBResourceException if mapping contains incorrect values.
     */
    private void processMapping(GDS gds, Map<String, String> stringMapping) throws FBResourceException {
        for (Map.Entry<String, String> entry : stringMapping.entrySet()) {
            String jdbcTxIsolation = entry.getKey();
            Integer isolationLevel;
            try {
                isolationLevel = getTransactionIsolationLevel(jdbcTxIsolation);
            } catch (IllegalArgumentException ex) {
                throw new FBResourceException(
                        "Transaction isolation " + jdbcTxIsolation +
                        " is not supported.");
            }
            TransactionParameterBuffer tpb = processMapping(gds, entry.getValue());
            mapping.put(isolationLevel, tpb);
        }
    }

    /**
     * Create instance of this class and load mapping from the specified
     * resource.
     * 
     * @param mappingResource name of the resource to load.
     * @param cl class loader that should be used to load specified resource.
     * 
     * @throws FBResourceException if resource cannot be loaded or contains
     * incorrect values.
     */
    public FBTpbMapper(GDS gds, String mappingResource, ClassLoader cl) throws FBResourceException {
        this(gds);
        try {
            ResourceBundle res = ResourceBundle.getBundle(
            		mappingResource, Locale.getDefault(), cl);

            Map<String, String> mapping = new HashMap<String, String>();

            Enumeration<String> en = res.getKeys();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                String value = res.getString(key);
                mapping.put(key, value);
            }

            processMapping(gds, mapping);

        } catch (MissingResourceException mrex) {
            throw new FBResourceException(
            		"Cannot load TPB mapping." + mrex.getMessage());
        }
    }

    /**
     * This method extracts TPB mapping information from the connection
     * parameters and adds it to the connectionProperties. Two formats are supported:
     * <ul>
     * <li><code>info</code> contains <code>"tpb_mapping"</code> parameter
     * pointing to a resource bundle with mapping information;
     * <li><code>info</code> contains separate mappings for each of following
     * transaction isolation levels: <code>"TRANSACTION_SERIALIZABLE"</code>,
     * <code>"TRANSACTION_REPEATABLE_READ"</code> and
     * <code>"TRANSACTION_READ_COMMITTED"</code>.
     * </ul>
     *
     * @param gds GDS object
     * @param connectionProperties FirebirdConnectionProperties to set transaction state
     * @param info connection parameters passed into a driver.
     *
     * @throws FBResourceException if specified mapping is incorrect.
     */
    public static void processMapping(GDS gds, FirebirdConnectionProperties connectionProperties, Properties info)
            throws FBResourceException {

        if (info.containsKey(TRANSACTION_SERIALIZABLE))
            connectionProperties.setTransactionParameters(
                    Connection.TRANSACTION_SERIALIZABLE,
                    processMapping(gds, info.getProperty(TRANSACTION_SERIALIZABLE)));

        if (info.containsKey(TRANSACTION_REPEATABLE_READ))
            connectionProperties.setTransactionParameters(
                    Connection.TRANSACTION_REPEATABLE_READ,
                    processMapping(gds, info.getProperty(TRANSACTION_REPEATABLE_READ)));

        if (info.containsKey(TRANSACTION_READ_COMMITTED))
            connectionProperties.setTransactionParameters(
                    Connection.TRANSACTION_READ_COMMITTED,
                    processMapping(gds, info.getProperty(TRANSACTION_READ_COMMITTED)));
    }

    /**
     * Process comma-separated list of keywords and convert them into TPB
     * values.
     * 
     * @param mapping comma-separated list of keywords.
     * 
     * @return set containing values corresponding to the specified keywords.
     * 
     * @throws FBResourceException if mapping contains keyword that is not
     * a TPB parameter.
     */
    public static TransactionParameterBuffer processMapping(GDS gds, String mapping) throws FBResourceException {
        TransactionParameterBuffer result = gds.newTransactionParameterBuffer();

        StringTokenizer st = new StringTokenizer(mapping, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            Integer value = ParameterBufferHelper.getTpbParam(token);
            if (value == null)
                throw new FBResourceException(
                		"Keyword " + token + " unknown. Please check your mapping.");

            result.addArgument(value);
        }

        return result;
    }

    /**
     * Get mapping for the specified transaction isolation level.
     * 
     * @param transactionIsolation transaction isolation level.
     * 
     * @return set with TPB parameters.
     * 
     * @throws IllegalArgumentException if specified transaction isolation level
     * is unknown.
     */
    public TransactionParameterBuffer getMapping(int transactionIsolation) {

        switch (transactionIsolation) {

        case Connection.TRANSACTION_SERIALIZABLE:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_READ_COMMITTED:
            return mapping.get(transactionIsolation).deepCopy();

            // promote transaction
        case Connection.TRANSACTION_READ_UNCOMMITTED:
            return mapping.get(Connection.TRANSACTION_READ_COMMITTED).deepCopy();

        case Connection.TRANSACTION_NONE:
        default:
            throw new IllegalArgumentException(
            		"Transaction isolation level " + transactionIsolation +
            		" is not supported.");
        }
    }

    /**
     * Set mapping for the specified transaction isolation.
     * 
     * @param transactionIsolation transaction isolation level.
     * @param tpb TPB parameters.
     * 
     * @throws IllegalArgumentException if incorrect isolation level is specified.
     */
    public void setMapping(int transactionIsolation, TransactionParameterBuffer tpb) {
        switch (transactionIsolation) {

        case Connection.TRANSACTION_SERIALIZABLE:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_READ_COMMITTED:
            mapping.put(transactionIsolation, tpb);
            break;

        case Connection.TRANSACTION_READ_UNCOMMITTED:
        case Connection.TRANSACTION_NONE:
        default:
            throw new IllegalArgumentException(
            		"Transaction isolation level " + transactionIsolation +
            		" is not supported.");
        }
    }

    /**
     * Get default mapping. Default mapping represents a TPB mapping for the
     * default transaction isolation level (read committed).
     * 
     * @return mapping for the default transaction isolation level.
     */
    public TransactionParameterBuffer getDefaultMapping() {
        return mapping.get(defaultIsolationLevel);
    }

    public int getDefaultTransactionIsolation() {
        return defaultIsolationLevel;
    }

    public void setDefaultTransactionIsolation(int isolationLevel) {
        this.defaultIsolationLevel = isolationLevel;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FBTpbMapper)) {
            return false;
        }

        FBTpbMapper that = (FBTpbMapper) obj;
        boolean result = this.mapping.equals(that.mapping);
        result &= (this.defaultIsolationLevel == that.defaultIsolationLevel);

        return result;
    }

    public int hashCode() {
        // TODO both these values are mutable, so potentially unstable hashcode
        return ObjectUtils.hash(mapping, defaultIsolationLevel);
    }

    public Object clone() {
        try {
            FBTpbMapper clone = (FBTpbMapper) super.clone();

            clone.mapping = new ConcurrentHashMap<Integer, TransactionParameterBuffer>(mapping);

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new Error("Assertion failure: clone not supported"); // Can't
                                                                       // happen
        }
    }
}
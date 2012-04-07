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
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jca.FBResourceException;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * This class is provides mapping capabilities between standard JDBC
 * transaction isolation level and Firebird Transaction Parameters Block (TPB).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBTpbMapper implements Serializable, Cloneable {

    private static final long serialVersionUID = 1690658870275668176L;

    public static final String DEFAULT_MAPPING_RESOURCE = "isc_tpb_mapping";

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

    private HashMap mapping = new HashMap();
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

        mapping.put(Integer.valueOf(Connection.TRANSACTION_SERIALIZABLE), serializableTpb);
        mapping.put(Integer.valueOf(Connection.TRANSACTION_REPEATABLE_READ), repeatableReadTpb);
        mapping.put(Integer.valueOf(Connection.TRANSACTION_READ_COMMITTED), readCommittedTpb);
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
    public FBTpbMapper(GDS gds, Map stringMapping) throws FBResourceException {
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
    private void processMapping(GDS gds, Map stringMapping) throws FBResourceException {

        Iterator iter = stringMapping.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();

            String jdbcTxIsolation = (String) entry.getKey();

            if (TRANSACTION_SERIALIZABLE.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                		Integer.valueOf(Connection.TRANSACTION_SERIALIZABLE),
                        processMapping(gds, (String) entry.getValue()));
            else if (TRANSACTION_REPEATABLE_READ.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                		Integer.valueOf(Connection.TRANSACTION_REPEATABLE_READ),
                        processMapping(gds, (String) entry.getValue()));
            else if (TRANSACTION_READ_COMMITTED.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                		Integer.valueOf(Connection.TRANSACTION_READ_COMMITTED),
                        processMapping(gds, (String) entry.getValue()));
            else if (TRANSACTION_READ_UNCOMMITTED.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                		Integer.valueOf(Connection.TRANSACTION_READ_UNCOMMITTED),
                        processMapping(gds, (String) entry.getValue()));
            else
                throw new FBResourceException(
                		"Transaction isolation " + jdbcTxIsolation +
                		" is not supported.");
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

            HashMap mapping = new HashMap();

            Enumeration en = res.getKeys();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
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
            Integer value = FBConnectionHelper.getTpbParam(token);
            if (value == null)
                throw new FBResourceException(
                		"Keyword " + token + " unknown. Please check your mapping.");

            result.addArgument(value.intValue());
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
     * @throws FBResourceException if specified transaction isolation level
     * is unknown.
     */
    public TransactionParameterBuffer getMapping(int transactionIsolation) {

        switch (transactionIsolation) {

        case Connection.TRANSACTION_SERIALIZABLE:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_READ_COMMITTED:
            return ((TransactionParameterBuffer) mapping.get(
            		Integer.valueOf(transactionIsolation))).deepCopy();

            // promote transaction
        case Connection.TRANSACTION_READ_UNCOMMITTED:
            return ((TransactionParameterBuffer) mapping.get(
            		Integer.valueOf(Connection.TRANSACTION_READ_COMMITTED))).deepCopy();

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
     * @throws FBResourceException if incorrect isolation level is specified.
     */
    public void setMapping(int transactionIsolation, TransactionParameterBuffer tpb) {
        switch (transactionIsolation) {

        case Connection.TRANSACTION_SERIALIZABLE:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_READ_COMMITTED:
            mapping.put(Integer.valueOf(transactionIsolation), tpb);
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
        return (TransactionParameterBuffer) mapping.get(Integer.valueOf(defaultIsolationLevel));
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
        boolean result = true;
        result &= this.mapping.equals(that.mapping);
        result &= (this.defaultIsolationLevel == that.defaultIsolationLevel);

        return result;
    }

    public int hashCode() {
        int result = 31;

        result = result * 83 + mapping.hashCode();
        result = result * 83 + defaultIsolationLevel;
        return result;
    }

    public Object clone() {
        try {
            FBTpbMapper clone = (FBTpbMapper) super.clone();

            clone.mapping = (HashMap) mapping.clone();

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new Error("Assertion failure: clone not supported"); // Can't
                                                                       // happen
        }
    }
}
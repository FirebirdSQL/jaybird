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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.jaybird.props.internal.TransactionNameMapping;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;

/**
 * This class is provides mapping capabilities between standard JDBC
 * transaction isolation level and Firebird Transaction Parameters Block (TPB).
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public final class FBTpbMapper implements Serializable, Cloneable {

    private static final long serialVersionUID = 1690658870275668176L;

    public static FBTpbMapper getDefaultMapper() {
        return new FBTpbMapper();
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
    public static final String TRANSACTION_SERIALIZABLE = TransactionNameMapping.TRANSACTION_SERIALIZABLE;

    /**
     * Dirty reads and non-repeatable reads are prevented; phantom reads can
     * occur. This level prohibits a transaction from reading a row with
     * uncommitted changes in it, and it also prohibits the situation where one
     * transaction reads a row, a second transaction alters the row, and the
     * first transaction rereads the row, getting different values the second
     * time (a "non-repeatable read").
     */
    public static final String TRANSACTION_REPEATABLE_READ = TransactionNameMapping.TRANSACTION_REPEATABLE_READ;

    /**
     * Dirty reads are prevented; non-repeatable reads and phantom reads can
     * occur. This level only prohibits a transaction from reading a row with
     * uncommitted changes in it.
     */
    public static final String TRANSACTION_READ_COMMITTED = TransactionNameMapping.TRANSACTION_READ_COMMITTED;

    private static final List<String> ISOLATION_LEVEL_NAMES = Collections.unmodifiableList(Arrays.asList(
            TRANSACTION_SERIALIZABLE, TRANSACTION_REPEATABLE_READ, TRANSACTION_READ_COMMITTED));

    // read uncommitted actually not supported
    /**
     * Dirty reads, non-repeatable reads and phantom reads can occur. This level
     * allows a row changed by one transaction to be read by another transaction
     * before any changes in that row have been committed (a "dirty read"). If
     * any of the changes are rolled back, the second transaction will have
     * retrieved an invalid row. <b>This level is not actually supported </b>
     */
    public static final String TRANSACTION_READ_UNCOMMITTED = TransactionNameMapping.TRANSACTION_READ_UNCOMMITTED;

    /**
     * Indicates that transactions are not supported. <b>This level is not
     * supported </b>
     */
    public static final String TRANSACTION_NONE = TransactionNameMapping.TRANSACTION_NONE;

    /**
     * Convert transaction isolation level into string.
     *
     * @param isolationLevel
     *         transaction isolation level as integer constant.
     * @return corresponding string representation.
     */
    public static String getTransactionIsolationName(int isolationLevel) {
        return TransactionNameMapping.toIsolationLevelName(isolationLevel);
    }

    /**
     * Convert transaction isolation level name into a corresponding constant.
     *
     * @param isolationName
     *         name of the transaction isolation.
     * @return corresponding constant.
     */
    public static int getTransactionIsolationLevel(String isolationName) {
        return TransactionNameMapping.toIsolationLevel(isolationName);
    }

    // ConcurrentHashMap because changes can - potentially - be made concurrently
    private Map<Integer, TransactionParameterBuffer> mapping = new ConcurrentHashMap<>();
    private int defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;

    /**
     * Create instance of this class with the default mapping of JDBC
     * transaction isolation levels to Firebird TPB.
     */
    public FBTpbMapper() {
        // TODO instance creation should be delegated to FbDatabase or another factory
        // TODO Should use isc_tpb_mapping.properties

        TransactionParameterBuffer serializableTpb = new TransactionParameterBufferImpl();
        serializableTpb.addArgument(isc_tpb_write);
        serializableTpb.addArgument(isc_tpb_wait);
        serializableTpb.addArgument(isc_tpb_consistency);

        TransactionParameterBuffer repeatableReadTpb = new TransactionParameterBufferImpl();
        repeatableReadTpb.addArgument(isc_tpb_write);
        repeatableReadTpb.addArgument(isc_tpb_wait);
        repeatableReadTpb.addArgument(isc_tpb_concurrency);

        TransactionParameterBuffer readCommittedTpb = new TransactionParameterBufferImpl();
        readCommittedTpb.addArgument(isc_tpb_write);
        readCommittedTpb.addArgument(isc_tpb_wait);
        readCommittedTpb.addArgument(isc_tpb_read_committed);
        readCommittedTpb.addArgument(isc_tpb_rec_version);

        mapping.put(Connection.TRANSACTION_SERIALIZABLE, serializableTpb);
        mapping.put(Connection.TRANSACTION_REPEATABLE_READ, repeatableReadTpb);
        mapping.put(Connection.TRANSACTION_READ_COMMITTED, readCommittedTpb);
    }

    /**
     * Create instance of this class for the specified string mapping.
     *
     * @param stringMapping mapping of JDBC transaction isolation to Firebird
     * mapping. Keys and values of this map must be strings. Keys can have
     * the following values:
     * <ul>
     * <li>{@code "TRANSACTION_SERIALIZABLE"}
     * <li>{@code "TRANSACTION_REPEATABLE_READ"}
     * <li>{@code "TRANSACTION_READ_COMMITTED"}
     * <li>{@code "TRANSACTION_READ_UNCOMMITTED"}
     * </ul>
     * Values are specified as comma-separated list of following keywords:
     * <ul>
     * <li>{@code "isc_tpb_consistency"}
     * <li>{@code "isc_tpb_concurrency"}
     * <li>{@code "isc_tpb_read_committed"}
     * <li>{@code "isc_tpb_rec_version"}
     * <li>{@code "isc_tpb_no_rec_version"}
     * <li>{@code "isc_tpb_wait"}
     * <li>{@code "isc_tpb_nowait"}
     * <li>{@code "isc_tpb_read"}
     * <li>{@code "isc_tpb_write"}
     * <li>{@code "isc_tpb_lock_read"}
     * <li>{@code "isc_tpb_lock_write"}
     * <li>{@code "isc_tpb_shared"}
     * <li>{@code "isc_tpb_protected"}
     * </ul>
     * It is also allowed to strip "isc_tpb_" prefix from above shown constants.
     * Meaning of these constants and possible combinations you can find in a
     * documentation.
     *
     * @throws SQLException if mapping contains incorrect values.
     */
    public FBTpbMapper(Map<String, String> stringMapping) throws SQLException {
        this();
        processMapping(stringMapping);
    }

    /**
     * Process specified string mapping. This method updates default mapping with values specified in
     * a {@code stringMapping}.
     *
     * @param stringMapping
     *         mapping to process.
     * @throws SQLException
     *         if mapping contains incorrect values.
     */
    private void processMapping(Map<String, String> stringMapping) throws SQLException {
        for (Map.Entry<String, String> entry : stringMapping.entrySet()) {
            String jdbcTxIsolation = entry.getKey();
            int isolationLevel;
            try {
                isolationLevel = getTransactionIsolationLevel(jdbcTxIsolation);
            } catch (IllegalArgumentException ex) {
                // TODO More specific exception, Jaybird error code
                throw new SQLException("Transaction isolation " + jdbcTxIsolation + " is not supported.");
            }
            TransactionParameterBuffer tpb = processMapping(entry.getValue());
            mapping.put(isolationLevel, tpb);
        }
    }

    /**
     * Create instance of this class and load mapping from the specified resource.
     *
     * @param mappingResource
     *         name of the resource to load.
     * @param cl
     *         class loader that should be used to load specified resource.
     * @throws SQLException
     *         if resource cannot be loaded or contains incorrect values.
     */
    public FBTpbMapper(String mappingResource, ClassLoader cl) throws SQLException {
        // TODO The documentation of DatabaseConnectionProperties.setTpbMapping suggests more functionality than
        //  actually available
        // Make sure the documented 'res:' protocol works
        if (mappingResource.startsWith("res:")) {
            mappingResource = mappingResource.substring(4);
        }
        try {
            ResourceBundle res = ResourceBundle.getBundle(mappingResource, Locale.getDefault(), cl);

            Map<String, String> mapping = new HashMap<>();

            Enumeration<String> en = res.getKeys();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                String value = res.getString(key);
                mapping.put(key, value);
            }

            processMapping(mapping);

        } catch (MissingResourceException mrex) {
            // TODO More specific exception, Jaybird error code
            throw new SQLException("Cannot load TPB mapping. " + mrex.getMessage(), mrex);
        }
    }

    /**
     * This method extracts TPB mapping information from the connection parameters and adds it to the
     * connectionProperties. The following format is supported:
     * <p>
     * {@code info} contains separate mappings for each of following transaction isolation levels:
     * {@code "TRANSACTION_SERIALIZABLE"}, {@code "TRANSACTION_REPEATABLE_READ"} and
     * {@code "TRANSACTION_READ_COMMITTED"}.
     * </p>
     *
     * @param connectionProperties
     *         FirebirdConnectionProperties to set transaction state
     * @param info
     *         connection parameters passed into a driver.
     * @throws SQLException
     *         if specified mapping is incorrect.
     * @see #processMapping(FirebirdConnectionProperties, Map)
     */
    public static void processMapping(FirebirdConnectionProperties connectionProperties, Properties info)
            throws SQLException {
        for (String isolationName : ISOLATION_LEVEL_NAMES) {
            String property = info.getProperty(isolationName);
            if (property == null) continue;
            connectionProperties.setTransactionParameters(
                    getTransactionIsolationLevel(isolationName),
                    processMapping(property));
        }
    }

    /**
     * This method extracts TPB mapping information from the connection parameters and adds it to the
     * connectionProperties. The following format is supported:
     * <p>
     * {@code info} contains separate mappings for each of following transaction isolation levels:
     * {@code "TRANSACTION_SERIALIZABLE"}, {@code "TRANSACTION_REPEATABLE_READ"} and
     * {@code "TRANSACTION_READ_COMMITTED"}.
     * </p>
     *
     * @param connectionProperties
     *         FirebirdConnectionProperties to set transaction state
     * @param info
     *         connection parameters passed into a driver.
     * @throws SQLException
     *         if specified mapping is incorrect.
     * @see #processMapping(FirebirdConnectionProperties, Properties)
     */
    public static void processMapping(FirebirdConnectionProperties connectionProperties, Map<String, String> info)
            throws SQLException {
        for (String isolationName : ISOLATION_LEVEL_NAMES) {
            String property = info.get(isolationName);
            if (property == null) continue;
            connectionProperties.setTransactionParameters(
                    getTransactionIsolationLevel(isolationName),
                    processMapping(property));
        }
    }

    /**
     * Process comma-separated list of keywords and convert them into TPB
     * values.
     *
     * @param mapping
     *         comma-separated list of keywords.
     * @return set containing values corresponding to the specified keywords.
     * @throws SQLException
     *         if mapping contains keyword that is not a TPB parameter.
     */
    public static TransactionParameterBuffer processMapping(String mapping) throws SQLException {
        // TODO instance creation should be delegated to FbDatabase
        TransactionParameterBuffer result = new TransactionParameterBufferImpl();

        StringTokenizer st = new StringTokenizer(mapping, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            Integer argValue = null;
            if (token.contains("=")) {
                String[] parts = token.split("=");
                try {
                    argValue = Integer.valueOf(parts[1]);
                } catch (NumberFormatException ex) {
                    // TODO More specific exception, Jaybird error code
                    throw new SQLException(parts[1] + " is not valid integer value");
                }
                token = parts[0];
            }
            Integer value = TpbMapping.getTpbParam(token);
            if (value == null) {
                // TODO More specific exception, Jaybird error code
                throw new SQLException("Keyword " + token + " unknown. Please check your mapping.");
            }

            if (argValue == null) {
                result.addArgument(value);
            } else {
                result.addArgument(value, argValue);
            }
        }

        return result;
    }

    /**
     * Get mapping for the specified transaction isolation level.
     *
     * @param transactionIsolation
     *         transaction isolation level.
     * @return set with TPB parameters.
     * @throws IllegalArgumentException
     *         if specified transaction isolation level is unknown.
     */
    public TransactionParameterBuffer getMapping(int transactionIsolation) {
        switch (transactionIsolation) {
        case Connection.TRANSACTION_SERIALIZABLE:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_READ_COMMITTED:
            return mapping.get(transactionIsolation).deepCopy();

        case Connection.TRANSACTION_READ_UNCOMMITTED:
            // promote transaction
            return mapping.get(Connection.TRANSACTION_READ_COMMITTED).deepCopy();

        case Connection.TRANSACTION_NONE:
        default:
            // TODO Throw SQLException instead?
            throw new IllegalArgumentException(
                    "Transaction isolation level " + transactionIsolation + " is not supported.");
        }
    }

    /**
     * Set mapping for the specified transaction isolation.
     *
     * @param transactionIsolation
     *         transaction isolation level.
     * @param tpb
     *         TPB parameters.
     * @throws IllegalArgumentException
     *         if incorrect isolation level is specified.
     */
    public void setMapping(int transactionIsolation, TransactionParameterBuffer tpb) {
        switch (transactionIsolation) {
        case Connection.TRANSACTION_SERIALIZABLE:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_READ_COMMITTED:
            mapping.put(transactionIsolation, tpb.deepCopy());
            break;

        case Connection.TRANSACTION_READ_UNCOMMITTED:
        case Connection.TRANSACTION_NONE:
        default:
            // TODO Throw SQLException instead?
            throw new IllegalArgumentException(
                    "Transaction isolation level " + transactionIsolation + " is not supported.");
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

    int getDefaultTransactionIsolation() {
        return defaultIsolationLevel;
    }

    void setDefaultTransactionIsolation(int isolationLevel) {
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
        return Objects.hash(mapping, defaultIsolationLevel);
    }

    public Object clone() {
        try {
            FBTpbMapper clone = (FBTpbMapper) super.clone();

            ConcurrentHashMap<Integer, TransactionParameterBuffer> newMapping = new ConcurrentHashMap<>();
            for (Map.Entry<Integer, TransactionParameterBuffer> entry : mapping.entrySet()) {
                newMapping.put(entry.getKey(), entry.getValue().deepCopy());
            }

            clone.mapping = newMapping;

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new Error("Assertion failure: clone not supported"); // Can't happen
        }
    }

    private static final class TpbMapping {

        private static final String TPB_PREFIX = "isc_tpb_";

        private static final Map<String, Integer> tpbTypes;

        // Initialize mappings between TPB constant names and their values; should be executed only once.
        static {
            final Map<String, Integer> tempTpbTypes = new HashMap<>(64);

            final Field[] fields = TpbItems.class.getFields();

            for (Field field : fields) {
                final String name = field.getName();
                if (!(name.startsWith(TPB_PREFIX) && field.getType().equals(int.class))) {
                    continue;
                }

                final Integer value;
                try {
                    value = field.getInt(null);
                } catch (IllegalAccessException iaex) {
                    continue;
                }

                // put the correct parameter name
                tempTpbTypes.put(name.substring(TPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tempTpbTypes.put(name, value);
            }

            tpbTypes = Collections.unmodifiableMap(tempTpbTypes);
        }

        /**
         * Get value of TPB parameter for the specified name. This method tries to match string representation of
         * the TPB parameter with its value.
         *
         * @param name
         *         string representation of TPB parameter, can have "isc_tpb_" prefix.
         * @return value corresponding to the specified parameter name or {@code null} if nothing was found.
         */
        private static Integer getTpbParam(String name) {
            return tpbTypes.get(name);
        }
    }
}
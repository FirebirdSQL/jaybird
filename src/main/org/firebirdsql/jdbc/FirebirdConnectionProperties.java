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
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;

/**
 * Connection properties for the Firebird connection. Main part of this
 * interface corresponds to the Database Parameter Buffer, but also contains
 * properties to specify default transaction parameters.
 */
public interface FirebirdConnectionProperties extends DatabaseConnectionProperties {

    /**
     * @return path to the database including the server name and the port, if needed.
     * @deprecated Use {@link #getDatabaseName()}; will be removed in Jaybird 6 or later
     */
    @Deprecated
    default String getDatabase() {
        return getDatabaseName();
    }

    /**
     * @param database
     *         path to the database including the server name and the port, if needed.
     * @deprecated Use {@link #setDatabaseName(String)}; will be removed in Jaybird 6 or later
     */
    @Deprecated
    default void setDatabase(String database) {
        setDatabaseName(database);
    }

    /**
     * @return name of the user that will be used when connecting to the database.
     * @deprecated Use {@link #getUser()} instead; will be retained indefinitely for compatibility
     */
    @Deprecated
    default String getUserName() {
        return getUser();
    }

    /**
     * @param userName
     *         name of the user that will be used when connecting to the database.
     * @deprecated Use {@link #setUser(String)}; will be retained indefinitely for compatibility
     */
    @Deprecated
    default void setUserName(String userName) {
        setUser(userName);
    }

    /**
     * @return number of cache buffers that should be allocated for this
     * connection, should be specified for ClassicServer instances,
     * SuperServer has a server-wide configuration parameter.
     * @deprecated Use {@link #getPageCacheSize()}; will be removed in Jaybird 6
     */
    @Deprecated
    default int getBuffersNumber() {
        return getPageCacheSize();
    }

    /**
     * @param buffersNumber
     *         number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     * @deprecated Use {@link #setPageCacheSize(int)}; will be removed in Jaybird 6
     */
    @Deprecated
    default void setBuffersNumber(int buffersNumber) {
        setPageCacheSize(buffersNumber);
    }

    /**
     * Get the property that does not have corresponding getter method by its
     * name.
     *
     * @param key
     *         name of the property to get.
     * @return value of the property.
     * @deprecated Use {@link #getProperty(String)}; will be removed in Jaybird 6
     */
    @Deprecated
    default String getNonStandardProperty(String key) {
        return getProperty(key);
    }

    /**
     * Set the property that does not have corresponding setter method.
     *
     * @param key
     *         name of the property to set.
     * @param value
     *         value of the property.
     * @deprecated Use {@link #setProperty(String, String)}; will be removed in Jaybird 6
     */
    @Deprecated
    default void setNonStandardProperty(String key, String value) {
        setProperty(key, value);
    }

    /**
     * Set the property that does not have corresponding setter method.
     *
     * @param propertyMapping
     *         parameter value in the {@code propertyName[=propertyValue]} form, this allows setting non-standard
     *         parameters using configuration files.
     */
    void setNonStandardProperty(String propertyMapping);

    /**
     * Get the transaction parameter buffer corresponding to the current
     * connection request information.
     *
     * @param isolation
     *         transaction isolation level for which TPB should be returned.
     * @return instance of {@link TransactionParameterBuffer}.
     */
    TransactionParameterBuffer getTransactionParameters(int isolation);

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * The specified TPB is used as a default mapping for the specified
     * isolation level.
     *
     * @param isolation
     *         transaction isolation level.
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} containing
     *         transaction parameters.
     */
    void setTransactionParameters(int isolation, TransactionParameterBuffer tpb);

}

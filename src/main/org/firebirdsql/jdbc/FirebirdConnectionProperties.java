// SPDX-FileCopyrightText: Copyright 2005-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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
     * @return name of the user that will be used when connecting to the database.
     * @deprecated Use {@link #getUser()} instead; will be retained indefinitely for compatibility
     */
    @Deprecated(since = "5")
    default String getUserName() {
        return getUser();
    }

    /**
     * @param userName
     *         name of the user that will be used when connecting to the database.
     * @deprecated Use {@link #setUser(String)}; will be retained indefinitely for compatibility
     */
    @Deprecated(since = "5")
    default void setUserName(String userName) {
        setUser(userName);
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

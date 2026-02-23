// SPDX-FileCopyrightText: Copyright 2005-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.jspecify.annotations.Nullable;

/**
 * Connection properties for a Firebird connection.
 */
public interface FirebirdConnectionProperties extends DatabaseConnectionProperties {

    /**
     * @return name of the user that will be used when connecting to the database.
     * @deprecated Use {@link #getUser()} instead; will be retained indefinitely for compatibility
     */
    @Deprecated(since = "5")
    default @Nullable String getUserName() {
        return getUser();
    }

    /**
     * @param userName
     *         name of the user that will be used when connecting to the database.
     * @deprecated Use {@link #setUser(String)}; will be retained indefinitely for compatibility
     */
    @Deprecated(since = "5")
    default void setUserName(@Nullable String userName) {
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
     * Get the transaction parameter buffer corresponding to the current connection request information.
     * <p>
     * Implementations may return {@code null} or throw an {@link IllegalArgumentException} for invalid values of
     * {@code isolation}. They may also return {@code null} for valid isolation levels before first use of the
     * transaction configuration when that isolation level was not explicitly configured. Future versions of Jaybird may
     * handle this differently with this method becoming {@code @NonNull}.
     * </p>
     *
     * @param isolation
     *         transaction isolation level for which TPB should be returned
     * @return instance of {@link TransactionParameterBuffer}
     * @throws IllegalArgumentException
     *         (optional) if {@code isolation} is not defined by JDBC; implementations may also return {@code null}
     *         before first use of the transaction configuration (this may change in the future, see above)
     */
    @Nullable TransactionParameterBuffer getTransactionParameters(int isolation);

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * <p>
     * The specified TPB is used as the default mapping for the specified isolation level.
     * </p>
     *
     * @param isolation
     *         transaction isolation level
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} containing transaction parameters (never {@code null})
     * @throws IllegalArgumentException
     *         (optional) if {@code isolation} is not defined by JDBC, though this may result in exceptions at a later
     *         time; behaviour may change after first use of transaction configuration (post-configuration phase) (this
     *         may change in the future, see also {@link #getTransactionParameters(int)})
     * @see #getTransactionParameters(int)
     */
    void setTransactionParameters(int isolation, TransactionParameterBuffer tpb);

}

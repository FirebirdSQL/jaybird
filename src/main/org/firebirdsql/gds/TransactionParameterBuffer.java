// SPDX-FileCopyrightText: Copyright 2005-2007 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

import static org.firebirdsql.jaybird.fb.constants.TpbItems.isc_tpb_autocommit;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.isc_tpb_read;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.isc_tpb_write;

/**
 * Instances of this interface represent Transaction Parameter Buffer from the Firebird API.
 */
public interface TransactionParameterBuffer extends ParameterBuffer {

    /**
     * Make a deep copy of this object.
     *
     * @return deep copy of this object.
     */
    TransactionParameterBuffer deepCopy();

    /**
     * Copies the contents of this transaction parameter buffer to {@code destination}.
     *
     * @param destination destination transaction parameter buffer
     * @since 6
     */
    default void copyTo(TransactionParameterBuffer destination) {
        for (Parameter parameter : this) {
            parameter.copyTo(destination, null);
        }
    }

    /**
     * Set the read-only flag ({@code isc_tpb_read}) or read/write flag ({@code isc_tpb_write}) on this TPB.
     *
     * @param readOnly
     *         if {@code true}, this TPB will be set to read-only, otherwise it will be read/write
     * @since 6
     */
    default void setReadOnly(boolean readOnly) {
        if (readOnly) {
            ensurePresentAbsent(isc_tpb_read, isc_tpb_write);
        } else {
            ensurePresentAbsent(isc_tpb_write, isc_tpb_read);
        }
    }

    private void ensurePresentAbsent(int present, int absent) {
        if (!hasArgument(present)) {
            addArgument(present);
        }
        removeArgument(absent);
    }

    /**
     * Determine whether this TPB is set to read-only.
     *
     * @return {@code true} if this TPB is read-only, otherwise {@code false}
     * @since 6
     */
    default boolean isReadOnly() {
        return hasArgument(isc_tpb_read);
    }

    /**
     * Sets the Firebird auto-commit flag on this TPB.
     * <p>
     * This shouldn't be confused with the normal JDBC auto-commit behavior. Effectively, setting this to {@code true}
     * will result in Firebird using commit retain after each executed statement.
     * </p>
     *
     * @param autoCommit
     *         {@code true} add the auto-commit flag, otherwise remove it
     * @see #isAutoCommit()
     * @since 6
     */
    default void setAutoCommit(boolean autoCommit) {
        if (autoCommit) {
            if (!hasArgument(isc_tpb_autocommit)) {
                addArgument(isc_tpb_autocommit);
            }
        } else {
            removeArgument(isc_tpb_autocommit);
        }
    }

    /**
     * Returns if this TPB has the auto-commit flag set.
     *
     * @return {@code true} if this TPB has the auto-commit flag, {@code false} otherwise
     * @see #setAutoCommit(boolean)
     * @since 6
     */
    default boolean isAutoCommit() {
        return hasArgument(isc_tpb_autocommit);
    }

}

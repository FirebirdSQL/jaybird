// SPDX-FileCopyrightText: Copyright 2013-2014 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Transactions states.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public enum TransactionState {

    ACTIVE {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.of(PREPARING, COMMITTING, ROLLING_BACK);
        }
    },
    PREPARING {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.of(PREPARED);
        }
    },
    PREPARED {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.of(COMMITTING, ROLLING_BACK);
        }
    },
    COMMITTING {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.of(COMMITTED, ROLLING_BACK /* To be able to rollback for errors during commit */);
        }
    },
    COMMITTED {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.noneOf(TransactionState.class);
        }
    },
    ROLLING_BACK {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.of(ROLLED_BACK);
        }
    },
    ROLLED_BACK {
        @Override
        Set<TransactionState> createValidTransitionSet() {
            return EnumSet.noneOf(TransactionState.class);
        }
    };

    private Set<TransactionState> validTransitions;

    /**
     * Is the transition to <code>toState</code> valid from this state.
     *
     * @param toState The next state
     * @return <code>true</code> transition is valid
     */
    public final boolean isValidTransition(TransactionState toState) {
        return validTransitionSet().contains(toState);
    }

    /**
     * @return Set of valid transitions from this state
     */
    public final Set<TransactionState> validTransitionSet() {
        if (validTransitions == null) {
            validTransitions = Collections.unmodifiableSet(createValidTransitionSet());
        }
        return validTransitions;
    }

    /**
     * Create the set of valid transitions.
     *
     * @return Set of valid transitions from this state
     */
    abstract Set<TransactionState> createValidTransitionSet();
}

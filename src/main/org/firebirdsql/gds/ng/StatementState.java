// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Statement states for {@link FbStatement} implementations
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public enum StatementState {
    /**
     * Statement is new and no statement handle has been allocated on the server.
     */
    NEW {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, ALLOCATED, CLOSING, NEW);
        }
    },
    /**
     * A statement is being closed, this is an ephemeral state that should only last as long as releasing resources on the
     * database takes.
     */
    CLOSING {
        @Override
        Set<StatementState> createValidTransitionSet() {
            return EnumSet.of(CLOSED, ERROR);
        }
    },
    /**
     * Statement is closed or has been de-allocated.
     */
    CLOSED {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(CLOSED);
        }
    },
    /**
     * Statement has been allocated or unprepared.
     */
    ALLOCATED {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, PREPARING, ALLOCATED, CLOSING);
        }
    },
    /**
     * Statement is preparing a new statement text, this is an ephemeral state that should only last as long as
     * the <em>prepare</em> call to the database takes.
     */
    PREPARING {
        @Override
        Set<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, PREPARING, PREPARED, ALLOCATED, CLOSING);
        }
    },
    /**
     * Statement has been prepared.
     */
    PREPARED {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, EXECUTING, CLOSING, PREPARING, PREPARED, ALLOCATED);
        }
    },
    /**
     * A statement is being executed, this is an ephemeral state that should only last as long as the <em>execute</em>
     * call to the database takes.
     */
    EXECUTING {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, CURSOR_OPEN, PREPARING, PREPARED, CLOSING);
        }
    },
    /**
     * Statement has been executed, cursor is still open.
     */
    CURSOR_OPEN {
        @Override
        public boolean isCursorOpen() {
            return true;
        }

        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, PREPARING, PREPARED, ALLOCATED, CLOSING);
        }
    },
    /**
     * Last statement execute or prepare resulted in an error.
     */
    ERROR {
        /**
         * {@inheritDoc}
         * <p>
         * When in error state, a cursor might be open (or not), as we don't know how we
         * transitioned into this state.
         * </p>
         */
        @Override
        public boolean isCursorOpen() {
            return true;
        }

        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, CLOSING);
        }
    };

    private @Nullable Set<StatementState> validTransitions;

    /**
     * Can a cursor be open in the current state?
     *
     * @return <code>true</code> a cursor can be open in this state
     */
    public boolean isCursorOpen() {
        return false;
    }

    /**
     * Is the transition to <code>toState</code> valid from this state.
     *
     * @param toState
     *         The next state
     * @return <code>true</code> transition is valid
     */
    public final boolean isValidTransition(StatementState toState) {
        return validTransitionSet().contains(toState);
    }

    /**
     * @return Set of valid transitions from this state
     */
    public final Set<StatementState> validTransitionSet() {
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
    abstract Set<StatementState> createValidTransitionSet();
}

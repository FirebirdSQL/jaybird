/*
 * $Id$
 *
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Statement states for {@link FbStatement} implementations
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
     * Statement is closed or has been de-allocated
     */
    CLOSED {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(CLOSED);
        }
    },
    /**
     * Statement has been allocated or unprepared
     */
    ALLOCATED {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, PREPARED, ALLOCATED, CLOSING);
        }
    },
    /**
     * Statement has been prepared
     */
    PREPARED {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, EXECUTING, CLOSING, PREPARED, ALLOCATED);
        }
    },
    /**
     * A statement is being executed, this is an ephemeral state that should only last as long as the execute call to the database takes.
     */
    EXECUTING {
        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, CURSOR_OPEN, PREPARED, CLOSING);
        }
    },
    /**
     * Statement has been executed, cursor is still open
     */
    CURSOR_OPEN {
        @Override
        public boolean isCursorOpen() {
            return true;
        }

        @Override
        EnumSet<StatementState> createValidTransitionSet() {
            return EnumSet.of(ERROR, PREPARED, ALLOCATED, CLOSING);
        }
    },
    /**
     * Last statement execute or prepare resulted in an error
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

    private Set<StatementState> validTransitions;

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

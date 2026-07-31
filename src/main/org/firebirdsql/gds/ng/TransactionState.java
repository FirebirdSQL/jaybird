/*
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
 * Transactions states.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
    },
    /**
     * State used when a transaction was aborted with an unknown result. For example, failure to commit, rollback, or
     * prepare due to broken connections (IO errors, or the connection already being closed).
     * <p>
     * In most cases, the transaction is probably rolled back, but we're not sure. For example, the commit could have
     * been received and processed by the server, but a subsequent connection failure occurred before the response was
     * received. Similar for a transaction prepare (first step of two-phase commit).
     * </p>
     * <p>
     * There are no valid transitions to and from this state; it can only be forcibly set.
     * </p>
     *
     * @since 5.0.13
     */
    ABORTED_UNKNOWN {
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

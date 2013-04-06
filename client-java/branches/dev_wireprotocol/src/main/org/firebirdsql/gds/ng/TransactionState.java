/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

import java.util.EnumSet;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public enum TransactionState {
    NO_TRANSACTION {
        @Override
        public boolean isValidTransition(TransactionState toState) {
            return EnumSet.of(ACTIVE, NO_TRANSACTION).contains(toState);
        }
    },
    ACTIVE {
        @Override
        public boolean isValidTransition(TransactionState toState) {
            // TODO Verify if these are the supported transitions
            return EnumSet.of(ACTIVE, PREPARED, NO_TRANSACTION).contains(toState);
        }
    },
    PREPARED {
        @Override
        public boolean isValidTransition(TransactionState toState) {
            // TODO Verify if these are the supported transitions
            return EnumSet.of(ACTIVE, PREPARED, NO_TRANSACTION).contains(toState);
        }
    };
    
    public abstract boolean isValidTransition(TransactionState toState);
}

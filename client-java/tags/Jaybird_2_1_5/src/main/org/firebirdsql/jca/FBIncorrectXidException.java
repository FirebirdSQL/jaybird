/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jca;

/**
 * This error is thrown when message read from the RDB$TRANSACTIONS table does
 * not represent a serialized Xid.
 */
public class FBIncorrectXidException extends FBResourceException {

    public FBIncorrectXidException(Exception original) {
        super(original);
    }

    public FBIncorrectXidException(String reason, Exception original) {
        super(reason, original);
    }

    public FBIncorrectXidException(String reason, String errorCode) {
        super(reason, errorCode);
    }

    public FBIncorrectXidException(String reason) {
        super(reason);
    }

}

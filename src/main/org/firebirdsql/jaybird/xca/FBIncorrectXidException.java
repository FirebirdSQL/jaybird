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
package org.firebirdsql.jaybird.xca;

import java.io.Serial;

/**
 * This error is thrown when message read from the RDB$TRANSACTIONS table does not represent a serialized Xid.
 */
public class FBIncorrectXidException extends Exception {

    @Serial
    private static final long serialVersionUID = -4422195562607053359L;

    public FBIncorrectXidException(String reason) {
        super(reason);
    }

    public FBIncorrectXidException(String reason, Throwable cause) {
        super(reason, cause);
    }

}

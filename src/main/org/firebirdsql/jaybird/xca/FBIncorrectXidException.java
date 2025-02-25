// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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

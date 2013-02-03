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
 * Exception represents transaction error in resource.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBResourceTransactionException extends FBResourceException {
    
    public static final String SQL_STATE_INVALID_TRANSACTION_STATE = "25S01";
    public static final String SQL_STATE_TRANSACTION_ACTIVE = "25S02";
    public static final String SQL_STATE_TRANSACTION_ROLLED_BACK = "25S03";

    /**
     * Create a new insance of <code>FBResourceTransactionException</code>
     * with a given message and generic error code
     *
     * @param reason The string message for this exception
     */
    public FBResourceTransactionException(String reason) {
        super(reason);
    }

    /**
     * Create a new instance of <code>FBResourceTransactionException</code>
     * with a given message and error code.
     *
     * @param reason The string message for this exception
     * @param errorCode The error code for this exception
     */
    public FBResourceTransactionException(String reason, String errorCode) {
        super(reason, errorCode);
    }
    
    /**
     * Create a new instance of <code>FBResourceTransactionException</code>
     * with a given message and sub-exception.
     *
     * @param reason The string message for this exception
     * @param cause The underlying exception
     */
    public FBResourceTransactionException(String reason, Exception cause) {
        super(reason, cause);
    }
    
    /**
     * Create a new instance of <code>FBResourceException</code>
     * with a given message, error code and underlying exception.
     *
     * @param reason The string message for this exception
     * @param errorCode The error code for this exception
     * @param cause The underlying exception
     */
    public FBResourceTransactionException(String reason, String errorCode, Exception cause) {
        super(reason, cause);
        setLinkedException(cause);
    }
    
}

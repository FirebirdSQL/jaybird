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

import javax.transaction.xa.XAException;

/**
 * Convenience exception that adds constructor taking message and error code together.
 *
 * @author Roman Rokytskyy
 */
public class FBXAException extends XAException {

    @Serial
    private static final long serialVersionUID = -1041372401682264104L;

    /**
     * Create a new instance of {@code FBXAException} with a given message.
     *
     * @param msg
     *         string message for this exception
     */
    public FBXAException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of {@code FBXAException} based around a specific error code.
     *
     * @param errorCode
     *         error code for this exception
     */
    public FBXAException(int errorCode) {
        super(errorCode);
    }

    /**
     * Create a new instance of {@code FBXAException} based around a message and specific error code.
     *
     * @param msg
     *         string message for this exception
     * @param errorCode
     *         error code for this exception
     */
    public FBXAException(String msg, int errorCode) {
        this(msg);
        this.errorCode = errorCode;
    }

    private Exception reason;

    /**
     * Create a new instance of {@code FBXAException} wrapped around an underlying exception.
     *
     * @param errorCode
     *         error code for this exception
     * @param reason
     *         underlying exception
     */
    public FBXAException(int errorCode, Exception reason) {
        this(errorCode);
        this.reason = reason;
        initCause(reason);
    }

    /**
     * Create a new instance of {@code FBXAException} based around a message and with an underlying exception.
     *
     * @param msg
     *         string message for this exception
     * @param errorCode
     *         error code for this exception
     * @param reason
     *         underlying exception
     */
    public FBXAException(String msg, int errorCode, Exception reason) {
        this(msg, errorCode);
        this.reason = reason;
        initCause(reason);
    }

    /**
     * Get message of this exception.
     *
     * @return combined message of this exception and original exception.
     */
    public String getMessage() {
        String s = super.getMessage();
        if (reason == null) return s;
        if (s == null) return reason.getMessage();
        return s + "\nReason: " + reason.getMessage();
    }

}

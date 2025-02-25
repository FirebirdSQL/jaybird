// SPDX-FileCopyrightText: Copyright 2003-2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2006 Ludovic Orban
// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
        initCause(reason);
    }

    /**
     * Get message of this exception.
     *
     * @return combined message of this exception and original exception.
     */
    @Override
    public String getMessage() {
        Throwable cause = getCause();
        String s = super.getMessage();
        if (cause == null) return s;
        if (s == null) return cause.getMessage();
        return s + "\nReason: " + cause.getMessage();
    }

}

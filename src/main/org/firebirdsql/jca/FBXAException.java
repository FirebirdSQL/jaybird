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

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.transaction.xa.XAException;

/**
 * Convenience exception that adds constructor taking message and error code
 * together. 
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBXAException extends XAException {

    public FBXAException() {
        super();
    }

    /**
     * Create a new instance of <code>FBXAException</code> with a given 
     * message.
     *
     * @param msg The string message for this exception
     */
    public FBXAException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of <code>FBXAException</code> based around
     * a specific error code.
     *
     * @param errorCode The error code for this exception
     */
    public FBXAException(int errorCode) {
        super(errorCode);
    }
    
    /**
     * Create a new instance of <code>FBXAException</code> based around
     * a message and specific error code.
     *
     * @param msg The string message for this exception
     * @param errorCode The error code for this exception
     */
    public FBXAException(String msg, int errorCode) {
        this(msg);
        
        this.errorCode = errorCode;
    }

    private Exception reason;
    
    /**
     * Create a new instance of <code>FBXAException</code> wrapped around
     * an underlying exception.
     *
     * @param errorCode The error code for this exception
     * @param reason The underlying exception
     */
    public FBXAException(int errorCode, Exception reason) {
        this(errorCode);
        
        this.reason = reason;
    }
    
    /**
     * Get message of this exception.
     * 
     * @return combined message of this exception and original exception.
     */
    public String getMessage() {
        String s = super.getMessage();

        if (reason == null)
            return s;
            
        if (s == null)
            return reason.getMessage();
            
        return s + "\nReason: " + reason.getMessage();
    }


    /** Print a stack trace for this exception to <code>STDERR</code>. */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print a stack trace for this exception with a 
     * given <code>PrintStream</code>.
     *
     * @param s The <code>PrintStream</code> to which the stack trace will
     *        be written
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (reason != null) {
            s.print("at ");
            reason.printStackTrace(s);
        }
    }

    /**
     * Print a stack trace for this exception with a 
     * given <code>PrintWriter</code>.
     *
     * @param s The <code>PrintWriter</code> to which the stack trace will
     *        be written
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (reason != null) {
            s.print("at ");
            reason.printStackTrace(s);
        }
    }

}

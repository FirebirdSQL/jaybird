/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jca;

import javax.resource.ResourceException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.sql.SQLException;

/**
 * <code>FBResourceException</code> should be used in places where 
 * {@link ResourceException} should be thrown according to the interface
 * specification, but we do not want to loose exception that we caught.
 * <p>
 * Example:
 * <pre>
 * try {
 *     // execute some code here
 *     ...
 * } catch(GDSException gdsex) {
 *     throw new FBResourceException(gdsex);
 * }
 * </pre>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@SuppressWarnings("deprecation")
public class FBResourceException extends ResourceException {
    
    public static final String SQL_STATE_GENERAL_ERROR = "HY000";
    
    /**
     * Create a new instance of <code>FBResourceException</code> with a given
     * string message and generic error code.
     *
     * @param reason The string message for the exception
     */
    public FBResourceException(String reason) {
        super(reason, SQL_STATE_GENERAL_ERROR);
    }

    /**
     * Create a new instance of <code>FBResourceException</code> with 
     * a message and specific error code.
     *
     * @param reason The string message for the exception
     * @param errorCode The error code for the cause of the exception
     */
    public FBResourceException(String reason, String errorCode) {
        super(reason, errorCode);
    }

    /**
     * Create a new instance of <code>FBResourceException</code> with a 
     * generic error code that is linked to another (sub) exception.
     *
     * @param reason The string message for the exception
     * @param original The original exception to which this instance is to
     *        be linked to
     */
    public FBResourceException(String reason, Exception original) {
        super(reason, SQL_STATE_GENERAL_ERROR);
        // Preserve setLinkedException for backwards compatibility
        setLinkedException(original);
        initCause(original);
        if (original instanceof SQLException) {
            SQLException origSql = (SQLException) original;
            if (origSql.getSQLState() != null) {
                setErrorCode(origSql.getSQLState());
            }
        }
    }

    /**
     * Create a new instance of <code>FBResourceException</code> with a 
     * generic error code that is linked to another (sub) exception.
     *
     * @param original The original exception to which this instance is
     *        to be linked to
     */
    public FBResourceException(Exception original) {
        this(original.getMessage(), original);
    }
    
    /**
     * Get message of this exception.
     * 
     * @return combined message of this exception and original exception.
     */
    public String getMessage() {
        String message = super.getMessage();
        String causeMessage = null;

        if (getCause() != null) {
            causeMessage = getCause().getMessage();
        } else if (getLinkedException() != null) {
            causeMessage = getLinkedException().getMessage();
        }

        if (causeMessage == null) {
            return message;
        }
            
        if (message == null) {
            return causeMessage;
        }
            
        return message + "\nReason: " + causeMessage;
    }


    /**
     * Print the stack trace of this exception to <code>STDERR</code>
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print the stack trace of this exception to a 
     * given <code>PrintStream</code>
     *
     * @param s The <code>PrintStream</code> to which to write the stack trace
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (getLinkedException() != null) {
            s.print("at ");
            getLinkedException().printStackTrace(s);
        }
    }

    /**
     * Print the stack trace of this exception to a
     * given <code>PrintWriter</code>
     *
     * @param s The <code>PrintWriter</code> to which to write the stack trace
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (getLinkedException() != null) {
            s.print("at ");
            getLinkedException().printStackTrace(s);
        }
    }
}
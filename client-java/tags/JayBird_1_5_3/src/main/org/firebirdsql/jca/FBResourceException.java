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

import javax.resource.ResourceException;
import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * <code>FBResourceException</code> should be used in places where 
 * {@link ResourceException} should be thrown according to the interface
 * specification, but we do not want to loose exception that we cautght.
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
public class FBResourceException extends ResourceException {
    
    public static final String SQL_STATE_GENERAL_ERROR = "HY000";
    
    public FBResourceException(String reason) {
        super(reason, SQL_STATE_GENERAL_ERROR);
    }

    public FBResourceException(String reason, String errorCode) {
        super(reason, errorCode);
    }

    public FBResourceException(String reason, Exception original) {
        super(reason, SQL_STATE_GENERAL_ERROR);
        setLinkedException(original);
    }

    public FBResourceException(Exception original) {
        super(original.getMessage(), SQL_STATE_GENERAL_ERROR);
        setLinkedException(original);
    }
    
    /**
     * Get message of this exception.
     * 
     * @return combined message of this exception and original exception.
     */
    public String getMessage() {
        String s = super.getMessage();

        if (getLinkedException() == null)
            return s;
            
        if (s == null)
            return getLinkedException().getMessage();
            
        return s + "\nReason: " + getLinkedException().getMessage();
    }


    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (getLinkedException() != null) {
            s.print("at ");
            getLinkedException().printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (getLinkedException() != null) {
            s.print("at ");
            getLinkedException().printStackTrace(s);
        }
    }
}
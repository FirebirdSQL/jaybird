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

package org.firebirdsql.jdbc;

import java.io.PrintWriter;
import java.io.PrintStream;
import java.sql.SQLWarning;
import org.firebirdsql.gds.GDSException;

/**
 * This class is a wrapper for {@link GDSException} instance that is 
 * warning.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBSQLWarning extends SQLWarning {
    
    public static final String SQL_STATE_WARNING = "01000";
    
    private GDSException original;
    private String message;
    
    /**
     * Create instance of this class.
     * 
     * @param original instance of {@link GDSException} that is 
     * warning 
     * 
     * @throws IllegalArgumentException if <code>original.isWarning()</code> 
     * returns <code>false</code>).
     */
    public FBSQLWarning(GDSException original) {
        super(original.getMessage(), SQL_STATE_WARNING);
            
        if (!original.isWarning())
            throw new IllegalArgumentException(
                "Only warnings can be wrapped.");
                
        this.original = original;
        this.message = original.getMessage();
    }
    
    /**
     * Create instance of this class for the specified message.
     * 
     * @param message message for this warning.
     */
    public FBSQLWarning(String message) {
        super(message, SQL_STATE_WARNING);
    }
    
    /**
     * Get error code for this warning.
     * 
     * @return error code for this warning.
     */
    public int getErrorCode() {
        if (original != null)
            return original.getIntParam();
        else
            return 0;
    }
    
    public String getSQLState() {
        if (original != null)
            return original.getSQLState();
        else
            return super.getSQLState();
    }

    public Exception getInternalException() {
        return original;
    }

    public String getMessage() {
        return message;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (original != null) {
            s.print("at ");
            original.printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (original != null) {
            s.print("at ");
            original.printStackTrace(s);
        }
    }
    
}
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
        super(original.getMessage(), SQL_STATE_WARNING, original);
            
        if (!original.isWarning())
            throw new IllegalArgumentException("Only warnings can be wrapped.");
                
        this.original = original;
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
     * Create instance of this class for the specified message, sqlState and fbErrorCode
     * @param message Warning message
     * @param sqlState SQL state
     * @param fbErrorCode Firebird error code
     */
    public FBSQLWarning(String message, String sqlState, int fbErrorCode) {
        super(message, sqlState != null ? sqlState : SQL_STATE_WARNING, fbErrorCode);
    }
    
    /**
     * Get error code for this warning.
     * 
     * @return error code for this warning.
     */
    public int getErrorCode() {
        if (original != null)
            return original.getFbErrorCode();
        else
            return super.getErrorCode();
    }
    
    public String getSQLState() {
        if (original != null)
            return original.getSQLState();
        else
            return super.getSQLState();
    }
}
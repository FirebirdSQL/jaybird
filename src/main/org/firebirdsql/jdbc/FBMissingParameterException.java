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



/**
 * Exception notifying developer that not all parameters were set when calling
 * the prepared statement.
 *
 * @deprecated Unused, will be removed in Jaybird 5
 */
@Deprecated
public class FBMissingParameterException extends FBSQLException {

    private boolean[] setParams;
    
    /**
     * @param message
     */
    FBMissingParameterException(String message, boolean[] setParams) {
        super(message);
        
        this.setParams = new boolean[setParams.length];
        System.arraycopy(setParams, 0, this.setParams, 0, setParams.length);
    }
    
    /**
     * Get information about set parameters.
     * 
     * @return array of booleans, each of which corresponds to the parameter
     * in the prepared statement, its value tells whether parameter was set or
     * not.
     */
    public boolean[] getSetParams() {
        return setParams;
    }
}

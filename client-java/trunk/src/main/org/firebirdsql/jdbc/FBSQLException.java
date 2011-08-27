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

import java.sql.SQLException;

import javax.resource.ResourceException;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.jca.FBResourceException;

public class FBSQLException extends SQLException {
    
	private static final long serialVersionUID = 8157410954186424083L;
	
	public static final String SQL_STATE_INVALID_CONN_ATTR = "01S00";
    public static final String SQL_STATE_NO_ROW_AVAIL = "01S06";
    
    public static final String SQL_STATE_GENERAL_ERROR = "HY000";
    public static final String SQL_STATE_INVALID_COLUMN = "HY002";
    public static final String SQL_STATE_INVALID_PARAM_TYPE = "HY105";
    public static final String SQL_STATE_INVALID_ARG_VALUE = "HY009";
    
    public static final String SQL_STATE_WRONG_PARAM_NUM = "07001";
    public static final String SQL_STATE_NO_RESULT_SET = "07005";
    public static final String SQL_STATE_INVALID_CONVERSION = "07006";
    
    public static final String SQL_STATE_INVALID_STATEMENT_ID = "26000";
    
    public static final String SQL_STATE_CONNECTION_CLOSED = "08003";
    public static final String SQL_STATE_CONNECTION_FAILURE = "08006";
    public static final String SQL_STATE_CONNECTION_FAILURE_IN_TX = "08007";
    public static final String SQL_STATE_COMM_LINK_FAILURE = "08S01";
    
    public FBSQLException(Exception ex) {
        super("Exception. " + ex.getMessage(), SQL_STATE_GENERAL_ERROR);
        initCause(ex);
    }
    
    public FBSQLException(GDSException ex) {
        super("GDS Exception. " + ex.getIntParam() + ". " + ex.getMessage(),
        		SQL_STATE_GENERAL_ERROR, ex.getIntParam());
    }

    public FBSQLException(ResourceException ex) {
        super(getResourceMessage(ex), 
                ex.getErrorCode() != null ? ex.getErrorCode() : SQL_STATE_GENERAL_ERROR, getSqlErrorCode(ex));

        // try to unwrap wrapped GDS exception, in this case FBResourceException
        // will never appear on the stack
        if (ex instanceof FBResourceException && ex.getLinkedException() != null) 
            initCause(ex.getLinkedException());
        else 
            initCause(ex);
    }
    
    public FBSQLException(String message) {
        super(message, SQL_STATE_GENERAL_ERROR);
    }

    /**
     * 
     * @param message Exception message
     * @param ex SQLException that should be set as the 'next exception'
     * @deprecated In all most all cases use {@link #FBSQLException(String, String) in combination with {@link #setNextException(SQLException)}.
     */
    public FBSQLException(String message, SQLException ex) {
        super(message, SQL_STATE_GENERAL_ERROR);
        setNextException(ex);
    }

    public FBSQLException(String message, String sqlState) {
        super(message, sqlState);
    }
    
    /**
     * @deprecated use {@link #getCause()} instead. 
     */
    public Exception getInternalException() {
        return (Exception)getCause();
    }

    /**
     * Helper method to create message text for constructor accepting ResourceException ({@link #FBSQLException(ResourceException)})
     * 
     * @param ex ResourceException
     * @return Exception message
     */
    private static String getResourceMessage(ResourceException ex) {
    	Throwable cause = ex;
    	if (ex instanceof FBResourceException && ex.getLinkedException() != null) { 
            cause = ex.getLinkedException();
    	}
    	
    	if (cause instanceof GDSException) {
            return "GDS Exception. "+ ((GDSException)cause).getIntParam() + ". " + ex.getMessage();
    	} else {
            return "Resource Exception. " + ex.getMessage();
    	}
    }
    
    /**
     * Helper method to get the SQL vendor code (or in the case of Firebird: the isc errorcode).
     * 
     * @param ex ResourceException
     * @return isc errorcode, or 0
     */
    private static int getSqlErrorCode(ResourceException ex) {
    	Throwable cause = ex;
    	if (ex instanceof FBResourceException && ex.getLinkedException() != null) {
            cause = ex.getLinkedException();
    	}
    	
    	if (cause instanceof GDSException) {
    		return ((GDSException)cause).getIntParam();
    	} else {
    		return 0;
    	}
    }
}

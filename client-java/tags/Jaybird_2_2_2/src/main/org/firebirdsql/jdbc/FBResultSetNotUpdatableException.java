package org.firebirdsql.jdbc;


/**
 * Exception is thrown when trying to modify the non-updatable result set. 
 */
public class FBResultSetNotUpdatableException extends FBSQLException {

    /**
     * Create default instance of this class.
     */
    public FBResultSetNotUpdatableException() {
        this("Underlying ResultSet is not updatable.");
    }
    
    /**
     * Create instance of this class for the specified message.
     * 
     * @param message message to display.
     */
    public FBResultSetNotUpdatableException(String message) {
        super(message, SQL_STATE_GENERAL_ERROR);
    }

}

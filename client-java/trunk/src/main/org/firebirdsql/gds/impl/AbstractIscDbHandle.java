package org.firebirdsql.gds.impl;

import java.util.Collection;

import org.firebirdsql.gds.IscDbHandle;


/**
 * 
 */
public abstract class AbstractIscDbHandle implements IscDbHandle {

    /**
     * Get the major version number of the database product to which this
     * handle is attached.
     *
     * @return The major product version number
     */
    public abstract int getDatabaseProductMajorVersion();

    /**
     * Get the minor version number of the database product to which this
     * handle is attached.
     *
     * @return The minor product version number
     */
    public abstract int getDatabaseProductMinorVersion();

    /**
     * Get the product name for the database to which this handle is attached.
     *
     * @return The product name of the database
     */
    public abstract String getDatabaseProductName();

    /**
     * Get the product version for the database to which this handle 
     * is attached.
     *
     * @return The product version of the database
     */
    public abstract String getDatabaseProductVersion();

    /**
     * Get the Interbase/Firebird dialect that is being used with this handle.
     *
     * @return The dialect being used
     */
    public abstract int getDialect();

    public abstract int getODSMajorVersion();

    public abstract int getODSMinorVersion();

    /**
     * Get all active transactions for this handle.
     *
     * @return All active transactions
     */
    public abstract Collection getTransactions();

    public abstract String getVersion();

    /**
     * Retrieve whether this handle has active transactions.
     *
     * @return <code>true</code> if this handle has active transactions,
     *         <code>false</code> otherwise
     */
    public abstract boolean hasTransactions();

    /**
     * Retrieve whether this handle is valid.
     *
     * @return <code>true</code> if this handle is valid, 
     *         <code>false</code> otherwise
     */
    public abstract boolean isValid();

    /**
     * Set the Interbase/Firebird dialect to be used with this handle.
     *
     * @param value The dialect to be used
     */
    public abstract void setDialect(int value);

    public abstract void setODSMajorVersion(int value);

    public abstract void setODSMinorVersion(int value);

    public abstract void setVersion(String value);

}

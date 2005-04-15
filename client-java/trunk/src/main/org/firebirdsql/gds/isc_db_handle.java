/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds;

import java.util.Collection;
import java.util.List;
import javax.security.auth.Subject;

/**
 * The interface <code>isc_db_handle</code> represents a socket connection
 * to the database server.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface isc_db_handle {

    /**
     * Retrieve whether this handle is valid.
     *
     * @return <code>true</code> if this handle is valid, 
     *         <code>false</code> otherwise
     */
    boolean isValid();

    /**
     * Get the <code>Subject</code> instance that represents the current
     * user identity for this link to the database.
     *
     * @return The current <code>Subject</code>
     */
    Subject getSubject();

    /**
     * Retrieve whether this handle has active transactions.
     *
     * @return <code>true</code> if this handle has active transactions,
     *         <code>false</code> otherwise
     */
    boolean hasTransactions();
    
    /**
     * Get all active transactions for this handle.
     *
     * @return All active transactions
     */
    Collection getTransactions();


    /**
     * Get list of warnings that were returned by the server.
     * 
     * @return instance of {@link List} containing instances of 
     * {@link GDSException} representing server warnings (method 
     * {@link GDSException#isWarning()} returns <code>true</code>).
     */
    List getWarnings();
    
    /**
     * Clear warning list associated with this connection.
     */
    void clearWarnings();

   
    /**
     * Set the Interbase/Firebird dialect to be used with this handle.
     *
     * @param value The dialect to be used
     */
    void setDialect(int value);

    /**
     * Get the Interbase/Firebird dialect that is being used with this handle.
     *
     * @return The dialect being used
     */
    int getDialect();

    void setVersion(String value);

    String getVersion();

    /**
     * Get the product name for the database to which this handle is attached.
     *
     * @return The product name of the database
     */
    String getDatabaseProductName();

    /**
     * Get the product version for the database to which this handle 
     * is attached.
     *
     * @return The product version of the database
     */
    String getDatabaseProductVersion();

    /**
     * Get the major version number of the database product to which this
     * handle is attached.
     *
     * @return The major product version number
     */
    int getDatabaseProductMajorVersion();

    /**
     * Get the minor version number of the database product to which this
     * handle is attached.
     *
     * @return The minor product version number
     */
    int getDatabaseProductMinorVersion();

    void setODSMajorVersion(int value);

    int getODSMajorVersion();

    void setODSMinorVersion(int value);

    int getODSMinorVersion();
}


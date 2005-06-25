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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.IscDbHandle;


/**
 * Abstract implementation of {@link org.firebirdsql.gds.IscDbHandle} interface.
 * This class defines additional information that can be obtained when 
 * connecting to the database. 
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

    /**
     * @return the major ODS version of the database. 
     */
    public abstract int getODSMajorVersion();

    /**
     * @return the minor ODS version of the database.
     */
    public abstract int getODSMinorVersion();

    /**
     * @return database server version.
     */
    public abstract String getVersion();

    /**
     * Retrieve whether this handle is valid.
     *
     * @return <code>true</code> if this handle is valid, 
     *         <code>false</code> otherwise
     */
    public abstract boolean isValid();

}

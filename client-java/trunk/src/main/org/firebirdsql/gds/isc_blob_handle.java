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

package org.firebirdsql.gds;


// imports --------------------------------------


/**
 * The interface <code>isc_blob_handle</code> is a java mapping for a blob handle..
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface isc_blob_handle {

    /**
     * Get the identifier for the blob to which this handle is linked.
     *
     * @return identifier for the blob
     */
    long getBlob_id();

    /**
     * Set the identifier for the blob to which this handle is linked.
     *
     * @param blob_id The identifier to be set
     */
    void setBlob_id(long blob_id);
    
    // only used in the tests

    /**
     * Retrieve whether the <code>EOF</code> has been reached with this blob.
     *
     * @return <code>true</code> if <code>EOF</code> has been reached, 
     *         <code>false</code> otherwise
     */
    boolean isEof();

}

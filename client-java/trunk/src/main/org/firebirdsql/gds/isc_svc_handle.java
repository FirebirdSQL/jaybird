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

import java.util.List;

/**
 * The interface <code>isc_svc_handle</code> is a java mapping for a isc_svc_handle handle.
 */
public interface isc_svc_handle {

    /**
     * Retrieve whether this service handle is valid.
     *
     * @return <code>true</code> if the handle is valid, 
     *         <code>false</code> otherwise
     */
    boolean isValid();

    /**
     * Retrieve whether this service handle is invalid.
     *
     * @return <code>true</code> if the handle is invalid, 
     *         <code>false</code> otherwise
     */
    boolean isNotValid();

    /**
     * Get list of warnings that were returned by the server.
     *
     * @return instance of {@link java.util.List} containing instances of
     * {@link GDSException} representing server warnings (method
     * {@link GDSException#isWarning()} returns <code>true</code>).
     */
    List getWarnings();

    /**
     * Clear warning list associated with this connection.
     */
    void clearWarnings();
    }

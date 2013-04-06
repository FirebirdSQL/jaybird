/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

/**
 * Functional interface to process an information buffer (responses to p_info_*
 * requests) returning an object of type T.
 * 
 * @param T
 *            Type of the result of the {@link #process(byte[])} method.
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface InfoProcessor<T> {

    /**
     * Process an infoResponse block into an object of type T.
     * 
     * @param infoResponse
     *            byte array containing the server response to an info-request.
     * @return Processed response of type T (usually - but not required - a
     *         newly created object).
     * @throws FbException
     *             For errors during the infoResponse.
     */
    T process(byte[] infoResponse) throws FbException;
}

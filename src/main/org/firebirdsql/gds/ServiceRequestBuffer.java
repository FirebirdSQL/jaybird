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

/**
 * The interface <code>ServiceRequestBuffer</code> models represents the interbase Service Request Buffer.
 */
public interface ServiceRequestBuffer
    {
    /**
     * Set a void (valueless) parameter on this 
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     */
    public void addArgument(int argumentType);

    /**
     * Set a <code>String</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     * @param value The value to set for the given parameter
     */
    public void addArgument(int argumentType, String value);

    /**
     * Set an <code>int</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     * @param value The value to set for the given parameter
     */
    public void addArgument(int argumentType, int value);
    }

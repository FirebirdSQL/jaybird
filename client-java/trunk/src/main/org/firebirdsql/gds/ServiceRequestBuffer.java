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
     *
     * @param argumentType
     */
    public void addArgument(int argumentType);

    /**
     *
     * @param argumentType
     * @param value
     */
    public void addArgument(int argumentType, String value);

    /**
     *
     * @param argumentType
     * @param value
     */
    public void addArgument(int argumentType, int value);
    }

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
 * The interface <code>ServiceParameterBuffer</code> models represents the interbase Service Parameter Buffer.
 */
public interface ServiceParameterBuffer
    {
    /**
     * Set a void (valueless) parameter on this 
     * <code>ServiceParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, one of the 
     *        <code>isc_spb_*</code> constants from {@link ISCConstants}
     */
    public void addArgument(int argumentType);

    /**
     * Set a <code>String</code> parameter on this 
     * <code>ServiceParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, one of the
     *        <code>isc_spb_*</code> constants from {@link ISCConstants}
     * @param value The value to set for the given parameter type
     */
    public void addArgument(int argumentType, String value);

    /**
     * Set an <code>int</code> paramter on this
     * <code>ServiceParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, one of the
     *        <code>isc_spb_*</code> constants from {@link ISCConstants}
     * @param value The value to set for the given parameter type
     */
    public void addArgument(int argumentType, int value);
    
    /**
     * Set an <code>byte[]</code> paramter on this
     * <code>ServiceParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, one of the
     *        <code>isc_spb_*</code> constants from {@link ISCConstants}
     * @param value The value to set for the given parameter type
     */
    public void addArgument(int argumentType, byte[] data);
    }

/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.crypt;

/**
 * Details of the connection, which the SPI can use to decide if they can work.
 * <p>
 * NOTE: This class is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
public interface CryptConnectionInfo {

    /**
     * Protocol version of the connection.
     * <p>
     * The protocol version is masked, so use the relevant {@code PROTOCOL_VERSIONnn} constants from
     * {@link org.firebirdsql.gds.impl.wire.WireProtocolConstants} or equivalents for checks.
     * </p>
     *
     * @return protocol version, {@code 0} means unknown (shouldn't occur normally)
     */
    int protocolVersion();
}

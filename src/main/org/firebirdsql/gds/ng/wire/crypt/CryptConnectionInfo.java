// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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

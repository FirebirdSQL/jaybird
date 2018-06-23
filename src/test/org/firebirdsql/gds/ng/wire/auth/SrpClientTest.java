/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.util.ByteArrayHelper;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SrpClient}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class SrpClientTest {
    @Test
    public void testSessionKey() throws SQLException {
        String user = "SYSDBA";
        String password = "masterkey";

        SrpClient srp = new SrpClient("SHA-1");
        byte[] salt = SrpClient.getSalt();
        SrpClient.KeyPair server_key_pair = srp.serverSeed(user, password, salt);

        byte[] serverSessionKey = srp.getServerSessionKey(user, password, salt, srp.getPublicKey(),
                server_key_pair.getPublicKey(), server_key_pair.getPrivateKey());

        byte[] proof = srp.clientProof(user, password, salt, server_key_pair.getPublicKey());

        byte[] clientSessionKey = srp.getSessionKey();

        assertEquals(ByteArrayHelper.toHexString(clientSessionKey),
                ByteArrayHelper.toHexString(serverSessionKey));
    }
}
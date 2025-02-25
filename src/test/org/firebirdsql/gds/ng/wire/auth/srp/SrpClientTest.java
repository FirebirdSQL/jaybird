// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SrpClient}.
 *
 * @author Mark Rotteveel
 */
class SrpClientTest {
    @Test
    void testSessionKey() throws SQLException {
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
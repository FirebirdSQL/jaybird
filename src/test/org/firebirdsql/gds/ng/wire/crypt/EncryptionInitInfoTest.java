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
package org.firebirdsql.gds.ng.wire.crypt;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.crypto.Cipher;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Tests for {@link EncryptionInitInfo}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class EncryptionInitInfoTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final EncryptionIdentifier DUMMY_IDENTIFIER = new EncryptionIdentifier("type", "plugin");

    private static final Cipher DUMMY_CIPHER_1;
    private static final Cipher DUMMY_CIPHER_2;

    static {
        try {
            DUMMY_CIPHER_1 = Cipher.getInstance("ARCFOUR");
            DUMMY_CIPHER_2 = Cipher.getInstance("ARCFOUR");

            assert DUMMY_CIPHER_1 != DUMMY_CIPHER_2;
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Test
    public void successValues() {
        EncryptionInitInfo initInfo = EncryptionInitInfo.success(DUMMY_IDENTIFIER, DUMMY_CIPHER_1, DUMMY_CIPHER_2);

        assertEquals("encryptionIdentifier", DUMMY_IDENTIFIER, initInfo.getEncryptionIdentifier());
        assertEquals("initResult", EncryptionInitInfo.InitResult.SUCCESS, initInfo.getInitResult());
        assertTrue("success", initInfo.isSuccess());
        assertNull("exception", initInfo.getException());
        assertSame("encryptionCipher", DUMMY_CIPHER_1, initInfo.getEncryptionCipher());
        assertSame("decryptionCipher", DUMMY_CIPHER_2, initInfo.getDecryptionCipher());
    }

    @Test
    public void successRequiresEncryptionCipherNotNull() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("encryptionCipher");

        EncryptionInitInfo.success(DUMMY_IDENTIFIER, null, DUMMY_CIPHER_2);
    }

    @Test
    public void successRequiresDecryptionCipherNotNull() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("decryptionCipher");

        EncryptionInitInfo.success(DUMMY_IDENTIFIER, DUMMY_CIPHER_1, null);
    }

    @Test
    public void failureValues() {
        final SQLException exception = new SQLException();
        EncryptionInitInfo initInfo = EncryptionInitInfo.failure(DUMMY_IDENTIFIER, exception);

        assertEquals("encryptionIdentifier", DUMMY_IDENTIFIER, initInfo.getEncryptionIdentifier());
        assertEquals("initResult", EncryptionInitInfo.InitResult.FAILURE, initInfo.getInitResult());
        assertFalse("success", initInfo.isSuccess());
        assertSame("exception", exception, initInfo.getException());
    }

    @Test
    public void failureRequiresExceptionNotNull() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("exception");

        EncryptionInitInfo.failure(DUMMY_IDENTIFIER, null);
    }

    @Test
    public void failureGetEncryptionCipherNotAllowed() {
        EncryptionInitInfo initInfo = EncryptionInitInfo.failure(DUMMY_IDENTIFIER, new SQLException());
        expectedException.expect(IllegalStateException.class);

        initInfo.getEncryptionCipher();
    }

    @Test
    public void failureGetDecryptionCipherNotAllowed() {
        EncryptionInitInfo initInfo = EncryptionInitInfo.failure(DUMMY_IDENTIFIER, new SQLException());
        expectedException.expect(IllegalStateException.class);

        initInfo.getDecryptionCipher();
    }
}
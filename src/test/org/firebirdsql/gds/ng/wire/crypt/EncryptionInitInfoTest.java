// SPDX-FileCopyrightText: Copyright 2017-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;

import java.sql.SQLException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EncryptionInitInfo}.
 *
 * @author Mark Rotteveel
 */
class EncryptionInitInfoTest {

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
    void successValues() {
        EncryptionInitInfo initInfo = EncryptionInitInfo.success(DUMMY_IDENTIFIER, DUMMY_CIPHER_1, DUMMY_CIPHER_2);

        assertEquals(DUMMY_IDENTIFIER, initInfo.getEncryptionIdentifier(), "encryptionIdentifier");
        assertEquals(EncryptionInitInfo.InitResult.SUCCESS, initInfo.getInitResult(), "initResult");
        assertTrue(initInfo.isSuccess(), "success");
        assertNull(initInfo.getException(), "exception");
        assertSame(DUMMY_CIPHER_1, initInfo.getEncryptionCipher(), "encryptionCipher");
        assertSame(DUMMY_CIPHER_2, initInfo.getDecryptionCipher(), "decryptionCipher");
    }

    @Test
    void successRequiresEncryptionCipherNotNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                ()-> EncryptionInitInfo.success(DUMMY_IDENTIFIER, null, DUMMY_CIPHER_2));
        assertThat(exception, message(equalTo("encryptionCipher")));
    }

    @Test
    void successRequiresDecryptionCipherNotNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                ()-> EncryptionInitInfo.success(DUMMY_IDENTIFIER, DUMMY_CIPHER_1, null));
        assertThat(exception, message(equalTo("decryptionCipher")));
    }

    @Test
    void failureValues() {
        final SQLException exception = new SQLException();
        EncryptionInitInfo initInfo = EncryptionInitInfo.failure(DUMMY_IDENTIFIER, exception);

        assertEquals(DUMMY_IDENTIFIER, initInfo.getEncryptionIdentifier(), "encryptionIdentifier");
        assertEquals(EncryptionInitInfo.InitResult.FAILURE, initInfo.getInitResult(), "initResult");
        assertFalse(initInfo.isSuccess(), "success");
        assertSame(exception, initInfo.getException(), "exception");
    }

    @Test
    void failureRequiresExceptionNotNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                ()-> EncryptionInitInfo.failure(DUMMY_IDENTIFIER, null));
        assertThat(exception, message(equalTo("exception")));
    }

    @Test
    void failureGetEncryptionCipherNotAllowed() {
        EncryptionInitInfo initInfo = EncryptionInitInfo.failure(DUMMY_IDENTIFIER, new SQLException());

        assertThrows(IllegalStateException.class, initInfo::getEncryptionCipher);
    }

    @Test
    void failureGetDecryptionCipherNotAllowed() {
        EncryptionInitInfo initInfo = EncryptionInitInfo.failure(DUMMY_IDENTIFIER, new SQLException());

        assertThrows(IllegalStateException.class, initInfo::getDecryptionCipher);
    }
}
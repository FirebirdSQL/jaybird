/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class TestGDSServerVersion {

    private static final String TEST_VERSION_15 =
            "WI-V1.5.2.4731 Firebird 1.5,WI-V1.5.2.4731 Firebird 1.5/tcp (PCRORO)/P10";

    private static final String TEST_VERSION_21 =
            "WI-V2.1.3.18185 Firebird 2.1-WI-V2.1.3.18185 Firebird 2.1/tcp (Ramona)/P10";

    private static final String TEST_VERSION_30 =
            "WI-V3.0.2.32703 Firebird 3.0/WI-V3.0.2.32703 Firebird 3.0/tcp (Ramona)/P13:C";

    private static final String TEST_NO_EXTENDED_INFO =
            "WI-V2.1.3.18185 Firebird 2.1";

    private static final String TEST_NO_EXTENDED_INFO_SPARC =
            "S4-V2.1.3.18185 Firebird 2.1";

    private static final String TEST_INCORRECT_FORMAT =
            "WI-V2.5.2a.26540 Firebird 2.5";

    private static final String TEST_MAC_WITH_REVISION =
            "UI-V2.5.8.27089-1 Firebird 2.5DUI-V2.5.8.27089-1 Firebird 2.5/tcp (MacBook-Air-de-Ulises.local)/P10";

    @Test
    public void testParse15() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_VERSION_15);

        assertEquals("WI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(1, version.getMajorVersion());
        assertEquals(5, version.getMinorVersion());
        assertEquals(2, version.getVariant());
        assertEquals(4731, version.getBuildNumber());
        assertEquals("Firebird 1.5", version.getServerName());
        assertEquals("WI-V1.5.2.4731 Firebird 1.5/tcp (PCRORO)/P10",
                version.getExtendedServerName());
        assertEquals("WI-V1.5.2.4731", version.getFullVersion());
        assertEquals(10, version.getProtocolVersion());
        assertFalse(version.isWireEncryptionUsed());
        assertFalse(version.isWireCompressionUsed());
    }

    @Test
    public void testParse21() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_VERSION_21);

        assertEquals("WI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(2, version.getMajorVersion());
        assertEquals(1, version.getMinorVersion());
        assertEquals(3, version.getVariant());
        assertEquals(18185, version.getBuildNumber());
        assertEquals("Firebird 2.1", version.getServerName());
        assertEquals("WI-V2.1.3.18185 Firebird 2.1/tcp (Ramona)/P10",
                version.getExtendedServerName());
        assertEquals("WI-V2.1.3.18185", version.getFullVersion());
        assertEquals(10, version.getProtocolVersion());
        assertFalse(version.isWireEncryptionUsed());
        assertFalse(version.isWireCompressionUsed());
    }

    @Test
    public void testParse30() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_VERSION_30);

        assertEquals("WI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(3, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(2, version.getVariant());
        assertEquals(32703, version.getBuildNumber());
        assertEquals("Firebird 3.0", version.getServerName());
        assertEquals("WI-V3.0.2.32703 Firebird 3.0/tcp (Ramona)/P13:C",
                version.getExtendedServerName());
        assertEquals("WI-V3.0.2.32703", version.getFullVersion());
        assertEquals(13, version.getProtocolVersion());
        assertTrue(version.isWireEncryptionUsed());
        assertFalse(version.isWireCompressionUsed());
    }

    @Test
    public void testParse30WithCompression() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_VERSION_30 + "Z");

        assertEquals("WI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(3, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(2, version.getVariant());
        assertEquals(32703, version.getBuildNumber());
        assertEquals("Firebird 3.0", version.getServerName());
        assertEquals("WI-V3.0.2.32703 Firebird 3.0/tcp (Ramona)/P13:CZ",
                version.getExtendedServerName());
        assertEquals("WI-V3.0.2.32703", version.getFullVersion());
        assertEquals(13, version.getProtocolVersion());
        assertTrue(version.isWireEncryptionUsed());
        assertTrue(version.isWireCompressionUsed());
    }

    @Test
    public void testParseNoExtendedInfo() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_NO_EXTENDED_INFO);

        assertEquals("WI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(2, version.getMajorVersion());
        assertEquals(1, version.getMinorVersion());
        assertEquals(3, version.getVariant());
        assertEquals(18185, version.getBuildNumber());
        assertEquals("Firebird 2.1", version.getServerName());
        assertNull(version.getExtendedServerName());
        assertEquals("WI-V2.1.3.18185", version.getFullVersion());
        assertEquals(-1, version.getProtocolVersion());
        assertFalse(version.isWireEncryptionUsed());
    }

    /**
     * Check if version with a number in the platform (ie Sparc 4) is correctly parsed
     */
    @Test
    public void testParseSparcVersion() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_NO_EXTENDED_INFO_SPARC);

        assertEquals("S4", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(2, version.getMajorVersion());
        assertEquals(1, version.getMinorVersion());
        assertEquals(3, version.getVariant());
        assertEquals(18185, version.getBuildNumber());
        assertEquals("Firebird 2.1", version.getServerName());
        assertNull(version.getExtendedServerName());
        assertEquals("S4-V2.1.3.18185", version.getFullVersion());
    }

    @Test
    public void testMacWithRevision() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_MAC_WITH_REVISION);

        assertEquals("UI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(2, version.getMajorVersion());
        assertEquals(5, version.getMinorVersion());
        assertEquals(8, version.getVariant());
        assertEquals(27089, version.getBuildNumber());
        assertEquals("Firebird 2.5DUI", version.getServerName());
        assertEquals("V2.5.8.27089-1 Firebird 2.5/tcp (MacBook-Air-de-Ulises.local)/P10",
                version.getExtendedServerName());
        assertEquals("UI-V2.5.8.27089-1", version.getFullVersion());
    }

    @Test(expected = GDSServerVersionException.class)
    public void testIncorrectFormat() throws Exception {
        GDSServerVersion.parseRawVersion(TEST_INCORRECT_FORMAT);
    }

    @Test
    public void testSerializable() throws Exception {
        final GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_VERSION_21);

        // Serialize object
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        objectOut.writeObject(version);

        // Deserialize object
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        GDSServerVersion serializedVersion = (GDSServerVersion) objectIn.readObject();

        assertEquals("Serialized version should be equal to original", version, serializedVersion);
    }
}

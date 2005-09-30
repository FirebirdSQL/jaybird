package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.GDSServerVersion;

import junit.framework.TestCase;


public class TestGDSServerVersion extends TestCase {

    public static final String TEST_VERSION = 
        "WI-V1.5.2.4731 Firebird 1.5,WI-V1.5.2.4731 Firebird 1.5/tcp (PCRORO)/P10";
    
    public TestGDSServerVersion(String arg0) {
        super(arg0);
    }

    public void testParse() throws Exception {
        GDSServerVersion version = new GDSServerVersion(TEST_VERSION);
        
        assertEquals("WI", version.getPlatform());
        assertEquals("V", version.getType());
        assertEquals(1, version.getMajorVersion());
        assertEquals(5, version.getMinorVersion());
        assertEquals(2, version.getVariant());
        assertEquals(4731, version.getBuildNumber());
        assertEquals("Firebird 1.5", version.getServerName());
        assertEquals("WI-V1.5.2.4731 Firebird 1.5/tcp (PCRORO)/P10", 
            version.getExtendedServerName());
    }
}

package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.GDSServerVersion;

import junit.framework.TestCase;

public class TestGDSServerVersion extends TestCase {

    private static final String TEST_VERSION_15 = 
        "WI-V1.5.2.4731 Firebird 1.5,WI-V1.5.2.4731 Firebird 1.5/tcp (PCRORO)/P10";
    
    private static final String TEST_VERSION_21 =
    	"WI-V2.1.3.18185 Firebird 2.1-WI-V2.1.3.18185 Firebird 2.1/tcp (Ramona)/P10";
    
    private static final String TEST_NO_EXTENDED_INFO =
    	"WI-V2.1.3.18185 Firebird 2.1";
    
    public TestGDSServerVersion(String arg0) {
        super(arg0);
    }

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
    }
    
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
    }
    
    public void testParseNoExtendedInfo() throws Exception {
    	GDSServerVersion version = GDSServerVersion.parseRawVersion(TEST_NO_EXTENDED_INFO);
    	
    	assertEquals("WI", version.getPlatform());
    	assertEquals("V", version.getType());
    	assertEquals(2, version.getMajorVersion());
    	assertEquals(1, version.getMinorVersion());
    	assertEquals(3, version.getVariant());
    	assertEquals(18185, version.getBuildNumber());
    	assertEquals("Firebird 2.1", version.getServerName());
    	assertEquals(null, 
    			version.getExtendedServerName());
    	assertEquals("WI-V2.1.3.18185", version.getFullVersion());
    }
}

package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

public class TestFBConnection4_0 extends FBTestBase {

    public TestFBConnection4_0(String name) {
        super(name);
    }

    public void testClientInfo() throws Exception {
        AbstractConnection connection = (AbstractConnection)getConnectionViaDriverManager();
        try {
            
            connection.setClientInfo("TestProperty", "testValue");
            String checkValue = connection.getClientInfo("TestProperty");
            assertEquals("testValue", checkValue);
            
        } finally {
            connection.close();
        }
    }
}

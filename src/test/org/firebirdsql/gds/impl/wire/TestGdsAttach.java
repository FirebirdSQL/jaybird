package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.wire.isc_db_handle_impl;

import junit.framework.TestCase;


public class TestGdsAttach extends org.firebirdsql.common.SimpleFBTestBase {

    private final String DATABASE = DB_SERVER_URL + ":" + DB_PATH 
        + "/" + DB_NAME;

    public TestGdsAttach(String name) {
        super(name);
    }
    
    private GDS gds;
    private DatabaseParameterBuffer c;
    
    protected void setUp() throws Exception {
        gds = GDSFactory.getDefaultGDS();

        c = gds.createDatabaseParameterBuffer();

        c.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
        c.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_user_name, "SYSDBA");
        c.addArgument(ISCConstants.isc_dpb_password, "masterkey");
        
        IscDbHandle db = gds.createIscDbHandle();

        gds.iscCreateDatabase(DATABASE, db, c);
        gds.iscDetachDatabase(db);

    }

    protected void tearDown() throws Exception {
        IscDbHandle db = gds.createIscDbHandle();

        gds.iscAttachDatabase(DATABASE, db, c);
        gds.iscDropDatabase(db);
        
    }
    
    public void testDummy() {
        // empty
    }
    
    public void _testMultipleAttach() throws Exception {
        IscDbHandle db1 = gds.createIscDbHandle();
        IscDbHandle db2 = gds.createIscDbHandle();
        IscDbHandle db3 = gds.createIscDbHandle();
        
        gds.iscAttachDatabase(DATABASE, db1, c);
        try {
            gds.iscAttachDatabase(DATABASE, db2, c);
            try {
                try {
                    gds.iscAttachDatabase(DATABASE, db3, c);
                    
                    assertTrue("DB1 object id is 0", ((isc_db_handle_impl)db1).getResp_object() != 0);
                    assertTrue("DB2 object id is 0", ((isc_db_handle_impl)db2).getResp_object() != 0);
                    assertTrue("DB3 object id is 0", ((isc_db_handle_impl)db3).getResp_object() != 0);
                    
                } finally {
                    gds.iscDetachDatabase(db3);
                }
            } finally {
                gds.iscDetachDatabase(db2);
            }
        } finally {
            gds.iscDetachDatabase(db1);
        }
    }
}

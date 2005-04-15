package org.firebirdsql.jgds;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.isc_db_handle;
import org.firebirdsql.gds.impl.GDSFactory;

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

        c = gds.newDatabaseParameterBuffer();

        c.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
        c.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_user_name, "SYSDBA");
        c.addArgument(ISCConstants.isc_dpb_password, "masterkey");
        
        isc_db_handle db = gds.get_new_isc_db_handle();

        gds.isc_create_database(DATABASE, db, c);
        gds.isc_detach_database(db);

    }

    protected void tearDown() throws Exception {
        isc_db_handle db = gds.get_new_isc_db_handle();

        gds.isc_attach_database(DATABASE, db, c);
        gds.isc_drop_database(db);
        
    }
    
    public void testDummy() {
        // empty
    }
    
    public void _testMultipleAttach() throws Exception {
        isc_db_handle db1 = gds.get_new_isc_db_handle();
        isc_db_handle db2 = gds.get_new_isc_db_handle();
        isc_db_handle db3 = gds.get_new_isc_db_handle();
        
        gds.isc_attach_database(DATABASE, db1, c);
        try {
            gds.isc_attach_database(DATABASE, db2, c);
            try {
                try {
                    gds.isc_attach_database(DATABASE, db3, c);
                    
                    assertTrue("DB1 object id is 0", ((isc_db_handle_impl)db1).getResp_object() != 0);
                    assertTrue("DB2 object id is 0", ((isc_db_handle_impl)db2).getResp_object() != 0);
                    assertTrue("DB3 object id is 0", ((isc_db_handle_impl)db3).getResp_object() != 0);
                    
                } finally {
                    gds.isc_detach_database(db3);
                }
            } finally {
                gds.isc_detach_database(db2);
            }
        } finally {
            gds.isc_detach_database(db1);
        }
    }
}

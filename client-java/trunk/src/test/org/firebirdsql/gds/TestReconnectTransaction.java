package org.firebirdsql.gds;

import java.sql.Connection;
import java.util.Arrays;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.jca.FBTpbMapper;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.jgds.isc_db_handle_impl;


/**
 * 
 */
public class TestReconnectTransaction extends FBTestBase {

    private static final byte[] message = new byte[] {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };
    
    private static final String RECOVERY_QUERY = ""
        + "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
        + "FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_STATE = 1";

    
    /**
     * @param name
     */
    public TestReconnectTransaction(String name) {
        super(name);
    }

    
    private GDS gds;
    private FBTpb tpb;
    private DatabaseParameterBuffer dpb;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        gds = GDSFactory.getGDSForType(getGdsType());
        
        dpb = gds.newDatabaseParameterBuffer();
        dpb.addArgument(DatabaseParameterBuffer.USER, this.DB_USER);
        dpb.addArgument(DatabaseParameterBuffer.PASSWORD, this.DB_PASSWORD);
        
        tpb = new FBTpb(FBTpbMapper.getDefaultMapper(gds));
        tpb.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private static class DataProvider implements FieldDataProvider {

        private isc_stmt_handle stmtHandle;
        private int fieldPos;
        private int row;
        
        private DataProvider(isc_stmt_handle stmtHandle, int fieldPos) {
            this.stmtHandle = stmtHandle;
            this.fieldPos = fieldPos;
        }
        
        public void setRow(int row) {
            this.row = row;
        }
        
        public byte[] getFieldData() {
            return ((byte[][])stmtHandle.getRows()[row])[fieldPos];
        }
        public void setFieldData(byte[] data) {
            throw new UnsupportedOperationException();
        }
}
    
    public void testReconnectTransaction() throws Exception {
        
        isc_db_handle dbHandle1 = gds.get_new_isc_db_handle();
        gds.isc_attach_database(getdbpath(DB_NAME), dbHandle1, dpb);
        

        isc_tr_handle trHandle1 = gds.get_new_isc_tr_handle();
        gds.isc_start_transaction(trHandle1, dbHandle1, tpb.getTransactionParameterBuffer());
        
        gds.isc_prepare_transaction2(trHandle1, message);
        
        //gds.isc_commit_transaction(trHandle1);
        
        //gds.isc_detach_database(dbHandle1);
        ((isc_db_handle_impl)dbHandle1).out.close();
        
        isc_db_handle dbHandle2 = gds.get_new_isc_db_handle();
        gds.isc_attach_database(getdbpath(DB_NAME), dbHandle2, dpb);
        
        isc_tr_handle trHandle2 = gds.get_new_isc_tr_handle();
        gds.isc_start_transaction(trHandle2, dbHandle2, tpb.getTransactionParameterBuffer());
        
        isc_stmt_handle stmtHandle2 = gds.get_new_isc_stmt_handle();
        gds.isc_dsql_allocate_statement(dbHandle2, stmtHandle2);
        
        GDSHelper gdsHelper2 = new GDSHelper(gds, dpb, dbHandle2);
        gdsHelper2.setCurrentTrHandle(trHandle2);
        
        gdsHelper2.prepareStatement(stmtHandle2, RECOVERY_QUERY, false);
        gdsHelper2.executeStatement(stmtHandle2, false);
        gdsHelper2.fetch(stmtHandle2, 10);
        
        DataProvider dataProvider0 = new DataProvider(stmtHandle2, 0);
        DataProvider dataProvider1 = new DataProvider(stmtHandle2, 1);
        
        FBField field0 = FBField.createField(stmtHandle2.getOutSqlda().sqlvar[0], dataProvider0, gdsHelper2, false);
        FBField field1 = FBField.createField(stmtHandle2.getOutSqlda().sqlvar[1], dataProvider1, gdsHelper2, false);
        
        field0.setConnection(gdsHelper2);
        field1.setConnection(gdsHelper2);
        
        int row = 0;
        
        while(row < stmtHandle2.getRows().length) {
        
            dataProvider0.setRow(row);
            dataProvider1.setRow(row);
            
            long inLimboTxId = field0.getLong();
            byte[] inLimboMessage = field1.getBytes();
        
            if (Arrays.equals(message, inLimboMessage)) {
                isc_tr_handle inLimboTrHandle = gds.get_new_isc_tr_handle();
                gds.isc_reconnect_transaction(inLimboTrHandle, dbHandle2, inLimboTxId);
                
                gds.isc_rollback_transaction(inLimboTrHandle);
            }
            
            row++;
        }
        
        gdsHelper2.closeStatement(stmtHandle2, true);
        
        gds.isc_commit_transaction(trHandle2);
        gds.isc_detach_database(dbHandle2);
    }
}

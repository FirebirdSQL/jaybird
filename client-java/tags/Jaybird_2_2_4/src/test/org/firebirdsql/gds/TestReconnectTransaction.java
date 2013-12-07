package org.firebirdsql.gds;

import java.util.Arrays;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.impl.*;
import org.firebirdsql.gds.impl.wire.isc_db_handle_impl;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.jdbc.FBTpbMapper;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;


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
        
        dpb = gds.createDatabaseParameterBuffer();
        dpb.addArgument(DatabaseParameterBuffer.USER, this.DB_USER);
        dpb.addArgument(DatabaseParameterBuffer.PASSWORD, this.DB_PASSWORD);
        
        tpb = new FBTpb(FBTpbMapper.getDefaultMapper(gds).getDefaultMapping());
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private static class DataProvider implements FieldDataProvider {

        private AbstractIscStmtHandle stmtHandle;
        private int fieldPos;
        private int row;
        
        private DataProvider(AbstractIscStmtHandle stmtHandle, int fieldPos) {
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
        if (getGdsType() != GDSType.getType("PURE_JAVA"))
            fail("This test case does not work with JNI connections.");
        
        IscDbHandle dbHandle1 = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(DB_NAME), dbHandle1, dpb);
        
        GDSHelper gdsHelper1 = new GDSHelper(gds, dpb, dbHandle1, null);

        AbstractIscTrHandle trHandle1 = (AbstractIscTrHandle)gds.createIscTrHandle();
        gds.iscStartTransaction(trHandle1, dbHandle1, tpb.getTransactionParameterBuffer());

        gdsHelper1.getTransactionId(trHandle1);
        
        gds.iscPrepareTransaction2(trHandle1, message);
        
        //gds.isc_commit_transaction(trHandle1);
        
        //gds.isc_detach_database(dbHandle1);
        if (dbHandle1 instanceof isc_db_handle_impl)
            ((isc_db_handle_impl)dbHandle1).out.close();
        
        IscDbHandle dbHandle2 = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(DB_NAME), dbHandle2, dpb);
        
        AbstractIscTrHandle trHandle2 = (AbstractIscTrHandle)gds.createIscTrHandle();
        gds.iscStartTransaction(trHandle2, dbHandle2, tpb.getTransactionParameterBuffer());
        
        AbstractIscStmtHandle stmtHandle2 = (AbstractIscStmtHandle)gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(dbHandle2, stmtHandle2);
        
        GDSHelper gdsHelper2 = new GDSHelper(gds, dpb, dbHandle2, null);
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
        
        boolean foundInLimboTx = false;
        int row = 0;
        while(row < stmtHandle2.getRows().length) {
            
            if (stmtHandle2.getRows()[row] == null) {
                row++;
                continue;
            }
                
        
            dataProvider0.setRow(row);
            dataProvider1.setRow(row);
            
            long inLimboTxId = field0.getLong();
            byte[] inLimboMessage = field1.getBytes();
        
            if (Arrays.equals(message, inLimboMessage)) {
                foundInLimboTx = true;
                
                IscTrHandle inLimboTrHandle = gds.createIscTrHandle();
                gds.iscReconnectTransaction(inLimboTrHandle, dbHandle2, inLimboTxId);
                assertEquals(
                        inLimboTxId,
                        gdsHelper2.getTransactionId(inLimboTrHandle));
                gds.iscRollbackTransaction(inLimboTrHandle);
                break;
            }
            
            row++;
        }


        
        gdsHelper2.closeStatement(stmtHandle2, true);
        
        gds.iscCommitTransaction(trHandle2);
        gds.iscDetachDatabase(dbHandle2);
        
        assertTrue("Should find in-limbo tx.", foundInLimboTx);
    }
}

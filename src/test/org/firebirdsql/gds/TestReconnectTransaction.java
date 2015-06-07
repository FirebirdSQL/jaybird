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
package org.firebirdsql.gds;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DefaultStatementListener;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.jdbc.FBTpbMapper;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestReconnectTransaction extends FBJUnit4TestBase {

    private static final byte[] message = new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };

    private static final String RECOVERY_QUERY = ""
            + "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
            + "FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_STATE = 1";

    private FBTpb tpb;

    @Before
    public void setUp() throws Exception {
        tpb = new FBTpb(FBTpbMapper.getDefaultMapper().getDefaultMapping());
    }

    private static class DataProvider implements FieldDataProvider {

        private final List<RowValue> rows;
        private final int fieldPos;
        private int row;

        private DataProvider(List<RowValue> rows, int fieldPos) {
            this.rows = rows;
            this.fieldPos = fieldPos;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public byte[] getFieldData() {
            return rows.get(row).getFieldValue(fieldPos).getFieldData();
        }

        public void setFieldData(byte[] data) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testReconnectTransaction() throws Exception {
        FbConnectionProperties connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");

        FbDatabaseFactory databaseFactory = GDSFactory.getDatabaseFactoryForType(getGdsType());
        try (FbDatabase dbHandle1 = databaseFactory.connect(connectionInfo)) {
            dbHandle1.attach();
            FbTransaction trHandle1 = dbHandle1.startTransaction(tpb.getTransactionParameterBuffer());
            trHandle1.prepare(message);

            // No commit! We leave trHandle1 in Limbo.
        }

        try (FbDatabase dbHandle2 = databaseFactory.connect(connectionInfo)) {
            dbHandle2.attach();
            GDSHelper gdsHelper2 = new GDSHelper(null, dbHandle2);
            FbTransaction trHandle2 = dbHandle2.startTransaction(tpb.getTransactionParameterBuffer());
            gdsHelper2.setCurrentTransaction(trHandle2);

            FbStatement stmtHandle2 = dbHandle2.createStatement(trHandle2);
            stmtHandle2.prepare(RECOVERY_QUERY);

            final List<RowValue> rows = new ArrayList<>();
            StatementListener stmtListener = new DefaultStatementListener() {
                @Override
                public void receivedRow(FbStatement sender, RowValue rowValues) {
                    rows.add(rowValues);
                }
            };
            stmtHandle2.addStatementListener(stmtListener);
            stmtHandle2.execute(RowValue.EMPTY_ROW_VALUE);
            stmtHandle2.fetchRows(10);

            DataProvider dataProvider0 = new DataProvider(rows, 0);
            DataProvider dataProvider1 = new DataProvider(rows, 1);

            FBField field0 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(0), dataProvider0, gdsHelper2, false);
            FBField field1 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(1), dataProvider1, gdsHelper2, false);

            boolean foundInLimboTx = false;
            int row = 0;
            while (row < rows.size()) {
                dataProvider0.setRow(row);
                dataProvider1.setRow(row);

                long inLimboTxId = field0.getLong();
                byte[] inLimboMessage = field1.getBytes();

                if (Arrays.equals(message, inLimboMessage)) {
                    foundInLimboTx = true;

                    FbTransaction inLimboTrHandle = dbHandle2.reconnectTransaction(inLimboTxId);
                    assertEquals(inLimboTxId, inLimboTrHandle.getTransactionId());
                    inLimboTrHandle.rollback();
                    break;
                }
                row++;
            }

            stmtHandle2.close();
            trHandle2.commit();

            assertTrue("Should find in-limbo tx.", foundInLimboTx);
        }


    }
}

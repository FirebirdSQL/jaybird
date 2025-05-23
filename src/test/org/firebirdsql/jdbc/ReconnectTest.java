/*
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;

class ReconnectTest {

    // TODO It is unclear what this class actually tests (there is no reconnect involved)

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private static final int TABLE_COUNT = 10;

    private Connection con;

    @AfterEach
    void tearDown() throws SQLException {
        if (con != null) {
            con.close();
        }
    }

    private static String getTableName(int no) {
        return "TEST" + no;
    }

    private void execute(String sql) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void execute(String sql, boolean retryOnError) throws SQLException {
        if (retryOnError) {
            try {
                execute(sql);
            } catch (SQLException e) {
                // Workaround for the well-known "object in use" error
                // (see release notes of Firebird 1.5)
                boolean oldAutoCommit = con.getAutoCommit();
                con.close();
                openConnection();
                if (con.getAutoCommit() != oldAutoCommit)
                    con.setAutoCommit(oldAutoCommit);
                execute(sql);
                // Here the program hangs (see socketRead), and
                // pressing ctrl-break I get the following output:
                // ---------

                //Full thread dump:
                //
                //"Signal Dispatcher" daemon prio=10 tid=0x8f2ec0 nid=0x29c waiting on monitor [0..0]
                //
                //"Finalizer" daemon prio=9 tid=0x8f1098 nid=0x2ec waiting on monitor [0x8cdf000..0x8cdfdbc]
                //        at java.lang.Object.wait(Native Method)
                //        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:108)
                //        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:123)
                //        at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:162)
                //
                //"Reference Handler" daemon prio=10 tid=0x8a20c68 nid=0x2bc waiting on monitor [0x8c9f000..0x8c9fdbc]
                //        at java.lang.Object.wait(Native Method)
                //        at java.lang.Object.wait(Object.java:420)
                //        at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:110)
                //
                //"main" prio=5 tid=0x346d8 nid=0x550 runnable [0x7f000..0x7fc34]
                //        at java.net.SocketInputStream.socketRead(Native Method)
                //        at java.net.SocketInputStream.read(SocketInputStream.java:86)
                //        at java.io.BufferedInputStream.fill(BufferedInputStream.java:186)
                //        at java.io.BufferedInputStream.read(BufferedInputStream.java:204)
                //        at java.io.DataInputStream.readInt(DataInputStream.java:338)
                //        at org.firebirdsql.jgds.GDS_Impl.nextOperation(GDS_Impl.java:1471)
                //        at org.firebirdsql.jgds.GDS_Impl.isc_dsql_execute2(GDS_Impl.java:728)
                //        at org.firebirdsql.jca.FBManagedConnection.executeStatement(FBManagedConnection.java:593)
                //        at org.firebirdsql.jdbc.FBConnection.executeStatement(FBConnection.java:1104)
                //        at org.firebirdsql.jdbc.FBStatement.internalExecute(FBStatement.java:929)
                //        at org.firebirdsql.jdbc.FBStatement.executeUpdate(FBStatement.java:146)
                //        at TestReconnect.execute(TestReconnect.java:19)
                //        at TestReconnect.execute(TestReconnect.java:58)
                //        at TestReconnect.alterForeignKeys(TestReconnect.java:192)
                //        at TestReconnect.run(TestReconnect.java:314)
                //        at TestReconnect.main(TestReconnect.java:337)
                //
                //"VM Thread" prio=5 tid=0x91b8f8 nid=0x5bc runnable
                //
                //"VM Periodic Task Thread" prio=10 tid=0x8f2c60 nid=0x30c waiting on monitor
                //"Suspend Checker Thread" prio=10 tid=0x8f2d80 nid=0x1d8 runnable
                // ---------
            }
        } else
            execute(sql);
    }

    private boolean tableExists(String tableName) throws SQLException {
        boolean exists = false;
        ResultSet rs = con.getMetaData().getTables(null, null, tableName, null);
        if (rs != null) {
            exists = rs.next();
            rs.close();
        }
        return exists;
    }

    private void dropTestTables() throws SQLException {
        if (!con.getAutoCommit())
            con.setAutoCommit(true);
        for (int i = TABLE_COUNT; i > 0; i--) {
            String tableName = getTableName(i);
            if (tableExists(tableName))
                execute("DROP TABLE " + tableName, true);
        }
    }

    private void createTestTables() throws SQLException {
        if (!con.getAutoCommit())
            con.setAutoCommit(true);

        for (int i = 1; i <= TABLE_COUNT; i++) {
            String table = getTableName(i);
            StringBuilder sql = new StringBuilder(100);
            sql.append("CREATE TABLE ");
            sql.append(table);
            sql.append("""
                     (
                    ID INTEGER NOT NULL,
                    NR INTEGER NOT NULL,
                    X1 VARCHAR(50),
                    X2 VARCHAR(50),
                    X3 VARCHAR(50),
                    """);
            if (i > 1) {
                sql.append("ID_");
                sql.append(getTableName(i - 1));
                sql.append(" INTEGER,\n");
            }
            sql.append("CONSTRAINT PK_");
            sql.append(table);
            sql.append(" PRIMARY KEY (ID))");
            execute(sql.toString());
        }
        // add FOREIGN KEY's
        for (int i = 2; i <= TABLE_COUNT; i++) {
            String thisTable = getTableName(i);
            String refTable = getTableName(i - 1);
            String sql = "ALTER TABLE " + thisTable +
                    " ADD CONSTRAINT FK_" + refTable +
                    " FOREIGN KEY (ID_" + refTable + ") REFERENCES " + refTable + " (ID)";
            execute(sql, true);
        }
    }

    private void alterForeignKeys(boolean cascade) throws SQLException {
        if (!con.getAutoCommit())
            con.setAutoCommit(true);
        // add FOREIGN KEY's
        for (int i = 2; i <= TABLE_COUNT; i++) {
            String thisTable = getTableName(i);
            String refTable = getTableName(i - 1);
            String sql = "ALTER TABLE " + thisTable +
                    " DROP CONSTRAINT FK_" + refTable;
            execute(sql, true);

            String sql2 = "ALTER TABLE " + thisTable +
                    " ADD CONSTRAINT FK_" + refTable +
                    " FOREIGN KEY (ID_" + refTable + ") REFERENCES " + refTable + " (ID)" +
                    (cascade ? " ON DELETE CASCADE" : "");
            execute(sql2, true);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void populateTestTables(int rowCount) throws SQLException {
        if (con.getAutoCommit())
            con.setAutoCommit(false);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 1; i <= TABLE_COUNT; i++) {
            StringBuilder sql = new StringBuilder(100);
            sql.append("INSERT INTO ");
            sql.append(getTableName(i));
            sql.append(" (ID,NR,X1,X2,X3");
            if (i > 1) {
                sql.append(",ID_");
                sql.append(getTableName(i - 1));
            }
            sql.append(") VALUES (?,?,?,?,?");
            if (i > 1)
                sql.append(",?");
            sql.append(")");
            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                for (int row = 1; row <= rowCount; row++) {
                    int rndValue = random.nextInt(1000);
                    stmt.setInt(1, row);
                    stmt.setInt(2, rndValue);
                    stmt.setString(3, "X1." + row + "." + rndValue);
                    stmt.setString(4, "X2." + row + "." + rndValue);
                    stmt.setString(5, "X3." + row + "." + rndValue);
                    if (i > 1)
                        stmt.setInt(6, row);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            con.commit();
        }
    }

    private void readResult(String title, ResultSet rs, boolean print) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        if (print) {
            System.out.println(title);
            System.out.println("-------------------------------------------------------------------------------");
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                if (i > 1)
                    sb.append('\t');
                sb.append(md.getColumnLabel(i));
            }
            System.out.println(sb);
            System.out.println("-------------------------------------------------------------------------------");
        }
        while (rs.next()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                String value = rs.getString(i);

                if (i > 1)
                    sb.append('\t');
                sb.append(value);

            }
            System.out.println(sb);
        }
        rs.close();
    }

    private void readMetaData() throws SQLException {
        DatabaseMetaData md = con.getMetaData();
        for (int i = 1; i <= TABLE_COUNT; i++) {
            String tableName = getTableName(i);
            boolean print = i == 1;
            readResult("COLUMNS of " + tableName, md.getColumns(null, null, tableName, null), print);
            readResult("INDEX INFO of " + tableName, md.getIndexInfo(null, null, tableName, false, false), print);
            readResult("PRIMARY KEYS of " + tableName, md.getPrimaryKeys(null, null, tableName), print);
            readResult("IMPORTED KEYS of " + tableName, md.getImportedKeys(null, null, tableName), print);
            readResult("EXPORTED KEYS of " + tableName, md.getExportedKeys(null, null, tableName), print);
        }
    }

    private void openConnection() throws SQLException {
        con = getConnectionViaDriverManager();
        con.setAutoCommit(true);
        con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    @Test
    void testReconnect() throws Exception {
        openConnection();
        dropTestTables();
        createTestTables();
        populateTestTables(100);

        readResult("TYPE INFO", con.getMetaData().getTypeInfo(), true);
        readMetaData();
        alterForeignKeys(true);
        readMetaData();
        alterForeignKeys(false);
    }
}

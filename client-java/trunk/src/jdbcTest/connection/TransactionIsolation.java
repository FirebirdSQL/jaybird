// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TransactionIsolation.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class TransactionIsolation extends TestModule
{

    public TransactionIsolation()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.setTransactionIsolation");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            test(connection, "getTransactionIsolation()");
            int i1 = connection.getTransactionIsolation();
            result(i1);
            assert(i1 != 0, "Transactions must be supported");
            switch(i1)
            {
            case 3: // '\003'
            case 5: // '\005'
            case 6: // '\006'
            case 7: // '\007'
            default:
                assert(false, "Invalid default isolation value");
                // fall through

            case 1: // '\001'
            case 2: // '\002'
            case 4: // '\004'
            case 8: // '\b'
                test(databasemetadata, "supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED)");
                break;
            }
            boolean flag = databasemetadata.supportsTransactionIsolationLevel(1);
            result(flag);
            if(flag)
            {
                test(connection, "setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED)");
                connection.setTransactionIsolation(1);
                test(connection, "getTransactionIsolation()");
                int i = connection.getTransactionIsolation();
                result(i);
                assert(i == 1, "Isolation should be TRANSACTION_READ_UNCOMMITTED");
            }
            test(databasemetadata, "supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED)");
            flag = databasemetadata.supportsTransactionIsolationLevel(2);
            result(flag);
            if(flag)
            {
                test(connection, "setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED)");
                connection.setTransactionIsolation(2);
                test(connection, "getTransactionIsolation()");
                int j = connection.getTransactionIsolation();
                result(j);
                assert(j == 2, "Isolation should be TRANSACTION_READ_COMMITTED");
            }
            test(databasemetadata, "supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ)");
            flag = databasemetadata.supportsTransactionIsolationLevel(4);
            if(flag)
            {
                test(connection, "setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ)");
                connection.setTransactionIsolation(4);
                test(connection, "getTransactionIsolation()");
                int k = connection.getTransactionIsolation();
                result(k);
                assert(k == 4, "Isolation should be TRANSACTION_REPEATABLE_READ");
            }
            test(databasemetadata, "supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)");
            flag = databasemetadata.supportsTransactionIsolationLevel(8);
            result(flag);
            if(flag)
            {
                test(connection, "setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE)");
                connection.setTransactionIsolation(8);
                test(connection, "getTransactionIsolation()");
                int l = connection.getTransactionIsolation();
                result(l);
                assert(l == 8, "Isolation should be TRANSACTION_SERIALIZABLE");
            }
            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace(Log.out);
            failed();
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   DisableAutoCommit.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class DisableAutoCommit extends TestModule
{

    public DisableAutoCommit()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.setAutoCommit");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            test(connection, "getMetaData()");
            DatabaseMetaData databasemetadata = connection.getMetaData();
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            assert(connection.getAutoCommit(), "Verify that auto commit is on by default");
            assert(databasemetadata.supportsTransactions(), "Skip this test if the driver doesn't support transactions");
            trySQL(connection, "drop table disableautocommit");
            executeSQL(connection, "create table disableautocommit (name char(30))");
            executeSQL(connection, "insert into disableautocommit values('first')");
            test(connection, "setAutoCommit(false)");
            connection.setAutoCommit(false);
            assert(connection.getAutoCommit() ^ true, "Verify that auto commit has now been disabled");
            test(connection, "rollback()");
            connection.rollback();
            executeSQL(connection, "insert into disableautocommit values('second')");
            test(connection, "commit()");
            connection.commit();
            executeSQL(connection, "insert into disableautocommit values('third')");
            test(connection, "rollback()");
            connection.rollback();
            test(statement, "executeQuery(\"select count(*) from disableautocommit\")");
            ResultSet resultset = statement.executeQuery("select count(*) from disableautocommit");
            result(resultset);
            test(resultset, "next()");
            resultset.next();
            test(resultset, "getInt(1)");
            int i = resultset.getInt(1);
            result(i);
            assert(i == 2, "The test table must contain two rows");
            test(statement, "executeQuery(\"select * from disableautocommit\")");
            resultset = statement.executeQuery("select * from disableautocommit");
            result(resultset);
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
            if(connection != null)
                trySQL(connection, "drop table disableautocommit");
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

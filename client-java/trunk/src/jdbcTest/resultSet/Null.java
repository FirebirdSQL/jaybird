// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Null.java

package jdbcTest.resultSet;

import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class Null extends TestModule
{

    public Null()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.null");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            test(connection, "getMetaData()");
            java.sql.DatabaseMetaData databasemetadata = connection.getMetaData();
            executeSQL(connection, "create table ResultSetNull(name char(30) NOT NULL, dept char(30))");
            executeSQL(connection, "insert into ResultSetNull values ('a name', NULL)");
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery(\"select * from ResultSetNull\")");
            ResultSet resultset = statement.executeQuery("select * from ResultSetNull");
            result(resultset);
            next(resultset);
            String s = resultset.getString(1);
            assert(resultset.wasNull() ^ true, "The first column value must be not null");
            String s1 = resultset.getString(2);
            assert(resultset.wasNull(), "The second column value must be null");
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
                trySQL(connection, "drop table ResultSetNull");
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

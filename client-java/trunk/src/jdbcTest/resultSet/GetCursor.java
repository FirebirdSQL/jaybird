// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetCursor.java

package jdbcTest.resultSet;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class GetCursor extends TestModule
{

    public GetCursor()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.getObject");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(databasemetadata.supportsSelectForUpdate())
            {
                Statement statement = connection.createStatement();
                trySQL(connection, "drop table ResultSetCursor");
                statement.executeUpdate("create table ResultSetCursor(val char(30))");
                connection.setAutoCommit(false);
                statement.executeUpdate("insert into ResultSetCursor(val) values('aaa')");
                statement.executeUpdate("insert into ResultSetCursor(val) values('bbb')");
                statement.executeUpdate("insert into ResultSetCursor(val) values('ccc')");
                ResultSet resultset = statement.executeQuery("select * from ResultSetCursor for update of val");
                test(resultset, "getCursor()");
                String s = resultset.getCursorName();
                result(s);
                Statement statement1 = connection.createStatement();
                while(resultset.next())
                    if(resultset.getString(1).trim().equals("bbb"))
                        statement1.executeUpdate("update ResultSetCursor set val='ddd' where current of " + s);
                connection.commit();
                resultset = statement.executeQuery("select * from ResultSetCursor order by val");
                resultset.next();
                assert(resultset.getString(1).trim().equals("aaa"), "This must be the 'aaa' row");
                resultset.next();
                assert(resultset.getString(1).trim().equals("ccc"), "This must be the 'ccc' row");
                resultset.next();
                assert(resultset.getString(1).trim().equals("ddd"), "This must be the 'ddd' row");
            }
            connection.commit();
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            try
            {
                if(connection != null)
                {
                    trySQL(connection, "drop table ResultSetCursor");
                    connection.commit();
                    connection.close();
                }
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

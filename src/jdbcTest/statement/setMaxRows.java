// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setMaxRows.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setMaxRows extends TestModule
{

    public setMaxRows()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.SetMaxRows");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            trySQL(connection, "drop table setmaxrows");
            executeSQL(connection, "create table setmaxrows(col1 char(30))");
            for(int j = 0; j < 10; j++)
                executeSQL(connection, "insert into setmaxrows values('xxx')");

            Statement statement = connection.createStatement();
            test(statement, "setMaxRows(5)");
            statement.setMaxRows(5);
            test(statement, "getMaxRows()");
            int i = statement.getMaxRows();
            result(i);
            assert(i == 5, "Max rows should be 5");
            ResultSet resultset = statement.executeQuery("select * from setmaxrows");
            for(i = 0; resultset.next(); i++);
            assert(i == 5, "The result set should contain 5 rows");
            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace();
            failed();
        }
        finally
        {
            if(connection != null)
                trySQL(connection, "drop table setmaxrows");
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

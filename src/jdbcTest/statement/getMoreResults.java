// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getMoreResults.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getMoreResults extends TestModule
{

    public getMoreResults()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.getMoreResults");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(databasemetadata.supportsMultipleResultSets())
            {
                Statement statement = connection.createStatement();
                trySQL(connection, "drop table getmrrslts");
                executeSQL(connection, "create table getmrrslts(col1 char(30))");
                for(int j = 0; j < 10; j++)
                    executeSQL(connection, "insert into getmrrslts values('xxx')");

                test(statement, "execute(\"SELECT * FROM getmrrslts; SELECT * FROM getmrrslts; insert into getmrrslts values('xxx')\")");
                boolean flag = statement.execute("SELECT * FROM getmrrslts; SELECT * FROM getmrrslts; insert into getmrrslts values('xxx')");
                result(flag);
                assert(flag, "This must indicate the result is a ResultSet");
                ResultSet resultset = statement.getResultSet();
                assert(resultset != null, "This must be a result set");
                resultset.close();
                test(statement, "getMoreResults()");
                flag = statement.getMoreResults();
                result(flag);
                assert(flag, "This must indicate the result is a ResultSet");
                test(statement, "getMoreResults()");
                flag = statement.getMoreResults();
                result(flag);
                assert(flag ^ true, "This must indicate the result is an update count");
                test(statement, "getUpdateCount()");
                int i = statement.getUpdateCount();
                result(i);
                assert(i > -1, "The update count must be > -1");
                test(statement, "getMoreResults()");
                flag = statement.getMoreResults();
                result(flag);
                test(statement, "getUpdateCount()");
                i = statement.getUpdateCount();
                result(i);
                assert(!flag && i == -1, "There should be no more results");
            }
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
                trySQL(connection, "drop table getmrrslts");
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

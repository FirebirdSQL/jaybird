// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   RetainedResultSet.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class RetainedResultSet extends TestModule
{

    public RetainedResultSet()
    {
    }

    private boolean checkResultSet(ResultSet resultset)
    {
        try
        {
            int i = 0;
            for(int j = 0; j < 10; j++)
            {
                resultset.next();
                int k = Integer.parseInt(resultset.getString("name"));
                i += k;
            }

            if(i != 45)
                verify(false, "The ResultSet used for this test must contain the expected data");
            return true;
        }
        catch(SQLException _ex)
        {
            return false;
        }
    }

    private ResultSet createAndExecuteStatement(Connection connection)
        throws SQLException
    {
        test(connection, "createStatement()");
        Statement statement = connection.createStatement();
        String s = "select name from retainedResultSet";
        test(statement, "executeQuery(\"" + s + "\")");
        return statement.executeQuery(s);
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.RetainedResultSet");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            trySQL(connection, "drop table retainedResultSet");
            executeSQL(connection, "create table retainedResultSet (name char(30))");
            for(int i = 0; i < 10; i++)
                executeSQL(connection, "insert into retainedResultSet values('" + Integer.toString(i) + "')");

            test(connection, "getMetadata().supportsOpenCursorsAcrossCommit()");
            boolean flag = connection.getMetaData().supportsOpenCursorsAcrossCommit();
            result(flag);
            test(connection, "getMetaData().supportsOpenCursorsAcrossRollback()");
            boolean flag1 = connection.getMetaData().supportsOpenCursorsAcrossRollback();
            result(flag1);
            ResultSet resultset = createAndExecuteStatement(connection);
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            String s = "delete from retainedResultSet where name='x'";
            test(statement, "executeUpdate(\"" + s + "\")");
            try
            {
                int j = statement.executeUpdate(s);
                result(j);
                verify(j == 0, "shouldn't have any rows deleted");
            }
            catch(SQLException sqlexception)
            {
                if(flag)
                    result("The failure of this operation is unexpected.");
                else
                    result("Since this driver does not retain ResultSets across commits; and, it appears that only one active Result per connection can be used, this test failure should be ignored.");
                throw sqlexception;
            }
            boolean flag2 = checkResultSet(resultset);
            if(flag)
                verify(flag2, "The ResultSet must be retained across an auto commit");
            else
            if(flag2)
                verify(true, "Even though supportsOpenCursorsAcrossCommit is false the ResultSet has been retained across an auto commit");
            else
                verify(true, "Attempting to access the ResultSet after an auto commit produced a SQLException as expected");
            test(connection, "setAutoCommit(false)");
            connection.setAutoCommit(false);
            resultset = createAndExecuteStatement(connection);
            connection.commit();
            flag2 = checkResultSet(resultset);
            if(flag)
                verify(flag2, "The ResultSet must be retained across a commit");
            else
            if(flag2)
                verify(true, "Even though supportsOpenCursorsAcrossCommit is false the ResultSet has been retained across a commit");
            else
                verify(true, "Attempting to access the ResultSet after a commit produced a SQLException as expected");
            resultset = createAndExecuteStatement(connection);
            connection.rollback();
            flag2 = checkResultSet(resultset);
            if(flag1)
                verify(flag2, "The ResultSet must be retained across a rollback");
            else
            if(flag2)
                verify(true, "Even though supportsOpenCursorsAcrossRollback is false the ResultSet has been retained across a rollback");
            else
                verify(true, "Attempting to access the ResultSet after a rollback produced a SQLException as expected");
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            trySQL(connection, "drop table retainedResultSet");
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

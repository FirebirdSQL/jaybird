// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   RetainedStatement.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class RetainedStatement extends TestModule
{

    public RetainedStatement()
    {
    }

    private boolean checkStatement(PreparedStatement preparedstatement, String s)
    {
        try
        {
            test(preparedstatement, "setString(1,\"" + s + "\")");
            preparedstatement.setString(1, s);
            test(preparedstatement, "pstmt.executeUpdate()");
            int i = preparedstatement.executeUpdate();
            result(i);
            return true;
        }
        catch(SQLException _ex)
        {
            return false;
        }
    }

    private PreparedStatement createAndExecuteStatement(Connection connection, String s)
        throws SQLException
    {
        String s1 = "insert into retainedStatement values(?)";
        test(connection, "prepareStatement(\"" + s1 + "\")");
        PreparedStatement preparedstatement = connection.prepareStatement(s1);
        test(preparedstatement, "setString(1,\"" + s + "\")");
        preparedstatement.setString(1, s);
        test(preparedstatement, "pstmt.executeUpdate()");
        int i = preparedstatement.executeUpdate();
        result(i);
        return preparedstatement;
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.RetainedStatement");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            trySQL(connection, "drop table retainedStatement");
            executeSQL(connection, "create table retainedStatement (name varchar(30))");
            test(connection, "getMetadata().supportsOpenStatementsAcrossCommit()");
            boolean flag = connection.getMetaData().supportsOpenStatementsAcrossCommit();
            result(flag);
            test(connection, "getMetaData().supportsOpenStatementsAcrossRollback()");
            boolean flag1 = connection.getMetaData().supportsOpenStatementsAcrossRollback();
            result(flag1);
            PreparedStatement preparedstatement = createAndExecuteStatement(connection, "first");
            boolean flag2 = checkStatement(preparedstatement, "first test");
            preparedstatement.close();
            if(flag)
                verify(flag2, "The PreparedStatement must be retained across an auto commit");
            else
            if(flag2)
                verify(true, "Even though supportsOpenStatementsAcrossCommit is false the PreparedStatement has been retained across an auto commit");
            else
                verify(true, "Attempting to execute the PreparedStatement after an auto commit produced a SQLException as expected");
            test(connection, "setAutoCommit(false)");
            connection.setAutoCommit(false);
            preparedstatement = createAndExecuteStatement(connection, "second");
            connection.commit();
            flag2 = checkStatement(preparedstatement, "second test");
            preparedstatement.close();
            connection.commit();
            if(flag)
                verify(flag2, "The PreparedStatement must be retained across a commit");
            else
            if(flag2)
                verify(true, "Even though supportsOpenStatementsAcrossCommit is false the PreparedStatement has been retained across a commit");
            else
                verify(true, "Attempting to execute the PreparedStatement after a commit produced a SQLException as expected");
            preparedstatement = createAndExecuteStatement(connection, "third");
            connection.rollback();
            flag2 = checkStatement(preparedstatement, "third test");
            preparedstatement.close();
            connection.rollback();
            if(flag1)
                verify(flag2, "The PreparedStatement must be retained across a rollback");
            else
            if(flag2)
                verify(true, "Even though supportsOpenStatementsAcrossRollback is false the PreparedStatement has been retained across a rollback");
            else
                verify(true, "Attempting to execute the PreparedStatement after a rollback produced a SQLException as expected");
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            trySQL(connection, "drop table retainedStatement");
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

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setEscapeProcessing.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.*;

public class setEscapeProcessing extends TestModule
{

    public setEscapeProcessing()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.setEscapeProcessing");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            SupportedSQLType supportedsqltype = getSupportedSQLType(91);
            SupportedSQLType supportedsqltype1 = getSupportedSQLType(92);
            SupportedSQLType supportedsqltype2 = getSupportedSQLType(93);
            if(supportedsqltype != null)
            {
                trySQL(connection, "drop table escppr");
                String s = "create table escppr(adate " + supportedsqltype.getTypeName() + ")";
                executeSQL(connection, s);
                Statement statement = connection.createStatement();
                test(statement, "setEscapeProcessing(false)");
                statement.setEscapeProcessing(false);
                try
                {
                    statement.executeUpdate("insert into escppr values({d '1993-04-24'})");
                    assert(false, "Sending an escape value for date should raise an exception");
                }
                catch(SQLException _ex) { }
                test(statement, "setEscapeProcessing(true)");
                statement.setEscapeProcessing(true);
                try
                {
                    statement.executeUpdate("insert into escppr values({d '1993-04-24'})");
                }
                catch(SQLException _ex)
                {
                    assert(false, "Sending an escape value for date should be accepted");
                }
            } else
            {
                result("Date type not supported");
            }
            if(supportedsqltype2 != null)
            {
                trySQL(connection, "drop table escppr");
                String s1 = "create table escppr(atimestamp " + supportedsqltype2.getTypeName() + ")";
                executeSQL(connection, s1);
                Statement statement1 = connection.createStatement();
                test(statement1, "setEscapeProcessing(false)");
                statement1.setEscapeProcessing(false);
                try
                {
                    statement1.executeUpdate("insert into escppr values({ts '1995-04-30 23:59:59.000000'})");
                    assert(false, "Sending an escape value for timestamp should raise an exception");
                }
                catch(SQLException _ex) { }
                test(statement1, "setEscapeProcessing(true)");
                statement1.setEscapeProcessing(true);
                try
                {
                    statement1.executeUpdate("insert into escppr values({ts '1995-04-30 23:59:59.000000'})");
                }
                catch(SQLException _ex)
                {
                    assert(false, "Sending an escape value for timestamp should be accepted");
                }
            } else
            {
                result("Timestamp type not supported");
            }
            if(supportedsqltype1 != null)
            {
                trySQL(connection, "drop table escppr");
                String s2 = "create table escppr(atime " + supportedsqltype1.getTypeName() + ")";
                executeSQL(connection, s2);
                Statement statement2 = connection.createStatement();
                test(statement2, "setEscapeProcessing(false)");
                statement2.setEscapeProcessing(false);
                try
                {
                    statement2.executeUpdate("insert into escppr values({ts '23:59:59'})");
                    assert(false, "Sending an escape value for time should raise an exception");
                }
                catch(SQLException _ex) { }
                test(statement2, "setEscapeProcessing(true)");
                statement2.setEscapeProcessing(true);
                try
                {
                    statement2.executeUpdate("insert into escppr values({ts '23:59:59'})");
                }
                catch(SQLException _ex)
                {
                    assert(false, "Sending an escape value for time should be accepted");
                }
            } else
            {
                result("Time type not supported");
            }
            if(databasemetadata.supportsLikeEscapeClause())
            {
                trySQL(connection, "drop table escppr");
                String s3 = "create table escppr(achar char(2))";
                executeSQL(connection, s3);
                executeSQL(connection, "insert into escppr values('%_')");
                Statement statement3 = connection.createStatement();
                test(statement3, "setEscapeProcessing(false)");
                statement3.setEscapeProcessing(false);
                try
                {
                    statement3.executeQuery("select * from escppr where achar like '*%*_' {escape '*'}");
                    assert(false, "Sending an escape value for like escape should raise an exception");
                }
                catch(SQLException _ex) { }
                test(statement3, "setEscapeProcessing(true)");
                statement3.setEscapeProcessing(true);
                try
                {
                    statement3.executeQuery("select * from escppr where achar like '*%*_' {escape '*'}");
                }
                catch(SQLException _ex)
                {
                    assert(false, "Sending an escape value for like escape should be accepted");
                }
            } else
            {
                result("Like escape not supported");
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
            if(connection != null)
                trySQL(connection, "drop table escppr");
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

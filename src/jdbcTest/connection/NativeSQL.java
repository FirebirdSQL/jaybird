// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   NativeSQL.java

package jdbcTest.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import jdbcTest.harness.Log;
import jdbcTest.harness.SupportedSQLType;
import jdbcTest.harness.TestModule;

public class NativeSQL extends TestModule
{

    public NativeSQL()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.nativeSQL");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            SupportedSQLType supportedsqltype = getSupportedSQLType(91);
            SupportedSQLType supportedsqltype1 = getSupportedSQLType(92);
            SupportedSQLType supportedsqltype2 = getSupportedSQLType(93);
            if(supportedsqltype != null)
            {
                trySQL(connection, "drop table nativesql");
                String s = "create table nativesql(adate " + supportedsqltype.getTypeName() + ")";
                executeSQL(connection, s);
                test(connection, "nativeSQL(\"insert into nativesql values({d '1993-04-24'})\")");
                String s4 = connection.nativeSQL("insert into nativesql values({d '1993-04-24'})");
                result(s4);
                assert(s4 != null, "There should be SQL here");
                executeSQL(connection, s4);
                Statement statement = connection.createStatement();
                ResultSet resultset = statement.executeQuery("select * from nativesql");
                resultset.next();
                test(resultset, "rs.getDate(1)");
                Date date = resultset.getDate(1);
                result(date);
                statement.close();
                assert(date.equals(Date.valueOf("1993-04-24")), "The dates must match");
            } else
            {
                result("Date type not supported");
            }
            if(supportedsqltype2 != null)
            {
                trySQL(connection, "drop table nativesql");
                String s1 = "create table nativesql(atimestamp " + supportedsqltype2.getTypeName() + ")";
                executeSQL(connection, s1);
                test(connection, "nativeSQL(\"insert into nativesql values({ts '1995-04-30 23:59:59.000000'})");
                String s5 = connection.nativeSQL("insert into nativesql values({ts '1995-04-30 23:59:59.000000'})");
                result(s5);
                assert(s5 != null, "There should be SQL here");
                executeSQL(connection, s5);
                Statement statement1 = connection.createStatement();
                ResultSet resultset1 = statement1.executeQuery("select * from nativesql");
                resultset1.next();
                test(resultset1, "rs.getTimestamp(1)");
                Timestamp timestamp = resultset1.getTimestamp(1);
                result(timestamp);
                statement1.close();
                assert(timestamp.equals(Timestamp.valueOf("1995-04-30 23:59:59.000000")), "The timestamps must match");
            } else
            {
                result("Timestamp type not supported");
            }
            if(supportedsqltype1 != null)
            {
                trySQL(connection, "drop table nativesql");
                String s2 = "create table nativesql(atime " + supportedsqltype1.getTypeName() + ")";
                executeSQL(connection, s2);
                test(connection, "nativeSQL(\"insert into nativesql values({ts '23:59:59'})");
                String s6 = connection.nativeSQL("insert into nativesql values({ts '23:59:59'})");
                result(s6);
                assert(s6 != null, "There should be SQL here");
                executeSQL(connection, s6);
                Statement statement2 = connection.createStatement();
                ResultSet resultset2 = statement2.executeQuery("select * from nativesql");
                resultset2.next();
                test(resultset2, "rs.getTime(1)");
                Time time = resultset2.getTime(1);
                result(time);
                statement2.close();
                assert(time.equals(Time.valueOf("23:59:59")), "The times must match");
            } else
            {
                result("Time type not supported");
            }
            if(databasemetadata.supportsLikeEscapeClause())
            {
                trySQL(connection, "drop table nativesql");
                String s3 = "create table nativesql(achar char(2))";
                executeSQL(connection, s3);
                executeSQL(connection, "insert into nativesql values('%_')");
                test(connection, "nativeSQL(\"select * from nativesql where achar like '*%*_' {escape '*'}\")");
                String s7 = connection.nativeSQL("select * from nativesql where achar like '*%*_' {escape '*'}");
                result(s7);
                assert(s7 != null, "There should be SQL here");
                Statement statement3 = connection.createStatement();
                ResultSet resultset3 = statement3.executeQuery(s7);
                resultset3.next();
                test(resultset3, "rs.getString(1)");
                String s8 = resultset3.getString(1);
                result(s8);
                statement3.close();
                assert(s8.compareTo("%_") == 0, "The strings must match");
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
            try
            {
                if(connection != null)
                    trySQL(connection, "drop table nativesql");
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setMaxFieldSize.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.SupportedSQLType;
import jdbcTest.harness.TestModule;

// Referenced classes of package jdbcTest.preparedStatement:
//            CharInputStream

public class setMaxFieldSize extends TestModule
{

    public setMaxFieldSize()
    {
    }

    public void run()
    {
        Connection connection = null;
        String s1 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int j = 50000;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.SetMaxFieldSize");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            SupportedSQLType supportedsqltype = getSupportedSQLType(-1);
            if(supportedsqltype == null)
            {
                result("Does not support LONGVARCHAR SQL type");
            } else
            {
                trySQL(connection, "drop table setmaxfs");
                executeSQL(connection, "create table setmaxfs(achar " + supportedsqltype.getTypeName() + ")");
                int k;
                if(j > supportedsqltype.getPrecision())
                    k = supportedsqltype.getPrecision();
                else
                    k = j;
                PreparedStatement preparedstatement = connection.prepareStatement("insert into setmaxfs values(?)");
                preparedstatement.setAsciiStream(1, new CharInputStream(s1, k), k);
                preparedstatement.executeUpdate();
                preparedstatement = connection.prepareStatement("select * from setmaxfs");
                int i = Math.min(1000, supportedsqltype.getPrecision() / 2);
                try
                {
                    test(preparedstatement, "setMaxFieldSize(" + i + ")");
                    preparedstatement.setMaxFieldSize(i);
                    test(preparedstatement, "getMaxFieldSize()");
                    assert(preparedstatement.getMaxFieldSize() == i, "The max field size: " + preparedstatement.getMaxFieldSize() + " should be " + i);
                    ResultSet resultset = preparedstatement.executeQuery();
                    resultset.next();
                    String s = resultset.getString(1);
                    assert(s.length() == i, "The " + k + " char value should be truncated to " + i);
                    java.sql.SQLWarning sqlwarning = resultset.getWarnings();
                    assert(sqlwarning == null, "There should not be a DataTruncation warning");
                }
                catch(SQLException _ex)
                {
                    result("setMaxFieldSize is not supported");
                }
            }
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            if(connection != null)
                trySQL(connection, "drop table setmaxfs");
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

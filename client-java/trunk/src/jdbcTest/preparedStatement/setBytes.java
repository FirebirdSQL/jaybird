// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setBytes.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setBytes extends TestModule
{

    public setBytes()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setBytes");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            if(getSupportedSQLType(-2) == null)
            {
                result("Does not support BINARY SQL type");
            } else
            {
                preparedstatement = connection.prepareStatement("SELECT BINCOL FROM JDBCTEST WHERE INTEGERCOL=5");
                resultset = preparedstatement.executeQuery();
                resultset.next();
                byte abyte0[] = resultset.getBytes(1);
                preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE BINCOL=?");
                test(preparedstatement, "setBytes()");
                preparedstatement.setBytes(1, abyte0);
                resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int i = resultset.getInt(1);
                    result(i);
                    assert(i == 5, "The correct row is found");
                }
                preparedstatement.close();
            }
            if(getSupportedSQLType(-3) == null)
            {
                result("Does not support VARBINARY SQL type");
            } else
            {
                preparedstatement = connection.prepareStatement("SELECT VARBINCOL FROM JDBCTEST WHERE INTEGERCOL=5");
                resultset = preparedstatement.executeQuery();
                resultset.next();
                byte abyte1[] = resultset.getBytes(1);
                preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE VARBINCOL=?");
                test(preparedstatement, "setBytes()");
                preparedstatement.setBytes(1, abyte1);
                resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int j = resultset.getInt(1);
                    result(j);
                    assert(j == 5, "The correct row is found");
                }
                preparedstatement.close();
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
            try
            {
                if(preparedstatement != null)
                    preparedstatement.close();
                if(connection != null)
                    connection.close();
                if(resultset != null)
                    resultset.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

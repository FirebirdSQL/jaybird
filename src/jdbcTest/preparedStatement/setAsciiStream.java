// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setAsciiStream.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

// Referenced classes of package jdbcTest.preparedStatement:
//            AsciiInputStream

public class setAsciiStream extends TestModule
{

    public setAsciiStream()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        Object obj2 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setAsciiStream");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            PreparedStatement preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE CHARCOL=?");
            test(preparedstatement, "setAsciiStream()");
            preparedstatement.setAsciiStream(1, new AsciiInputStream(), 10);
            ResultSet resultset = preparedstatement.executeQuery();
            if(next(resultset))
            {
                int i = resultset.getInt(1);
                result(i);
                assert(i == 2, "Row 2 must be found");
            }
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
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

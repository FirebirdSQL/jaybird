// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setBinaryStream.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

// Referenced classes of package jdbcTest.preparedStatement:
//            BinaryInputStream

public class setBinaryStream extends TestModule
{

    public setBinaryStream()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setBinaryStream");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            PreparedStatement preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE BINCOL=?");
            test(preparedstatement, "setBinaryStream()");
            preparedstatement.setBinaryStream(1, new BinaryInputStream(), 6);
            ResultSet resultset = preparedstatement.executeQuery();
            if(next(resultset))
            {
                int i = resultset.getInt(1);
                result(i);
                assert(i == 5, "The correct row is found");
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

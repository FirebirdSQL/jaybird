// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioBytes.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioBytes extends TestModule
{

    public ioBytes()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setBytes");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                if(getSupportedSQLType(-2) == null)
                {
                    result("Does not support BINARY SQL type");
                } else
                {
                    PreparedStatement preparedstatement = connection.prepareStatement("SELECT BINCOL FROM JDBCTEST WHERE INTEGERCOL=5");
                    ResultSet resultset = preparedstatement.executeQuery();
                    resultset.next();
                    byte abyte0[] = resultset.getBytes(1);
                    preparedstatement.close();
                    CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_BYTES(?)}");
                    test(callablestatement, "setBytes()");
                    callablestatement.setBytes(1, abyte0);
                    callablestatement.registerOutParameter(1, -2);
                    callablestatement.executeUpdate();
                    abyte0 = callablestatement.getBytes(1);
                    result(abyte0);
                    assert(abyte0[0] == 106 && abyte0[1] == 107 && abyte0.length == 10, "The bytes output must be 0x6A6B");
                }
                if(getSupportedSQLType(-3) == null)
                {
                    result("Does not support VARBINARY SQL type");
                } else
                {
                    connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
                    PreparedStatement preparedstatement1 = connection.prepareStatement("SELECT VARBINCOL FROM JDBCTEST WHERE INTEGERCOL=5");
                    ResultSet resultset1 = preparedstatement1.executeQuery();
                    resultset1.next();
                    byte abyte1[] = resultset1.getBytes(1);
                    preparedstatement1.close();
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_IO_VBYTES(?)}");
                    test(callablestatement1, "setBytes()");
                    callablestatement1.setBytes(1, abyte1);
                    callablestatement1.registerOutParameter(1, -3);
                    callablestatement1.executeUpdate();
                    abyte1 = callablestatement1.getBytes(1);
                    result(abyte1);
                    assert(abyte1[0] == 106 && abyte1[1] == 107 && abyte1.length == 2, "The bytes output must be 0x6A6B");
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

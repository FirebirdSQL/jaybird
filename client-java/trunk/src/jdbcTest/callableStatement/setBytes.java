// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setBytes.java

package jdbcTest.callableStatement;

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
                    CallableStatement callablestatement = connection.prepareCall("{call JDBC_SET_BYTES(?,?)}");
                    test(callablestatement, "setBytes()");
                    callablestatement.setBytes(1, abyte0);
                    callablestatement.registerOutParameter(2, 4);
                    callablestatement.execute();
                    int i = callablestatement.getInt(2);
                    result(i);
                    assert(i == 5, "Row 5 should be found");
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
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_SET_VBYTES(?,?)}");
                    test(callablestatement1, "setBytes()");
                    callablestatement1.setBytes(1, abyte1);
                    callablestatement1.registerOutParameter(2, 4);
                    callablestatement1.execute();
                    int j = callablestatement1.getInt(2);
                    result(j);
                    assert(j == 5, "Row 5 should be found");
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

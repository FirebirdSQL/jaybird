// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setTimestamp.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setTimestamp extends TestModule
{

    public setTimestamp()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setTimestamp");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(93) == null)
            {
                result("Does not support TIMESTAMP SQL type");
            } else
            {
                PreparedStatement preparedstatement = connection.prepareStatement("SELECT TSCOL FROM JDBCTEST WHERE INTEGERCOL=5");
                ResultSet resultset = preparedstatement.executeQuery();
                resultset.next();
                java.sql.Timestamp timestamp = resultset.getTimestamp(1);
                result(timestamp);
                preparedstatement.close();
                callablestatement = connection.prepareCall("{call JDBC_SET_TIMESTAMP(?,?)}");
                test(callablestatement, "setTimestamp()");
                callablestatement.setTimestamp(1, timestamp);
                callablestatement.registerOutParameter(2, 4);
                callablestatement.execute();
                int i = callablestatement.getInt(2);
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
                if(callablestatement != null)
                    callablestatement.close();
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

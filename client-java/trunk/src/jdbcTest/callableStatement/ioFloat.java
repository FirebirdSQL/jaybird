// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioFloat.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioFloat extends TestModule
{

    public ioFloat()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setFloat");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(7) == null)
            {
                result("Does not support REAL SQL type");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_IO_FLOAT(?)}");
                test(callablestatement, "setFloat(1,9.424712f)");
                callablestatement.setFloat(1, 9.424712F);
                callablestatement.registerOutParameter(1, 7);
                callablestatement.executeUpdate();
                float f = callablestatement.getFloat(1);
                result(f);
                assert(f > 6.424711F && f < 6.424713F, "The output value must be ~6.424712");
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
                //wtf??if(obj != null)
                    //wtf??obj.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}

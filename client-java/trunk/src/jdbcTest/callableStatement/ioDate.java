// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioDate.java

package jdbcTest.callableStatement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import jdbcTest.harness.TestModule;

public class ioDate extends TestModule
{

    public ioDate()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setDate");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(91) == null)
            {
                result("Does not support DATE SQL type");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_IO_DATE(?)}");
                Date date = Date.valueOf("1982-03-03");
                test(callablestatement, "setDate()");
                callablestatement.setDate(1, date);
                callablestatement.registerOutParameter(1, 91);
                callablestatement.executeUpdate();
                date = callablestatement.getDate(1);
                result(date);
                assert(date.equals(Date.valueOf("1982-05-05")), "Date output value must be 1982-05-05");
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

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Conversions.java

package jdbcTest.dbmetadata;

import java.io.PrintStream;
import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class Conversions extends TestModule
{

    public Conversions()
    {
    }

    public void run()
    {
        int ai[] = {
            -7, -6, 5, 4, -5, 6, 7, 8, 2, 3,
            1, 12, -1, 91, 92, 93, -2, -3, -3
        };
        String as[] = {
            "BIT", "TINYINT", "SMALLINT", "INTEGER", "BIGINT", "FLOAT", "REAL", "DOUBLE", "NUMERIC", "DECIMAL",
            "CHAR", "VARCHAR", "LONGVARCHAR", "DATE", "TIME", "TIMESTAMP", "BINARY", "VARBINARY", "VARBINARY"
        };
        Connection connection = null;
        Object obj = null;
        boolean flag = false;
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.Supported");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            for(int i = 0; i < ai.length; i++)
            {
                for(int j = 0; j < ai.length; j++)
                    if(i != j)
                    {
                        test(databasemetadata, "supportsConvert(Types." + as[i] + "Types." + as[j] + ")");
                        boolean flag1 = databasemetadata.supportsConvert(ai[i], ai[j]);
                        Log.out.println("jdbcTest.dbmetadata.Conversions:Conversion from " + as[i] + " to " + as[j] + "is " + (!flag1 ? "not " : "") + "supported");
                    }

            }

            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception(exception1);
            failed();
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

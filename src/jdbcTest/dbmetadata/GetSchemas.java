// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetSchemas.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class GetSchemas extends TestModule
{

    public GetSchemas()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.Schema");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            try
            {
                test(databasemetadata, "getSchemas()");
                ResultSet resultset = databasemetadata.getSchemas();
                ResultSetMetaData resultsetmetadata = resultset.getMetaData();
                verify(resultsetmetadata.getColumnName(1).compareTo("TABLE_SCHEM") == 0, "ColumnName(1) must be TABLE_SCHEM not '" + resultsetmetadata.getColumnName(1) + "'");
                logResultSet(resultset);
            }
            catch(SQLException _ex)
            {
                result("getSchemas is not supported");
            }
            try
            {
                test(databasemetadata, "getCatalogs()");
                ResultSet resultset1 = databasemetadata.getCatalogs();
                ResultSetMetaData resultsetmetadata1 = resultset1.getMetaData();
                verify(resultsetmetadata1.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata1.getColumnName(1) + "'");
                logResultSet(resultset1);
            }
            catch(SQLException _ex)
            {
                result("getCatalogs is not supported");
            }
            try
            {
                test(databasemetadata, "getTableTypes()");
                ResultSet resultset2 = databasemetadata.getTableTypes();
                ResultSetMetaData resultsetmetadata2 = resultset2.getMetaData();
                verify(resultsetmetadata2.getColumnName(1).compareTo("TABLE_TYPE") == 0, "ColumnName(1) must be TABLE_TYPE not '" + resultsetmetadata2.getColumnName(1) + "'");
                logResultSet(resultset2);
            }
            catch(SQLException _ex)
            {
                result("getTableTypes is not supported");
            }
            try
            {
                test(databasemetadata, "getTypeInfo()");
                ResultSet resultset3 = databasemetadata.getTypeInfo();
                ResultSetMetaData resultsetmetadata3 = resultset3.getMetaData();
                if(resultsetmetadata3.getColumnCount() < 18)
                {
                    verify(false, "The getTypes table should contain 18 columns but only contains " + resultsetmetadata3.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata3.getColumnName(1).compareTo("TYPE_NAME") == 0, "ColumnName(1) must be TYPE_NAME not '" + resultsetmetadata3.getColumnName(1) + "'");
                    verify(resultsetmetadata3.getColumnName(2).compareTo("DATA_TYPE") == 0, "ColumnName(2) must be DATA_TYPE not '" + resultsetmetadata3.getColumnName(2) + "'");
                    verify(resultsetmetadata3.getColumnName(3).compareTo("PRECISION") == 0, "ColumnName(3) must be PRECISION not '" + resultsetmetadata3.getColumnName(3) + "'");
                    verify(resultsetmetadata3.getColumnName(4).compareTo("LITERAL_PREFIX") == 0, "ColumnName(4) must be LITERAL_PREFIX not '" + resultsetmetadata3.getColumnName(4) + "'");
                    verify(resultsetmetadata3.getColumnName(5).compareTo("LITERAL_SUFFIX") == 0, "ColumnName(5) must be LITERAL_SUFFIX not '" + resultsetmetadata3.getColumnName(5) + "'");
                    verify(resultsetmetadata3.getColumnName(6).compareTo("CREATE_PARAMS") == 0, "ColumnName(6) must be CREATE_PARAMS not '" + resultsetmetadata3.getColumnName(6) + "'");
                    verify(resultsetmetadata3.getColumnName(7).compareTo("NULLABLE") == 0, "ColumnName(7) must be NULLABLE not '" + resultsetmetadata3.getColumnName(7) + "'");
                    verify(resultsetmetadata3.getColumnName(8).compareTo("CASE_SENSITIVE") == 0, "ColumnName(8) must be CASE_SENSITIVE not '" + resultsetmetadata3.getColumnName(8) + "'");
                    verify(resultsetmetadata3.getColumnName(9).compareTo("SEARCHABLE") == 0, "ColumnName(9) must be SEARCHABLE not '" + resultsetmetadata3.getColumnName(9) + "'");
                    verify(resultsetmetadata3.getColumnName(10).compareTo("UNSIGNED_ATTRIBUTE") == 0, "ColumnName(10) must be UNSIGNED_ATTRIBUTE not '" + resultsetmetadata3.getColumnName(10) + "'");
                    verify(resultsetmetadata3.getColumnName(11).compareTo("FIXED_PREC_SCALE") == 0, "ColumnName(11) must be FIXED_PREC_SCALE not '" + resultsetmetadata3.getColumnName(11) + "'");
                    verify(resultsetmetadata3.getColumnName(12).compareTo("AUTO_INCREMENT") == 0, "ColumnName(12) must be AUTO_INCREMENT not '" + resultsetmetadata3.getColumnName(12) + "'");
                    verify(resultsetmetadata3.getColumnName(13).compareTo("LOCAL_TYPE_NAME") == 0, "ColumnName(13) must be LOCAL_TYPE_NAME not '" + resultsetmetadata3.getColumnName(13) + "'");
                    verify(resultsetmetadata3.getColumnName(14).compareTo("MINIMUM_SCALE") == 0, "ColumnName(14) must be MINIMUM_SCALE not '" + resultsetmetadata3.getColumnName(14) + "'");
                    verify(resultsetmetadata3.getColumnName(15).compareTo("MAXIMUM_SCALE") == 0, "ColumnName(15) must be MAXIMUM_SCALE not '" + resultsetmetadata3.getColumnName(15) + "'");
                    verify(resultsetmetadata3.getColumnName(18).compareTo("NUM_PREC_RADIX") == 0, "ColumnName(18) must be NUM_PREC_RADIX not '" + resultsetmetadata3.getColumnName(18) + "'");
                }
                logResultSet(resultset3);
            }
            catch(SQLException _ex)
            {
                result("getTypeInfo is not supported");
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

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetProcedures.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class GetProcedures extends TestModule
{

    public GetProcedures()
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
                test(databasemetadata, "getProcedures(null,null,\"%_SET_STR_NG\")");
                ResultSet resultset = databasemetadata.getProcedures(null, null, "%_SET_STR_NG");
                ResultSetMetaData resultsetmetadata = resultset.getMetaData();
                if(resultsetmetadata.getColumnCount() < 8)
                {
                    verify(false, "The getProcedures table should contain 8 columns but only contains " + resultsetmetadata.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata.getColumnName(1).compareTo("PROCEDURE_CAT") == 0, "ColumnName(1) must be PROCEDURE_CAT not '" + resultsetmetadata.getColumnName(1) + "'");
                    verify(resultsetmetadata.getColumnName(2).compareTo("PROCEDURE_SCHEM") == 0, "ColumnName(2) must be PROCEDURE_SCHEM not '" + resultsetmetadata.getColumnName(2) + "'");
                    verify(resultsetmetadata.getColumnName(3).compareTo("PROCEDURE_NAME") == 0, "ColumnName(3) must be PROCEDURE_NAME not '" + resultsetmetadata.getColumnName(3) + "'");
                    verify(resultsetmetadata.getColumnName(7).compareTo("REMARKS") == 0, "ColumnName(7) must be REMARKS not '" + resultsetmetadata.getColumnName(7) + "'");
                    verify(resultsetmetadata.getColumnName(8).compareTo("PROCEDURE_TYPE") == 0, "ColumnName(8) must be PROCEDURE_TYPE not '" + resultsetmetadata.getColumnName(8) + "'");
                }
                if(next(resultset))
                {
                    String s = resultset.getString(3);
                    if(s.length() > 15)
                        s = s.substring(0, 15);
                    verify(s.equalsIgnoreCase("JDBC_SET_STRING"), "The JDBC_SET_STRING must be found");
                    resultset.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getProcedures is not supported");
            }
            try
            {
                test(databasemetadata, "getProcedureColumns(null,null,\"%_SET_STR_NG\",\"\")");
                ResultSet resultset1 = databasemetadata.getProcedureColumns(null, null, "%_SET_STR_NG", "%");
                ResultSetMetaData resultsetmetadata1 = resultset1.getMetaData();
                if(resultsetmetadata1.getColumnCount() < 13)
                {
                    verify(false, "The getProcedureColumns table should contain13  columns but only contains " + resultsetmetadata1.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata1.getColumnName(1).compareTo("PROCEDURE_CAT") == 0, "ColumnName(1) must be PROCEDURE_CAT not '" + resultsetmetadata1.getColumnName(1) + "'");
                    verify(resultsetmetadata1.getColumnName(2).compareTo("PROCEDURE_SCHEM") == 0, "ColumnName(2) must be PROCEDURE_SCHEM not '" + resultsetmetadata1.getColumnName(2) + "'");
                    verify(resultsetmetadata1.getColumnName(3).compareTo("PROCEDURE_NAME") == 0, "ColumnName(3) must be PROCEDURE_NAME not '" + resultsetmetadata1.getColumnName(3) + "'");
                    verify(resultsetmetadata1.getColumnName(4).compareTo("COLUMN_NAME") == 0, "ColumnName(4) must be COLUMN_NAME not '" + resultsetmetadata1.getColumnName(4) + "'");
                    verify(resultsetmetadata1.getColumnName(5).compareTo("COLUMN_TYPE") == 0, "ColumnName(5) must be COLUMN_TYPE not '" + resultsetmetadata1.getColumnName(5) + "'");
                    verify(resultsetmetadata1.getColumnName(6).compareTo("DATA_TYPE") == 0, "ColumnName(6) must be DATA_TYPE not '" + resultsetmetadata1.getColumnName(6) + "'");
                    verify(resultsetmetadata1.getColumnName(7).compareTo("TYPE_NAME") == 0, "ColumnName(7) must be TYPE_NAME not '" + resultsetmetadata1.getColumnName(7) + "'");
                    verify(resultsetmetadata1.getColumnName(8).compareTo("PRECISION") == 0, "ColumnName(8) must be PRECISION not '" + resultsetmetadata1.getColumnName(8) + "'");
                    verify(resultsetmetadata1.getColumnName(9).compareTo("LENGTH") == 0, "ColumnName(9) must be LENGTH not '" + resultsetmetadata1.getColumnName(9) + "'");
                    verify(resultsetmetadata1.getColumnName(10).compareTo("SCALE") == 0, "ColumnName(10) must be SCALE not '" + resultsetmetadata1.getColumnName(10) + "'");
                    verify(resultsetmetadata1.getColumnName(11).compareTo("RADIX") == 0, "ColumnName(11) must be RADIX not '" + resultsetmetadata1.getColumnName(11) + "'");
                    verify(resultsetmetadata1.getColumnName(12).compareTo("NULLABLE") == 0, "ColumnName(12) must be NULLABLE not '" + resultsetmetadata1.getColumnName(12) + "'");
                    verify(resultsetmetadata1.getColumnName(13).compareTo("REMARKS") == 0, "ColumnName(13) must be REMARKS not '" + resultsetmetadata1.getColumnName(13) + "'");
                }
                if(next(resultset1))
                {
                    if(resultset1.getInt(5) == 5)
                        next(resultset1);
                    String s1 = resultset1.getString(4);
                    verify(s1.equalsIgnoreCase("CCOL") || s1.equalsIgnoreCase("@CCOL"), "The JDBC_SET_STRING parameter named CCOL or @CCOL must be found");
                    resultset1.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getProcedureColumns is not supported");
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

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetColumns.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;
import jdbcTest.harness.TestTable;

public class GetColumns extends TestModule
{

    public GetColumns()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.Schema");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            String s = databasemetadata.getIdentifierQuoteString();
            TestTable testtable = new TestTable(s + "JDBCSCHEM" + s, 5, connection);
            try
            {
                test(databasemetadata, "getColumns(null,null,\"JDB%HE_\",\"VAR%\")");
                ResultSet resultset = databasemetadata.getColumns(null, null, "JDB%HE_", "VAR%");
                ResultSetMetaData resultsetmetadata = resultset.getMetaData();
                if(resultsetmetadata.getColumnCount() < 18)
                {
                    verify(false, "The getColumns table should contain 18 columns but only contains " + resultsetmetadata.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata.getColumnName(1) + "'");
                    verify(resultsetmetadata.getColumnName(2).compareTo("TABLE_SCHEM") == 0, "ColumnName(2) must be TABLE_SCHEM not '" + resultsetmetadata.getColumnName(2) + "'");
                    verify(resultsetmetadata.getColumnName(3).compareTo("TABLE_NAME") == 0, "ColumnName(3) must be TABLE_NAME not '" + resultsetmetadata.getColumnName(3) + "'");
                    verify(resultsetmetadata.getColumnName(4).compareTo("COLUMN_NAME") == 0, "ColumnName(4) must be COLUMN_NAME not '" + resultsetmetadata.getColumnName(4) + "'");
                    verify(resultsetmetadata.getColumnName(5).compareTo("DATA_TYPE") == 0, "ColumnName(5) must be DATA_TYPE not '" + resultsetmetadata.getColumnName(5) + "'");
                    verify(resultsetmetadata.getColumnName(6).compareTo("TYPE_NAME") == 0, "ColumnName(6) must be TYPE_NAME not '" + resultsetmetadata.getColumnName(6) + "'");
                    verify(resultsetmetadata.getColumnName(7).compareTo("COLUMN_SIZE") == 0, "ColumnName(7) must be COLUMN_SIZE not '" + resultsetmetadata.getColumnName(7) + "'");
                    verify(resultsetmetadata.getColumnName(8).compareTo("BUFFER_LENGTH") == 0, "ColumnName(8) must be BUFFER_LENGTH not '" + resultsetmetadata.getColumnName(8) + "'");
                    verify(resultsetmetadata.getColumnName(9).compareTo("DECIMAL_DIGITS") == 0, "ColumnName(9) must be DECIMAL_DIGITS not '" + resultsetmetadata.getColumnName(9) + "'");
                    verify(resultsetmetadata.getColumnName(10).compareTo("NUM_PREC_RADIX") == 0, "ColumnName(10) must be NUM_PREC_RADIX not '" + resultsetmetadata.getColumnName(10) + "'");
                    verify(resultsetmetadata.getColumnName(11).compareTo("NULLABLE") == 0, "ColumnName(11) must be NULLABLE not '" + resultsetmetadata.getColumnName(11) + "'");
                    verify(resultsetmetadata.getColumnName(12).compareTo("REMARKS") == 0, "ColumnName(12) must be REMARKS not '" + resultsetmetadata.getColumnName(12) + "'");
                    verify(resultsetmetadata.getColumnName(13).compareTo("COLUMN_DEF") == 0, "ColumnName(13) must be COLUMN_DEF not '" + resultsetmetadata.getColumnName(13) + "'");
                    verify(resultsetmetadata.getColumnName(14).compareTo("SQL_DATA_TYPE") == 0, "ColumnName(14) must be SQL_DATA_TYPE not '" + resultsetmetadata.getColumnName(14) + "'");
                    verify(resultsetmetadata.getColumnName(15).compareTo("SQL_DATETIME_SUB") == 0, "ColumnName(15) must be SQL_DATETIME_SUB not '" + resultsetmetadata.getColumnName(15) + "'");
                    verify(resultsetmetadata.getColumnName(16).compareTo("CHAR_OCTET_LENGTH") == 0, "ColumnName(16) must be CHAR_OCTET_LENGTH not '" + resultsetmetadata.getColumnName(16) + "'");
                    verify(resultsetmetadata.getColumnName(17).compareTo("ORDINAL_POSITION") == 0, "ColumnName(17) must be ORDINAL_POSITION not '" + resultsetmetadata.getColumnName(17) + "'");
                    verify(resultsetmetadata.getColumnName(18).compareTo("IS_NULLABLE") == 0, "ColumnName(18) must be IS_NULLABLE not '" + resultsetmetadata.getColumnName(18) + "'");
                }
                if(next(resultset))
                {
                    String s1 = resultset.getString(3);
                    verify(s1.equalsIgnoreCase("JDBCSCHEM"), "Columns for the table named JDBCSCHEM must be found");
                    resultset.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getColumns is not supported");
            }
            try
            {
                test(databasemetadata, "getColumnPrivileges(null,null,\"JDBCSCHEM\",\"VAR%\")");
                ResultSet resultset1 = databasemetadata.getColumnPrivileges(null, null, "JDBCSCHEM", "VAR%");
                ResultSetMetaData resultsetmetadata1 = resultset1.getMetaData();
                if(resultsetmetadata1.getColumnCount() < 8)
                {
                    verify(false, "The getColumnPrivileges table should contain 8 columns but only contains " + resultsetmetadata1.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata1.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata1.getColumnName(1) + "'");
                    verify(resultsetmetadata1.getColumnName(2).compareTo("TABLE_SCHEM") == 0, "ColumnName(2) must be TABLE_SCHEM not '" + resultsetmetadata1.getColumnName(2) + "'");
                    verify(resultsetmetadata1.getColumnName(3).compareTo("TABLE_NAME") == 0, "ColumnName(3) must be TABLE_NAME not '" + resultsetmetadata1.getColumnName(3) + "'");
                    verify(resultsetmetadata1.getColumnName(4).compareTo("COLUMN_NAME") == 0, "ColumnName(4) must be COLUMN_NAME not '" + resultsetmetadata1.getColumnName(4) + "'");
                    verify(resultsetmetadata1.getColumnName(5).compareTo("GRANTOR") == 0, "ColumnName(5) must be GRANTOR not '" + resultsetmetadata1.getColumnName(5) + "'");
                    verify(resultsetmetadata1.getColumnName(6).compareTo("GRANTEE") == 0, "ColumnName(6) must be GRANTEE not '" + resultsetmetadata1.getColumnName(6) + "'");
                    verify(resultsetmetadata1.getColumnName(7).compareTo("PRIVILEGE") == 0, "ColumnName(7) must be PRIVILEGE not '" + resultsetmetadata1.getColumnName(7) + "'");
                    verify(resultsetmetadata1.getColumnName(8).compareTo("IS_GRANTABLE") == 0, "ColumnName(8) must be IS_GRANTABLE not '" + resultsetmetadata1.getColumnName(8) + "'");
                }
                if(next(resultset1))
                {
                    String s2 = resultset1.getString(3);
                    verify(s2.equalsIgnoreCase("JDBCSCHEM"), "Column priviledges for the table named JDBCSCHEM must be found");
                    resultset1.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getColumnPrivileges is not supported");
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

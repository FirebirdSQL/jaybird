// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetTables.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;
import jdbcTest.harness.TestTable;

public class GetTables extends TestModule
{

    public GetTables()
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
                test(databasemetadata, "getTables(null,null,\"JDB%HE_\",null)");
                ResultSet resultset = databasemetadata.getTables(null, null, "JDB%HE_", null);
                ResultSetMetaData resultsetmetadata = resultset.getMetaData();
                if(resultsetmetadata.getColumnCount() < 5)
                {
                    verify(false, "The getTables table should contain 5 columns but only contains " + resultsetmetadata.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata.getColumnName(1) + "'");
                    verify(resultsetmetadata.getColumnName(2).compareTo("TABLE_SCHEM") == 0, "ColumnName(2) must be TABLE_SCHEM not '" + resultsetmetadata.getColumnName(2) + "'");
                    verify(resultsetmetadata.getColumnName(3).compareTo("TABLE_NAME") == 0, "ColumnName(3) must be TABLE_NAME not '" + resultsetmetadata.getColumnName(3) + "'");
                    verify(resultsetmetadata.getColumnName(4).compareTo("TABLE_TYPE") == 0, "ColumnName(4) must be TABLE_TYPE not '" + resultsetmetadata.getColumnName(4) + "'");
                    verify(resultsetmetadata.getColumnName(5).compareTo("REMARKS") == 0, "ColumnName(5) must be REMARKS not '" + resultsetmetadata.getColumnName(5) + "'");
                }
                if(next(resultset))
                {
                    String s1 = resultset.getString(3);
                    verify(s1.equalsIgnoreCase("JDBCSCHEM"), "The table named JDBCSCHEM must be found");
                    resultset.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getTables is not supported");
            }
            try
            {
                test(databasemetadata, "getTablePrivileges(null,null,\"JDB%HE_\")");
                ResultSet resultset1 = databasemetadata.getTablePrivileges(null, null, "JDB%HE_");
                ResultSetMetaData resultsetmetadata1 = resultset1.getMetaData();
                if(resultsetmetadata1.getColumnCount() < 7)
                {
                    verify(false, "The getTablePrivileges table should contain 7 columns but only contains " + resultsetmetadata1.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata1.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata1.getColumnName(1) + "'");
                    verify(resultsetmetadata1.getColumnName(2).compareTo("TABLE_SCHEM") == 0, "ColumnName(2) must be TABLE_SCHEM not '" + resultsetmetadata1.getColumnName(2) + "'");
                    verify(resultsetmetadata1.getColumnName(3).compareTo("TABLE_NAME") == 0, "ColumnName(3) must be TABLE_NAME not '" + resultsetmetadata1.getColumnName(3) + "'");
                    verify(resultsetmetadata1.getColumnName(4).compareTo("GRANTOR") == 0, "ColumnName(4) must be GRANTOR not '" + resultsetmetadata1.getColumnName(4) + "'");
                    verify(resultsetmetadata1.getColumnName(5).compareTo("GRANTEE") == 0, "ColumnName(5) must be GRANTEE not '" + resultsetmetadata1.getColumnName(5) + "'");
                    verify(resultsetmetadata1.getColumnName(6).compareTo("PRIVILEGE") == 0, "ColumnName(6) must be PRIVILEGE not '" + resultsetmetadata1.getColumnName(6) + "'");
                    verify(resultsetmetadata1.getColumnName(7).compareTo("IS_GRANTABLE") == 0, "ColumnName(7) must be IS_GRANTABLE not '" + resultsetmetadata1.getColumnName(7) + "'");
                }
                if(next(resultset1))
                {
                    String s2 = resultset1.getString(3);
                    verify(s2.equalsIgnoreCase("JDBCSCHEM"), "The table privelidges for JDBCSCHEM must be found");
                    resultset1.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getTablePrivileges is not supported");
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

// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Keys.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class Keys extends TestModule
{

    public Keys()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        boolean flag = false;
        Object obj1 = null;
        String s = "";
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.Keys");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            String s2 = databasemetadata.getIdentifierQuoteString();
            trySQL("drop table JDBCKEY2");
            trySQL("drop table JDBCKEY1");
            String s1 = "create table " + s2 + "JDBCKEY1" + s2 + " (VAR1 char(5) not null ,VAR2 char(5), primary key (VAR1))";
            executeSQL(s1);
            s1 = "create table " + s2 + "JDBCKEY2" + s2 + " (VAR3 char(5) not null ,VAR4 char(5), primary key (VAR3), foreign key (VAR4) references JDBCKEY1)";
            executeSQL(s1);
            try
            {
                test(databasemetadata, "getBestRowIdentifier(null,null,\"JDBCKEY1\",dbm.bestRowSession,false)");
                ResultSet resultset = databasemetadata.getBestRowIdentifier(null, null, "JDBCKEY1", 2, false);
                ResultSetMetaData resultsetmetadata = resultset.getMetaData();
                if(resultsetmetadata.getColumnCount() < 8)
                {
                    verify(false, "The getBestRowIdentifier table should contain 8 columns but only contains " + resultsetmetadata.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata.getColumnName(1).compareTo("SCOPE") == 0, "ColumnName(1) must be SCOPE not '" + resultsetmetadata.getColumnName(1) + "'");
                    verify(resultsetmetadata.getColumnName(2).compareTo("COLUMN_NAME") == 0, "ColumnName(2) must be COLUMN_NAME not '" + resultsetmetadata.getColumnName(2) + "'");
                    verify(resultsetmetadata.getColumnName(3).compareTo("DATA_TYPE") == 0, "ColumnName(3) must be DATA_TYPE not '" + resultsetmetadata.getColumnName(3) + "'");
                    verify(resultsetmetadata.getColumnName(4).compareTo("TYPE_NAME") == 0, "ColumnName(4) must be TYPE_NAME not '" + resultsetmetadata.getColumnName(4) + "'");
                    verify(resultsetmetadata.getColumnName(5).compareTo("COLUMN_SIZE") == 0, "ColumnName(5) must be COLUMN_SIZE not '" + resultsetmetadata.getColumnName(5) + "'");
                    verify(resultsetmetadata.getColumnName(6).compareTo("BUFFER_LENGTH") == 0, "ColumnName(6) must be BUFFER_LENGTH not '" + resultsetmetadata.getColumnName(6) + "'");
                    verify(resultsetmetadata.getColumnName(7).compareTo("DECIMAL_DIGITS") == 0, "ColumnName(7) must be DECIMAL_DIGITS not '" + resultsetmetadata.getColumnName(7) + "'");
                    verify(resultsetmetadata.getColumnName(8).compareTo("PSEUDO_COLUMN") == 0, "ColumnName(8) must be PSEUDO_COLUMN not '" + resultsetmetadata.getColumnName(8) + "'");
                }
                if(next(resultset))
                {
                    String s3 = resultset.getString(2);
                    result(s3);
                    verify(s3.equalsIgnoreCase("VAR1"), "The best row identifier for JDBCKEY1 must be VAR1");
                    resultset.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getBestRowIdentifier is not supported");
            }
            try
            {
                test(databasemetadata, "getVersionColumns(null,null,\"JDBCKEY1\")");
                ResultSet resultset1 = databasemetadata.getVersionColumns(null, null, "JDBCKEY1");
                logResultSet(resultset1);
                ResultSetMetaData resultsetmetadata1 = resultset1.getMetaData();
                if(resultsetmetadata1.getColumnCount() < 8)
                {
                    verify(false, "The getVersionColumns table should contain 8 columns but only contains " + resultsetmetadata1.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata1.getColumnName(1).compareTo("SCOPE") == 0, "ColumnName(1) must be SCOPE not '" + resultsetmetadata1.getColumnName(1) + "'");
                    verify(resultsetmetadata1.getColumnName(2).compareTo("COLUMN_NAME") == 0, "ColumnName(2) must be COLUMN_NAME not '" + resultsetmetadata1.getColumnName(2) + "'");
                    verify(resultsetmetadata1.getColumnName(3).compareTo("DATA_TYPE") == 0, "ColumnName(3) must be DATA_TYPE not '" + resultsetmetadata1.getColumnName(3) + "'");
                    verify(resultsetmetadata1.getColumnName(4).compareTo("TYPE_NAME") == 0, "ColumnName(4) must be TYPE_NAME not '" + resultsetmetadata1.getColumnName(4) + "'");
                    verify(resultsetmetadata1.getColumnName(5).compareTo("COLUMN_SIZE") == 0, "ColumnName(5) must be COLUMN_SIZE not '" + resultsetmetadata1.getColumnName(5) + "'");
                    verify(resultsetmetadata1.getColumnName(6).compareTo("BUFFER_LENGTH") == 0, "ColumnName(6) must be BUFFER_LENGTH not '" + resultsetmetadata1.getColumnName(6) + "'");
                    verify(resultsetmetadata1.getColumnName(7).compareTo("DECIMAL_DIGITS") == 0, "ColumnName(7) must be DECIMAL_DIGITS not '" + resultsetmetadata1.getColumnName(7) + "'");
                    verify(resultsetmetadata1.getColumnName(8).compareTo("PSEUDO_COLUMN") == 0, "ColumnName(8) must be PSEUDO_COLUMN not '" + resultsetmetadata1.getColumnName(8) + "'");
                }
            }
            catch(SQLException _ex)
            {
                result("getVersionColumns is not supported");
            }
            try
            {
                test(databasemetadata, "getPrimaryKeys(null,null,\"JDBCKEY1\")");
                ResultSet resultset2 = databasemetadata.getPrimaryKeys(null, null, "JDBCKEY1");
                ResultSetMetaData resultsetmetadata2 = resultset2.getMetaData();
                if(resultsetmetadata2.getColumnCount() < 6)
                {
                    verify(false, "The getPrimaryKeys table should contain 6 columns but only contains " + resultsetmetadata2.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata2.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata2.getColumnName(1) + "'");
                    verify(resultsetmetadata2.getColumnName(2).compareTo("TABLE_SCHEM") == 0, "ColumnName(2) must be TABLE_SCHEM not '" + resultsetmetadata2.getColumnName(2) + "'");
                    verify(resultsetmetadata2.getColumnName(3).compareTo("TABLE_NAME") == 0, "ColumnName(3) must be TABLE_NAME not '" + resultsetmetadata2.getColumnName(3) + "'");
                    verify(resultsetmetadata2.getColumnName(4).compareTo("COLUMN_NAME") == 0, "ColumnName(4) must be COLUMN_NAME not '" + resultsetmetadata2.getColumnName(4) + "'");
                    verify(resultsetmetadata2.getColumnName(5).compareTo("KEY_SEQ") == 0, "ColumnName(5) must be KEY_SEQ not '" + resultsetmetadata2.getColumnName(5) + "'");
                    verify(resultsetmetadata2.getColumnName(6).compareTo("PK_NAME") == 0, "ColumnName(6) must be PK_NAME not '" + resultsetmetadata2.getColumnName(6) + "'");
                }
                if(next(resultset2))
                {
                    String s4 = resultset2.getString(4);
                    result(s4);
                    verify(s4.equalsIgnoreCase("VAR1"), "The primary key for JDBCKEY1 must be VAR1");
                    resultset2.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getPrimaryKeys is not supported");
            }
            try
            {
                test(databasemetadata, "getImportedKeys(null,null,\"JDBCKEY2\")");
                ResultSet resultset3 = databasemetadata.getImportedKeys(null, null, "JDBCKEY2");
                ResultSetMetaData resultsetmetadata3 = resultset3.getMetaData();
                if(resultsetmetadata3.getColumnCount() < 14)
                {
                    verify(false, "The getImportedKeys table should contain 14 columns but only contains " + resultsetmetadata3.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata3.getColumnName(1).compareTo("PKTABLE_CAT") == 0, "ColumnName(1) must be PKTABLE_CAT not '" + resultsetmetadata3.getColumnName(1) + "'");
                    verify(resultsetmetadata3.getColumnName(2).compareTo("PKTABLE_SCHEM") == 0, "ColumnName(2) must be PKTABLE_SCHEM not '" + resultsetmetadata3.getColumnName(2) + "'");
                    verify(resultsetmetadata3.getColumnName(3).compareTo("PKTABLE_NAME") == 0, "ColumnName(3) must be PKTABLE_NAME not '" + resultsetmetadata3.getColumnName(3) + "'");
                    verify(resultsetmetadata3.getColumnName(4).compareTo("PKCOLUMN_NAME") == 0, "ColumnName(4) must be PKCOLUMN_NAME not '" + resultsetmetadata3.getColumnName(4) + "'");
                    verify(resultsetmetadata3.getColumnName(5).compareTo("FKTABLE_CAT") == 0, "ColumnName(5) must be FKTABLE_CAT not '" + resultsetmetadata3.getColumnName(5) + "'");
                    verify(resultsetmetadata3.getColumnName(6).compareTo("FKTABLE_SCHEM") == 0, "ColumnName(6) must be FKTABLE_SCHEM not '" + resultsetmetadata3.getColumnName(6) + "'");
                    verify(resultsetmetadata3.getColumnName(7).compareTo("FKTABLE_NAME") == 0, "ColumnName(7) must be FKTABLE_NAME not '" + resultsetmetadata3.getColumnName(7) + "'");
                    verify(resultsetmetadata3.getColumnName(8).compareTo("FKCOLUMN_NAME") == 0, "ColumnName(8) must be FKCOLUMN_NAME not '" + resultsetmetadata3.getColumnName(8) + "'");
                    verify(resultsetmetadata3.getColumnName(9).compareTo("KEY_SEQ") == 0, "ColumnName(9) must be KEY_SEQ not '" + resultsetmetadata3.getColumnName(9) + "'");
                    verify(resultsetmetadata3.getColumnName(10).compareTo("UPDATE_RULE") == 0, "ColumnName(10) must be UPDATE_RULE not '" + resultsetmetadata3.getColumnName(10) + "'");
                    verify(resultsetmetadata3.getColumnName(11).compareTo("DELETE_RULE") == 0, "ColumnName(11) must be DELETE_RULE not '" + resultsetmetadata3.getColumnName(11) + "'");
                    verify(resultsetmetadata3.getColumnName(12).compareTo("FK_NAME") == 0, "ColumnName(12) must be FK_NAME not '" + resultsetmetadata3.getColumnName(12) + "'");
                    verify(resultsetmetadata3.getColumnName(13).compareTo("PK_NAME") == 0, "ColumnName(13) must be PK_NAME not '" + resultsetmetadata3.getColumnName(13) + "'");
                    verify(resultsetmetadata3.getColumnName(14).compareTo("DEFERRABILITY") == 0, "ColumnName(14) must be DEFERRABILITY not '" + resultsetmetadata3.getColumnName(14) + "'");
                }
                if(next(resultset3))
                {
                    String s5 = resultset3.getString(3);
                    result(s5);
                    verify(s5.equalsIgnoreCase("JDBCKEY1"), "Table JDBCKEY1 exports a key to JDBCKEY2");
                    String s8 = resultset3.getString(4);
                    result(s8);
                    verify(s8.equalsIgnoreCase("VAR1"), "Table JDBCKEY exports the VAR1 key to JDBCKEY2");
                    resultset3.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getImportedKeys is not supported");
            }
            try
            {
                test(databasemetadata, "getExportedKeys(null,null,\"JDBCKEY1\")");
                ResultSet resultset4 = databasemetadata.getExportedKeys(null, null, "JDBCKEY1");
                ResultSetMetaData resultsetmetadata4 = resultset4.getMetaData();
                if(resultsetmetadata4.getColumnCount() < 14)
                {
                    verify(false, "The getExportedKeys table should contain 14 columns but only contains " + resultsetmetadata4.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata4.getColumnName(1).compareTo("PKTABLE_CAT") == 0, "ColumnName(1) must be PKTABLE_CAT not '" + resultsetmetadata4.getColumnName(1) + "'");
                    verify(resultsetmetadata4.getColumnName(2).compareTo("PKTABLE_SCHEM") == 0, "ColumnName(2) must be PKTABLE_SCHEM not '" + resultsetmetadata4.getColumnName(2) + "'");
                    verify(resultsetmetadata4.getColumnName(3).compareTo("PKTABLE_NAME") == 0, "ColumnName(3) must be PKTABLE_NAME not '" + resultsetmetadata4.getColumnName(3) + "'");
                    verify(resultsetmetadata4.getColumnName(4).compareTo("PKCOLUMN_NAME") == 0, "ColumnName(4) must be PKCOLUMN_NAME not '" + resultsetmetadata4.getColumnName(4) + "'");
                    verify(resultsetmetadata4.getColumnName(5).compareTo("FKTABLE_CAT") == 0, "ColumnName(5) must be FKTABLE_CAT not '" + resultsetmetadata4.getColumnName(5) + "'");
                    verify(resultsetmetadata4.getColumnName(6).compareTo("FKTABLE_SCHEM") == 0, "ColumnName(6) must be FKTABLE_SCHEM not '" + resultsetmetadata4.getColumnName(6) + "'");
                    verify(resultsetmetadata4.getColumnName(7).compareTo("FKTABLE_NAME") == 0, "ColumnName(7) must be FKTABLE_NAME not '" + resultsetmetadata4.getColumnName(7) + "'");
                    verify(resultsetmetadata4.getColumnName(8).compareTo("FKCOLUMN_NAME") == 0, "ColumnName(8) must be FKCOLUMN_NAME not '" + resultsetmetadata4.getColumnName(8) + "'");
                    verify(resultsetmetadata4.getColumnName(9).compareTo("KEY_SEQ") == 0, "ColumnName(9) must be KEY_SEQ not '" + resultsetmetadata4.getColumnName(9) + "'");
                    verify(resultsetmetadata4.getColumnName(10).compareTo("UPDATE_RULE") == 0, "ColumnName(10) must be UPDATE_RULE not '" + resultsetmetadata4.getColumnName(10) + "'");
                    verify(resultsetmetadata4.getColumnName(11).compareTo("DELETE_RULE") == 0, "ColumnName(11) must be DELETE_RULE not '" + resultsetmetadata4.getColumnName(11) + "'");
                    verify(resultsetmetadata4.getColumnName(12).compareTo("FK_NAME") == 0, "ColumnName(12) must be FK_NAME not '" + resultsetmetadata4.getColumnName(12) + "'");
                    verify(resultsetmetadata4.getColumnName(13).compareTo("PK_NAME") == 0, "ColumnName(13) must be PK_NAME not '" + resultsetmetadata4.getColumnName(13) + "'");
                    verify(resultsetmetadata4.getColumnName(14).compareTo("DEFERRABILITY") == 0, "ColumnName(14) must be DEFERRABILITY not '" + resultsetmetadata4.getColumnName(14) + "'");
                }
                if(next(resultset4))
                {
                    String s6 = resultset4.getString(7);
                    result(s6);
                    verify(s6.equalsIgnoreCase("JDBCKEY2"), "Table JDBCKEY2 imports a key from JDBCKEY1");
                    String s9 = resultset4.getString(8);
                    result(s9);
                    verify(s9.equalsIgnoreCase("VAR4"), "Table JDBCKEY2 imports the VAR4 key from JDBCKEY1");
                    resultset4.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getExportedKeys is not supported");
            }
            try
            {
                test(databasemetadata, "getCrossReference(null,null,\"JDBCKEY1\",null,null,\"JDBCKEY2\")");
                ResultSet resultset5 = databasemetadata.getCrossReference(null, null, "JDBCKEY1", null, null, "JDBCKEY2");
                ResultSetMetaData resultsetmetadata5 = resultset5.getMetaData();
                if(resultsetmetadata5.getColumnCount() < 14)
                {
                    verify(false, "The getCrossReference table should contain 14 columns but only contains " + resultsetmetadata5.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata5.getColumnName(1).compareTo("PKTABLE_CAT") == 0, "ColumnName(1) must be PKTABLE_CAT not '" + resultsetmetadata5.getColumnName(1) + "'");
                    verify(resultsetmetadata5.getColumnName(2).compareTo("PKTABLE_SCHEM") == 0, "ColumnName(2) must be PKTABLE_SCHEM not '" + resultsetmetadata5.getColumnName(2) + "'");
                    verify(resultsetmetadata5.getColumnName(3).compareTo("PKTABLE_NAME") == 0, "ColumnName(3) must be PKTABLE_NAME not '" + resultsetmetadata5.getColumnName(3) + "'");
                    verify(resultsetmetadata5.getColumnName(4).compareTo("PKCOLUMN_NAME") == 0, "ColumnName(4) must be PKCOLUMN_NAME not '" + resultsetmetadata5.getColumnName(4) + "'");
                    verify(resultsetmetadata5.getColumnName(5).compareTo("FKTABLE_CAT") == 0, "ColumnName(5) must be FKTABLE_CAT not '" + resultsetmetadata5.getColumnName(5) + "'");
                    verify(resultsetmetadata5.getColumnName(6).compareTo("FKTABLE_SCHEM") == 0, "ColumnName(6) must be FKTABLE_SCHEM not '" + resultsetmetadata5.getColumnName(6) + "'");
                    verify(resultsetmetadata5.getColumnName(7).compareTo("FKTABLE_NAME") == 0, "ColumnName(7) must be FKTABLE_NAME not '" + resultsetmetadata5.getColumnName(7) + "'");
                    verify(resultsetmetadata5.getColumnName(8).compareTo("FKCOLUMN_NAME") == 0, "ColumnName(8) must be FKCOLUMN_NAME not '" + resultsetmetadata5.getColumnName(8) + "'");
                    verify(resultsetmetadata5.getColumnName(9).compareTo("KEY_SEQ") == 0, "ColumnName(9) must be KEY_SEQ not '" + resultsetmetadata5.getColumnName(9) + "'");
                    verify(resultsetmetadata5.getColumnName(10).compareTo("UPDATE_RULE") == 0, "ColumnName(10) must be UPDATE_RULE not '" + resultsetmetadata5.getColumnName(10) + "'");
                    verify(resultsetmetadata5.getColumnName(11).compareTo("DELETE_RULE") == 0, "ColumnName(11) must be DELETE_RULE not '" + resultsetmetadata5.getColumnName(11) + "'");
                    verify(resultsetmetadata5.getColumnName(12).compareTo("FK_NAME") == 0, "ColumnName(12) must be FK_NAME not '" + resultsetmetadata5.getColumnName(12) + "'");
                    verify(resultsetmetadata5.getColumnName(13).compareTo("PK_NAME") == 0, "ColumnName(13) must be PK_NAME not '" + resultsetmetadata5.getColumnName(13) + "'");
                    verify(resultsetmetadata5.getColumnName(14).compareTo("DEFERRABILITY") == 0, "ColumnName(14) must be DEFERRABILITY not '" + resultsetmetadata5.getColumnName(14) + "'");
                }
                if(next(resultset5))
                {
                    String s7 = resultset5.getString(3);
                    result(s7);
                    verify(s7.equalsIgnoreCase("JDBCKEY1"), "Table JDBCKEY1 exports a key to JDBCKEY2");
                    String s10 = resultset5.getString(4);
                    result(s10);
                    verify(s10.equalsIgnoreCase("VAR1"), "Table JDBCKEY1 exports the VAR1 key to JDBCKEY2");
                    String s11 = resultset5.getString(7);
                    result(s11);
                    verify(s11.equalsIgnoreCase("JDBCKEY2"), "Table JDBCKEY2 imports a key from JDBCKEY1");
                    String s12 = resultset5.getString(8);
                    result(s12);
                    verify(s12.equalsIgnoreCase("VAR4"), "Table JDBCKEY2 imports the VAR4 key from JDBCKEY1");
                    resultset5.close();
                }
            }
            catch(SQLException _ex)
            {
                result("getCrossReference is not supported");
            }
            try
            {
                test(databasemetadata, "getIndexInfo(null,null,\"%\",false,true)");
                ResultSet resultset6 = databasemetadata.getIndexInfo(null, null, "%", false, true);
                logResultSet(resultset6);
                ResultSetMetaData resultsetmetadata6 = resultset6.getMetaData();
                if(resultsetmetadata6.getColumnCount() < 13)
                {
                    verify(false, "The getIndexInfo table should contain 13 columns but only contains " + resultsetmetadata6.getColumnCount() + " columns");
                } else
                {
                    verify(resultsetmetadata6.getColumnName(1).compareTo("TABLE_CAT") == 0, "ColumnName(1) must be TABLE_CAT not '" + resultsetmetadata6.getColumnName(1) + "'");
                    verify(resultsetmetadata6.getColumnName(2).compareTo("TABLE_SCHEM") == 0, "ColumnName(2) must be TABLE_SCHEM not '" + resultsetmetadata6.getColumnName(2) + "'");
                    verify(resultsetmetadata6.getColumnName(3).compareTo("TABLE_NAME") == 0, "ColumnName(3) must be TABLE_NAME not '" + resultsetmetadata6.getColumnName(3) + "'");
                    verify(resultsetmetadata6.getColumnName(4).compareTo("NON_UNIQUE") == 0, "ColumnName(4) must be NON_UNIQUE not '" + resultsetmetadata6.getColumnName(4) + "'");
                    verify(resultsetmetadata6.getColumnName(5).compareTo("INDEX_QUALIFIER") == 0, "ColumnName(5) must be INDEX_QUALIFIER not '" + resultsetmetadata6.getColumnName(5) + "'");
                    verify(resultsetmetadata6.getColumnName(6).compareTo("INDEX_NAME") == 0, "ColumnName(6) must be INDEX_NAME not '" + resultsetmetadata6.getColumnName(6) + "'");
                    verify(resultsetmetadata6.getColumnName(7).compareTo("TYPE") == 0, "ColumnName(7) must be TYPE not '" + resultsetmetadata6.getColumnName(7) + "'");
                    verify(resultsetmetadata6.getColumnName(8).compareTo("ORDINAL_POSITION") == 0, "ColumnName(8) must be ORDINAL_POSITION not '" + resultsetmetadata6.getColumnName(8) + "'");
                    verify(resultsetmetadata6.getColumnName(9).compareTo("COLUMN_NAME") == 0, "ColumnName(9) must be COLUMN_NAME not '" + resultsetmetadata6.getColumnName(9) + "'");
                    verify(resultsetmetadata6.getColumnName(10).compareTo("ASC_OR_DESC") == 0, "ColumnName(10) must be ASC_OR_DESC not '" + resultsetmetadata6.getColumnName(10) + "'");
                    verify(resultsetmetadata6.getColumnName(11).compareTo("CARDINALITY") == 0, "ColumnName(11) must be CARDINALITY not '" + resultsetmetadata6.getColumnName(11) + "'");
                    verify(resultsetmetadata6.getColumnName(12).compareTo("PAGES") == 0, "ColumnName(12) must be PAGES not '" + resultsetmetadata6.getColumnName(12) + "'");
                    verify(resultsetmetadata6.getColumnName(13).compareTo("FILTER_CONDITION") == 0, "ColumnName(13) must be FILTER_CONDITION not '" + resultsetmetadata6.getColumnName(13) + "'");
                }
            }
            catch(SQLException _ex)
            {
                result("getIndexInfo is not supported");
            }
            trySQL("drop table JDBCKEY2");
            trySQL("drop table JDBCKEY1");
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

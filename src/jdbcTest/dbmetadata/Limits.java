// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Limits.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class Limits extends TestModule
{

    public Limits()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        boolean flag = false;
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.Supported");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            test(databasemetadata, "getMaxBinaryLiteralLength()");
            result(databasemetadata.getMaxBinaryLiteralLength());
            test(databasemetadata, "getMaxCharLiteralLength()");
            result(databasemetadata.getMaxCharLiteralLength());
            test(databasemetadata, "getMaxColumnNameLength()");
            result(databasemetadata.getMaxColumnNameLength());
            test(databasemetadata, "getMaxColumnsInGroupBy()");
            result(databasemetadata.getMaxColumnsInGroupBy());
            test(databasemetadata, "getMaxColumnsInIndex()");
            result(databasemetadata.getMaxColumnsInIndex());
            test(databasemetadata, "getMaxColumnsInOrderBy()");
            result(databasemetadata.getMaxColumnsInOrderBy());
            test(databasemetadata, "getMaxColumnsInSelect()");
            result(databasemetadata.getMaxColumnsInSelect());
            test(databasemetadata, "getMaxColumnsInTable()");
            result(databasemetadata.getMaxColumnsInTable());
            test(databasemetadata, "getMaxConnections()");
            result(databasemetadata.getMaxConnections());
            test(databasemetadata, "getMaxCursorNameLength()");
            result(databasemetadata.getMaxCursorNameLength());
            test(databasemetadata, "getMaxIndexLength()");
            result(databasemetadata.getMaxIndexLength());
            test(databasemetadata, "getMaxSchemaNameLength()");
            result(databasemetadata.getMaxSchemaNameLength());
            test(databasemetadata, "getMaxProcedureNameLength()");
            result(databasemetadata.getMaxProcedureNameLength());
            test(databasemetadata, "getMaxCatalogNameLength()");
            result(databasemetadata.getMaxCatalogNameLength());
            test(databasemetadata, "getMaxRowSize()");
            result(databasemetadata.getMaxRowSize());
            test(databasemetadata, "doesMaxRowSizeIncludeBlobs()");
            result(databasemetadata.doesMaxRowSizeIncludeBlobs());
            test(databasemetadata, "getMaxStatementLength()");
            result(databasemetadata.getMaxStatementLength());
            test(databasemetadata, "getMaxStatements()");
            result(databasemetadata.getMaxStatements());
            test(databasemetadata, "getMaxTableNameLength()");
            result(databasemetadata.getMaxTableNameLength());
            test(databasemetadata, "getMaxTablesInSelect()");
            result(databasemetadata.getMaxTablesInSelect());
            test(databasemetadata, "getMaxUserNameLength()");
            result(databasemetadata.getMaxUserNameLength());
            test(databasemetadata, "getDefaultTransactionIsolation()");
            result(databasemetadata.getDefaultTransactionIsolation());
            test(databasemetadata, "supportsTransactions()");
            result(databasemetadata.supportsTransactions());
            test(databasemetadata, "supportsDataDefinitionAndDataManipulationTransactions()");
            result(databasemetadata.supportsDataDefinitionAndDataManipulationTransactions());
            test(databasemetadata, "supportsDataManipulationTransactionsOnly()");
            result(databasemetadata.supportsDataManipulationTransactionsOnly());
            test(databasemetadata, "dataDefinitionCausesTransactionCommit()");
            result(databasemetadata.dataDefinitionCausesTransactionCommit());
            test(databasemetadata, "dataDefinitionIgnoredInTransactions()");
            result(databasemetadata.dataDefinitionIgnoredInTransactions());
            test(databasemetadata, "supportsTransactionIsolationLevel(TRANSACTION_NONE)");
            result(databasemetadata.supportsTransactionIsolationLevel(0));
            test(databasemetadata, "supportsTransactionIsolationLevel(TRANSACTION_READ_UNCOMMITTED)");
            result(databasemetadata.supportsTransactionIsolationLevel(1));
            test(databasemetadata, "supportsTransactionIsolationLevel(TRANSACTION_READ_COMMITTED)");
            result(databasemetadata.supportsTransactionIsolationLevel(2));
            test(databasemetadata, "supportsTransactionIsolationLevel(TRANSACTION_REPEATABLE_READ)");
            result(databasemetadata.supportsTransactionIsolationLevel(4));
            test(databasemetadata, "supportsTransactionIsolationLevel(TRANSACTION_SERIALIZABLE)");
            result(databasemetadata.supportsTransactionIsolationLevel(8));
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

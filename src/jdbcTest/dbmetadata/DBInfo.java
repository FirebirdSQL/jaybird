// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   DBInfo.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class DBInfo extends TestModule
{

    public DBInfo()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        boolean flag = false;
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.DBInfo");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            test(databasemetadata, "allProceduresAreCallable()");
            result(databasemetadata.allProceduresAreCallable());
            test(databasemetadata, "allTablesAreSelectable()");
            result(databasemetadata.allTablesAreSelectable());
            test(databasemetadata, "getURL()");
            String s = databasemetadata.getURL();
            result(s);
            verify(s.equalsIgnoreCase(getUrl()), "getURL() should match signon URL");
            test(databasemetadata, "getUserName()");
            result(databasemetadata.getUserName());
            test(databasemetadata, "isReadOnly()");
            result(databasemetadata.isReadOnly());
            test(databasemetadata, "nullsAreSortedHigh()");
            result(databasemetadata.nullsAreSortedHigh());
            test(databasemetadata, "nullsAreSortedLow()");
            result(databasemetadata.nullsAreSortedLow());
            verify((databasemetadata.nullsAreSortedHigh() && databasemetadata.nullsAreSortedLow()) ^ true, "nullsAreSortedHigh and nullsAreSortedLow return inconsistent results");
            test(databasemetadata, "nullsAreSortedAtStart()");
            result(databasemetadata.nullsAreSortedAtStart());
            test(databasemetadata, "nullsAreSortedAtEnd()");
            result(databasemetadata.nullsAreSortedAtEnd());
            verify((databasemetadata.nullsAreSortedAtStart() && databasemetadata.nullsAreSortedAtEnd()) ^ true, "nullsAreSortedAtStart and nullsAreSortedAtEnd return inconsistent results");
            test(databasemetadata, "getDatabaseProductName()");
            result(databasemetadata.getDatabaseProductName());
            test(databasemetadata, "getDatabaseProductVersion()");
            result(databasemetadata.getDatabaseProductVersion());
            test(databasemetadata, "getDriverName()");
            result(databasemetadata.getDriverName());
            test(databasemetadata, "getDriverVersion()");
            result(databasemetadata.getDriverVersion());
            test(databasemetadata, "getDriverMajorVersion()");
            result(databasemetadata.getDriverMajorVersion());
            test(databasemetadata, "getDriverMinorVersion()");
            result(databasemetadata.getDriverMinorVersion());
            test(databasemetadata, "usesLocalFiles()");
            result(databasemetadata.usesLocalFiles());
            test(databasemetadata, "usesLocalFilePerTable()");
            result(databasemetadata.usesLocalFilePerTable());
            test(databasemetadata, "supportsMixedCaseIdentifiers()");
            result(databasemetadata.supportsMixedCaseIdentifiers());
            verify(databasemetadata.supportsMixedCaseIdentifiers() ^ true, "supportsMixedCaseIdentifiers should return false");
            test(databasemetadata, "storesUpperCaseIdentifiers()");
            result(databasemetadata.storesUpperCaseIdentifiers());
            test(databasemetadata, "storesLowerCaseIdentifiers()");
            result(databasemetadata.storesLowerCaseIdentifiers());
            test(databasemetadata, "storesMixedCaseIdentifiers()");
            result(databasemetadata.storesMixedCaseIdentifiers());
            test(databasemetadata, "supportsMixedCaseQuotedIdentifiers()");
            result(databasemetadata.supportsMixedCaseQuotedIdentifiers());
            verify(databasemetadata.supportsMixedCaseQuotedIdentifiers(), "supportsMixedCaseQuotedIdentifiers should return true");
            test(databasemetadata, "storesUpperCaseQuotedIdentifiers()");
            result(databasemetadata.storesUpperCaseQuotedIdentifiers());
            test(databasemetadata, "storesLowerCaseQuotedIdentifiers()");
            result(databasemetadata.storesLowerCaseQuotedIdentifiers());
            test(databasemetadata, "storesMixedCaseQuotedIdentifiers()");
            result(databasemetadata.storesMixedCaseQuotedIdentifiers());
            test(databasemetadata, "getIdentifierQuoteString()");
            result(databasemetadata.getIdentifierQuoteString());
            verify(databasemetadata.getIdentifierQuoteString().equals("\""), "quote String should be \"");
            test(databasemetadata, "getSQLKeywords()");
            result(databasemetadata.getSQLKeywords());
            test(databasemetadata, "getNumericFunctions()");
            result(databasemetadata.getNumericFunctions());
            test(databasemetadata, "getStringFunctions()");
            result(databasemetadata.getStringFunctions());
            test(databasemetadata, "getSystemFunctions()");
            result(databasemetadata.getSystemFunctions());
            test(databasemetadata, "getTimeDateFunctions()");
            result(databasemetadata.getTimeDateFunctions());
            test(databasemetadata, "getSearchStringEscape()");
            result(databasemetadata.getSearchStringEscape());
            test(databasemetadata, "getExtraNameCharacters()");
            result(databasemetadata.getExtraNameCharacters());
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

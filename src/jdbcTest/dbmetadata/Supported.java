// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Supported.java

package jdbcTest.dbmetadata;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class Supported extends TestModule
{

    public Supported()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        jdbcTest.harness.TestCase testcase = createTestCase("Test DBMetadata.Supported");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            test(databasemetadata, "supportsAlterTableWithAddColumn()");
            result(databasemetadata.supportsAlterTableWithAddColumn());
            test(databasemetadata, "supportsAlterTableWithDropColumn()");
            result(databasemetadata.supportsAlterTableWithDropColumn());
            test(databasemetadata, "supportsColumnAliasing()");
            result(databasemetadata.supportsColumnAliasing());
            verify(databasemetadata.supportsColumnAliasing(), "support for column aliasing required");
            test(databasemetadata, "nullPlusNonNullIsNull()");
            result(databasemetadata.nullPlusNonNullIsNull());
            test(databasemetadata, "supportsConvert()");
            result(databasemetadata.supportsConvert());
            test(databasemetadata, "supportsTableCorrelationNames()");
            result(databasemetadata.supportsTableCorrelationNames());
            verify(databasemetadata.supportsTableCorrelationNames(), "table correlation names should be supported");
            test(databasemetadata, "supportsDifferentTableCorrelationNames()");
            result(databasemetadata.supportsDifferentTableCorrelationNames());
            test(databasemetadata, "supportsExpressionsInOrderBy()");
            result(databasemetadata.supportsExpressionsInOrderBy());
            test(databasemetadata, "supportsOrderByUnrelated()");
            result(databasemetadata.supportsOrderByUnrelated());
            test(databasemetadata, "supportsGroupBy()");
            result(databasemetadata.supportsGroupBy());
            test(databasemetadata, "supportsGroupByUnrelated()");
            result(databasemetadata.supportsGroupByUnrelated());
            test(databasemetadata, "supportsGroupByBeyondSelect()");
            result(databasemetadata.supportsGroupByBeyondSelect());
            test(databasemetadata, "supportsLikeEscapeClause()");
            result(databasemetadata.supportsLikeEscapeClause());
            verify(databasemetadata.supportsLikeEscapeClause(), "Like escape clauses should be supported");
            test(databasemetadata, "supportsMultipleResultSets()");
            result(databasemetadata.supportsMultipleResultSets());
            test(databasemetadata, "supportsMultipleTransactions()");
            result(databasemetadata.supportsMultipleTransactions());
            test(databasemetadata, "supportsNonNullableColumns()");
            result(databasemetadata.supportsNonNullableColumns());
            verify(databasemetadata.supportsNonNullableColumns(), "NonNullable columns must be supported");
            test(databasemetadata, "supportsMinimumSQLGrammar()");
            result(databasemetadata.supportsMinimumSQLGrammar());
            verify(databasemetadata.supportsMinimumSQLGrammar(), "Support for minimum grammar required");
            test(databasemetadata, "supportsCoreSQLGrammar()");
            result(databasemetadata.supportsCoreSQLGrammar());
            test(databasemetadata, "supportsExtendedSQLGrammar()");
            result(databasemetadata.supportsExtendedSQLGrammar());
            test(databasemetadata, "supportsIntegrityEnhancementFacility()");
            result(databasemetadata.supportsIntegrityEnhancementFacility());
            test(databasemetadata, "supportsOuterJoins()");
            result(databasemetadata.supportsOuterJoins());
            test(databasemetadata, "supportsFullOuterJoins()");
            result(databasemetadata.supportsFullOuterJoins());
            test(databasemetadata, "supportsLimitedOuterJoins()");
            result(databasemetadata.supportsLimitedOuterJoins());
            test(databasemetadata, "getSchemaTerm()");
            result(databasemetadata.getSchemaTerm());
            test(databasemetadata, "getProcedureTerm()");
            result(databasemetadata.getProcedureTerm());
            test(databasemetadata, "getCatalogTerm()");
            result(databasemetadata.getCatalogTerm());
            test(databasemetadata, "isCatalogAtStart()");
            result(databasemetadata.isCatalogAtStart());
            test(databasemetadata, "getCatalogSeparator()");
            result(databasemetadata.getCatalogSeparator());
            test(databasemetadata, "supportsSchemasInDataManipulation()");
            result(databasemetadata.supportsSchemasInDataManipulation());
            test(databasemetadata, "supportsSchemasInProcedureCalls()");
            result(databasemetadata.supportsSchemasInProcedureCalls());
            test(databasemetadata, "supportsSchemasInTableDefinitions()");
            result(databasemetadata.supportsSchemasInTableDefinitions());
            test(databasemetadata, "supportsSchemasInIndexDefinitions()");
            result(databasemetadata.supportsSchemasInIndexDefinitions());
            test(databasemetadata, "supportsSchemasInPrivilegeDefinitions()");
            result(databasemetadata.supportsSchemasInPrivilegeDefinitions());
            test(databasemetadata, "supportsCatalogsInDataManipulation()");
            result(databasemetadata.supportsCatalogsInDataManipulation());
            test(databasemetadata, "supportsCatalogsInProcedureCalls()");
            result(databasemetadata.supportsCatalogsInProcedureCalls());
            test(databasemetadata, "supportsCatalogsInTableDefinitions()");
            result(databasemetadata.supportsCatalogsInTableDefinitions());
            test(databasemetadata, "supportsCatalogsInIndexDefinitions()");
            result(databasemetadata.supportsCatalogsInIndexDefinitions());
            test(databasemetadata, "supportsCatalogsInPrivilegeDefinitions()");
            result(databasemetadata.supportsCatalogsInPrivilegeDefinitions());
            test(databasemetadata, "supportsPositionedDelete()");
            result(databasemetadata.supportsPositionedDelete());
            test(databasemetadata, "supportsPositionedUpdate()");
            result(databasemetadata.supportsPositionedUpdate());
            test(databasemetadata, "supportsSelectForUpdate()");
            result(databasemetadata.supportsSelectForUpdate());
            test(databasemetadata, "supportsStoredProcedures()");
            result(databasemetadata.supportsStoredProcedures());
            test(databasemetadata, "supportsSubqueriesInComparisons()");
            result(databasemetadata.supportsSubqueriesInComparisons());
            verify(databasemetadata.supportsSubqueriesInComparisons(), "subqueries in comparisons should be supported");
            test(databasemetadata, "supportsSubqueriesInExists()");
            result(databasemetadata.supportsSubqueriesInExists());
            verify(databasemetadata.supportsSubqueriesInExists(), "subqueries in exists should be supported");
            test(databasemetadata, "supportsSubqueriesInIns()");
            result(databasemetadata.supportsSubqueriesInIns());
            verify(databasemetadata.supportsSubqueriesInIns(), "subqueries in inserts should be supported");
            test(databasemetadata, "supportsSubqueriesInQuantifieds()");
            result(databasemetadata.supportsSubqueriesInQuantifieds());
            verify(databasemetadata.supportsSubqueriesInQuantifieds(), "subqueries in quantified expressions should be supported");
            test(databasemetadata, "supportsCorrelatedSubqueries()");
            result(databasemetadata.supportsCorrelatedSubqueries());
            verify(databasemetadata.supportsCorrelatedSubqueries(), "correlated subqueries should be supported");
            test(databasemetadata, "supportsUnion()");
            result(databasemetadata.supportsUnion());
            test(databasemetadata, "supportsUnionAll()");
            result(databasemetadata.supportsUnion());
            test(databasemetadata, "supportsOpenCursorsAcrossCommit()");
            result(databasemetadata.supportsOpenCursorsAcrossCommit());
            test(databasemetadata, "supportsOpenCursorsAcrossRollback()");
            result(databasemetadata.supportsOpenCursorsAcrossRollback());
            test(databasemetadata, "supportsOpenStatementsAcrossCommit()");
            result(databasemetadata.supportsOpenStatementsAcrossCommit());
            test(databasemetadata, "supportsOpenStatementsAcrossRollback()");
            result(databasemetadata.supportsOpenStatementsAcrossRollback());
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

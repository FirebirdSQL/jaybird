/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jdbc.parser.JaybirdStatementModel;
import org.firebirdsql.jdbc.parser.StatementParser;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Builds (updates) queries to add generated keys support.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
final class GeneratedKeysQueryBuilder {

    // TODO Add caching for column info

    private static final Logger logger = LoggerFactory.getLogger(GeneratedKeysQueryBuilder.class);
    private static final GeneratedKeysSupport.QueryType[] statementTypeToQueryType;
    static {
        GeneratedKeysSupport.QueryType[] temp =
                new GeneratedKeysSupport.QueryType[JaybirdStatementModel.MAX_STATEMENT_TYPE_VALUE + 1];
        Arrays.fill(temp, GeneratedKeysSupport.QueryType.UNSUPPORTED);
        temp[JaybirdStatementModel.INSERT_TYPE] = GeneratedKeysSupport.QueryType.INSERT;
        temp[JaybirdStatementModel.UPDATE_TYPE] = GeneratedKeysSupport.QueryType.UPDATE;
        temp[JaybirdStatementModel.DELETE_TYPE] = GeneratedKeysSupport.QueryType.DELETE;
        temp[JaybirdStatementModel.UPDATE_OR_INSERT_TYPE] = GeneratedKeysSupport.QueryType.UPDATE_OR_INSERT;
        temp[JaybirdStatementModel.MERGE_TYPE] = GeneratedKeysSupport.QueryType.MERGE;
        statementTypeToQueryType = temp;
    }

    private static final int IDX_COLUMN_NAME = 4;
    private static final int IDX_ORDINAL_POSITION = 17;

    private final String originalSql;
    private final JaybirdStatementModel statementModel;
    private final Set<GeneratedKeysSupport.QueryType> supportedQueryTypes;

    /**
     * Creates a generated keys query builder.
     *
     * @param originalSql
     *         Original statement text
     * @param statementModel
     *         Parsed statement model
     * @param supportedQueryTypes
     *         Supported query types
     */
    private GeneratedKeysQueryBuilder(String originalSql, JaybirdStatementModel statementModel,
            Set<GeneratedKeysSupport.QueryType> supportedQueryTypes) {
        this.originalSql = originalSql;
        this.statementModel = statementModel;
        this.supportedQueryTypes = supportedQueryTypes;
    }

    /**
     * Creates a generated keys query builder that will always return the original SQL.
     *
     * @param originalSql
     *         Original statement text
     */
    private GeneratedKeysQueryBuilder(String originalSql) {
        this(originalSql, null, Collections.<GeneratedKeysSupport.QueryType>emptySet());
    }

    /**
     * Create a generated keys query builder.
     *
     * @param parser
     *         Parser for parsing the statement
     * @param statementText
     *         Statement text
     * @param supportedQueryTypes
     *         Query types to support for generated keys
     * @return A generated keys query builder
     */
    static GeneratedKeysQueryBuilder create(StatementParser parser, String statementText,
            Set<GeneratedKeysSupport.QueryType> supportedQueryTypes) {
        try {
            JaybirdStatementModel statementModel = parser.parseStatement(statementText);
            return new GeneratedKeysQueryBuilder(statementText, statementModel, supportedQueryTypes);
        } catch (StatementParser.ParseException e) {
            if (logger.isDebugEnabled()) logger.debug("Exception parsing query: " + statementText, e);
            return new GeneratedKeysQueryBuilder(statementText);
        }
    }

    /**
     * @return {@code true} when the query type is supported for returning generated keys
     */
    boolean isSupportedType() {
        if (statementModel == null) {
            return false;
        }
        int statementType = statementModel.getStatementType();
        try {
            GeneratedKeysSupport.QueryType queryType = statementTypeToQueryType[statementType];
            return supportedQueryTypes.contains(queryType);
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Unsupported or incorrectly defined statement type: " + statementType);
            return false;
        }
    }

    /**
     * Produces Query instance for the {@link java.sql.Statement#NO_GENERATED_KEYS} option.
     * <p>
     * Historically Jaybird allows generated keys retrieval if a {@code RETURNING} clause is explicitly present, even
     * when executed with NO_GENERATED_KEYS. This avoids issues with executeUpdate producing result sets. This is done
     * irrespective of the configured {@code supportedQueryTypes}.
     * </p>
     *
     * @return Query object that only has {@link org.firebirdsql.jdbc.GeneratedKeysSupport.Query#generatesKeys()} with
     * value {@code true} if the original statement already had a {@code RETURNING} clause.
     */
    GeneratedKeysSupport.Query forNoGeneratedKeysOption() {
        if (hasReturning()) {
            return new GeneratedKeysSupport.Query(true, originalSql);
        }
        return new GeneratedKeysSupport.Query(false, originalSql);
    }

    /**
     * Returns a generated keys query object for all columns (if supported).
     *
     * @param databaseMetaData
     *         Database meta data
     * @return Query object
     * @throws SQLException
     *         if a database access error occurs
     */
    GeneratedKeysSupport.Query forReturnGeneratedKeysOption(FirebirdDatabaseMetaData databaseMetaData)
            throws SQLException {
        if (hasReturning()) {
            // See also comment on forNoGeneratedKeysOption
            return new GeneratedKeysSupport.Query(true, originalSql);
        }
        if (isSupportedType()) {
            // TODO Use an strategy when creating this builder or even push this up to the GeneratedKeysSupportFactory?
            if (supportsReturningAll(databaseMetaData)) {
                return useReturningAll();
            }
            return useReturningAllColumnsByName(databaseMetaData);
        }
        return new GeneratedKeysSupport.Query(false, originalSql);
    }

    /**
     * Determines support for {@code RETURNING *}.
     *
     * @param databaseMetaData
     *         Database meta data
     * @return {@code true} if this version of Firebird supports {@code RETURNING *}.
     * @throws SQLException
     *         for database access errors
     */
    private boolean supportsReturningAll(FirebirdDatabaseMetaData databaseMetaData) throws SQLException {
        return databaseMetaData.getDatabaseMajorVersion() >= 4;
    }

    /**
     * Generates the query using {@code RETURNING *}
     */
    private GeneratedKeysSupport.Query useReturningAll() {
        return addColumnsByNameImpl(Collections.singletonList("*"), QuoteStrategy.NO_QUOTES);
    }

    /**
     * Generates the query by retrieving all column names and appending them to a {@code RETURNING} clause.
     */
    private GeneratedKeysSupport.Query useReturningAllColumnsByName(FirebirdDatabaseMetaData databaseMetaData)
            throws SQLException {
        List<String> columnNames = getAllColumnNames(statementModel.getTableName(), databaseMetaData);
        QuoteStrategy quoteStrategy = QuoteStrategy.forDialect(databaseMetaData.getConnectionDialect());
        return addColumnsByNameImpl(columnNames, quoteStrategy);
    }

    private boolean hasReturning() {
        return statementModel != null && statementModel.hasReturning();
    }

    /**
     * Returns a generated keys query object with columns identified by the indexes passed
     *
     * @param columnIndexes
     *         1-based indexes of the columns to return
     * @param databaseMetaData
     *         Database meta data
     * @return Query object
     * @throws SQLException
     *         if a database access error occurs or the query cannot be built
     */
    GeneratedKeysSupport.Query forColumnsByIndex(int[] columnIndexes, FirebirdDatabaseMetaData databaseMetaData)
            throws SQLException {
        if (hasReturning()) {
            // See also comment on forNoGeneratedKeysOption
            return new GeneratedKeysSupport.Query(true, originalSql);
        } else if (columnIndexes == null || columnIndexes.length == 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull)
                    .messageParameter("columnIndexes")
                    .toFlatSQLException();
        } else if (isSupportedType()) {
            List<String> columnNames = getColumnNames(statementModel.getTableName(), columnIndexes, databaseMetaData);
            QuoteStrategy quoteStrategy = QuoteStrategy.forDialect(databaseMetaData.getConnectionDialect());
            return addColumnsByNameImpl(columnNames, quoteStrategy);
        } else {
            // Unsupported type, ignore column indexes
            return new GeneratedKeysSupport.Query(false, originalSql);
        }
    }

    /**
     * Returns a generated keys query object for the specified columns.
     *
     * @param columnNames
     *         Array with column names to add (NOTE: current implementation expects already quoted where necessary)
     * @return Query object
     * @throws SQLException
     *         if a database access error occurs
     */
    GeneratedKeysSupport.Query forColumnsByName(String[] columnNames) throws SQLException {
        if (hasReturning()) {
            // See also comment on forNoGeneratedKeysOption
            return new GeneratedKeysSupport.Query(true, originalSql);
        } else if (columnNames == null || columnNames.length == 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull)
                    .messageParameter("columnNames")
                    .toFlatSQLException();
        } else if (isSupportedType()) {
            return addColumnsByNameImpl(Arrays.asList(columnNames), QuoteStrategy.NO_QUOTES);
        } else {
            // Unsupported type, ignore column names
            return new GeneratedKeysSupport.Query(false, originalSql);
        }
    }

    private GeneratedKeysSupport.Query addColumnsByNameImpl(List<String> columnNames, QuoteStrategy quoteStrategy) {
        assert columnNames != null && !columnNames.isEmpty() : "Column names are required";

        StringBuilder returningQuery = new StringBuilder(originalSql);
        // Strip whitespace and ';' from end
        for (int idx = returningQuery.length() - 1; idx >= 0; idx--) {
            char currentChar = returningQuery.charAt(idx);
            if (currentChar == ';') {
                returningQuery.setLength(idx);
                break;
            } else if (!Character.isWhitespace(currentChar)) {
                returningQuery.setLength(idx + 1);
                break;
            }
        }
        returningQuery
                .append('\n')
                .append("RETURNING ");
        for (String columnName : columnNames) {
            quoteStrategy
                    .appendQuoted(columnName, returningQuery)
                    .append(',');
        }
        // Delete last ','
        returningQuery.setLength(returningQuery.length() - 1);
        return new GeneratedKeysSupport.Query(true, returningQuery.toString());
    }

    private List<String> getAllColumnNames(String tableName, FirebirdDatabaseMetaData databaseMetaData)
            throws SQLException {
        try (ResultSet rs = databaseMetaData.getColumns(null, null, normalizeObjectName(tableName), null)) {
            if (rs.next()) {
                List<String> columns = new ArrayList<>();
                do {
                    columns.add(rs.getString(IDX_COLUMN_NAME));
                } while (rs.next());
                return columns;
            }
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_generatedKeysNoColumnsFound)
                    .messageParameter(tableName)
                    .toFlatSQLException();
        }
    }

    private List<String> getColumnNames(String tableName, int[] columnIndexes,
            FirebirdDatabaseMetaData databaseMetaData) throws SQLException {
        Map<Integer, String> columnByIndex = mapColumnNamesByIndex(tableName, columnIndexes, databaseMetaData);

        List<String> columns = new ArrayList<>(columnIndexes.length);
        for (int indexToAdd : columnIndexes) {
            String columnName = columnByIndex.get(indexToAdd);
            if (columnName == null) {
                throw new FbExceptionBuilder()
                        .nonTransientException(JaybirdErrorCodes.jb_generatedKeysInvalidColumnPosition)
                        .messageParameter(indexToAdd)
                        .messageParameter(tableName)
                        .toFlatSQLException();
            }
            columns.add(columnName);
        }
        return columns;
    }

    private Map<Integer, String> mapColumnNamesByIndex(String tableName, int[] columnIndexes,
            FirebirdDatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet rs = databaseMetaData.getColumns(null, null, normalizeObjectName(tableName), null)) {
            if (!rs.next()) {
                throw new FbExceptionBuilder()
                        .nonTransientException(JaybirdErrorCodes.jb_generatedKeysNoColumnsFound)
                        .messageParameter(tableName)
                        .toFlatSQLException();
            }

            Map<Integer, String> columnByIndex = new HashMap<>();
            int[] sortedIndexes = columnIndexes.clone();
            Arrays.sort(sortedIndexes);
            do {
                int columnPosition = rs.getInt(IDX_ORDINAL_POSITION);
                if (Arrays.binarySearch(sortedIndexes, columnPosition) >= 0) {
                    columnByIndex.put(columnPosition, rs.getString(IDX_COLUMN_NAME));
                }
            } while (rs.next());

            return columnByIndex;
        }
    }

    /**
     * Normalizes an object name from the parser.
     * <p>
     * Like-wildcard characters are escaped, and unquoted identifiers are uppercased, and quoted identifiers are
     * returned with the quotes stripped and double double quotes replaced by a single double quote.
     * </p>
     *
     * @param objectName
     *         Object name
     * @return Normalized object name
     */
    private String normalizeObjectName(String objectName) {
        if (objectName == null) return null;
        objectName = objectName.trim();
        objectName = MetadataPattern.escapeWildcards(objectName);
        if (objectName.length() > 2
                && objectName.charAt(0) == '"'
                && objectName.charAt(objectName.length() - 1) == '"') {
            return objectName.substring(1, objectName.length() - 1).replaceAll("\"\"", "\"");
        }
        return objectName.toUpperCase();
    }

}

// SPDX-FileCopyrightText: Copyright 2025-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.util.SearchPathHelper;
import org.jspecify.annotations.Nullable;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrBlank;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Changes the current schema of a connection, reports on the current schema, and tracks the information necessary to
 * correctly perform subsequent modifications.
 *
 * @author Mark Rotteveel
 * @since 7
 */
sealed abstract class SchemaChanger {

    /**
     * If schemas are supported, attempts to change the current schema to {@code schema}.
     * <p>
     * If {@code schema} is not an existing schema, the search path may be modified, but will not actually change
     * the current schema. If schemas are not supported, this method is a no-op.
     * </p>
     * <p>
     * The implementation tries to handle external search path changes, but correct functioning is
     * <string>not</strong> guaranteed if it is.
     * </p>
     *
     * @param schema
     *         new schema to set (non-{@code null} and not blank)
     * @throws SQLException
     *         for database access errors, or if {@code schema} is {@code null} or blank <em>if</em> schemas are
     *         supported
     * @see #setSearchPath(String)
     */
    abstract void setSchema(String schema) throws SQLException;

    /**
     * Sets the search path, overriding any previously set current schema or search path.
     *
     * @param searchPath
     *         new search path to set (non-{@code null} and not blank, comma-separate, and quoted if needed)
     * @throws java.sql.SQLFeatureNotSupportedException
     *         if schemas are not supported (Firebird 5.0 and older)
     * @throws SQLException
     *         for database access errors, or if {@code searchPath} is {@code null} or blank
     * @see #setSchema(String)
     */
    abstract void setSearchPath(String searchPath) throws SQLException;

    /**
     * Current schema and search path.
     * <p>
     * If schemas are not supported, an instance is returned with {@code schema} and {@code searchPath} {@code null}.
     * </p>
     *
     * @return current schema and search path
     * @throws SQLException
     *         for database access errors
     */
    abstract SchemaInfo getCurrentSchemaInfo() throws SQLException;

    /**
     * Creates a schema changer.
     * <p>
     * Depending on the Firebird version, the returned instance may ignore attempts to change the schema.
     * </p>
     *
     * @param connection
     *         connection
     * @return a schema change (never {@code null})
     * @throws SQLException
     *         for database access errors
     */
    static SchemaChanger createInstance(FBConnection connection) throws SQLException {
        if (supportInfoFor(connection).supportsSchemas()) {
            return new SchemaSupport(connection);
        }
        return NoSchemaSupport.getInstance();
    }

    /**
     * Schema and search path.
     *
     * @param schema
     *         schema
     * @param searchPath
     *         search path string
     */
    record SchemaInfo(@Nullable String schema, @Nullable String searchPath) {
        static final SchemaInfo NULL_INSTANCE = new SchemaInfo(null, null);

        List<String> toSearchPathList() {
            return SearchPathHelper.parseSearchPath(searchPath);
        }

        boolean searchPathEquals(SchemaInfo other) {
            return Objects.equals(this.searchPath, other.searchPath);
        }
    }

    /**
     * Implementation for Firebird 6.0 and higher, which support schemas.
     */
    private static final class SchemaSupport extends SchemaChanger {

        private final FBConnection connection;
        // Holds statement used for querying and changing the schema
        private final MetadataStatementHolder statementHolder;
        private SchemaInfo schemaInfoAfterLastChange = SchemaInfo.NULL_INSTANCE;
        /**
         * {@code null} signifies no change recorded
         */
        private @Nullable String lastSchemaChange;
        private List<String> lastSearchPath = List.of();

        SchemaSupport(FBConnection connection) throws SQLException {
            connection.checkValidity();
            if (!supportInfoFor(connection).supportsSchemas()) {
                throw new FBDriverNotCapableException("Schema support is only available in Firebird 6.0 and higher");
            }
            this.connection = connection;
            statementHolder = new MetadataStatementHolder(connection);
        }

        private Statement getStatement() throws SQLException {
            return statementHolder.getStatement();
        }

        @Override
        SchemaInfo getCurrentSchemaInfo() throws SQLException {
            try (var rs = getStatement().executeQuery(
                    "select CURRENT_SCHEMA, RDB$GET_CONTEXT('SYSTEM', 'SEARCH_PATH') from SYSTEM.RDB$DATABASE")) {
                rs.next();
                return new SchemaInfo(rs.getString(1), rs.getString(2));
            }
        }

        @Override
        void setSchema(String schema) throws SQLException {
            if (isNullOrBlank(schema)) {
                // TODO externalize?
                throw new SQLDataException("schema must be non-null and not blank",
                        SQLStateConstants.SQL_STATE_INVALID_USE_NULL);
            }
            try (var ignored = connection.withLock()) {
                SchemaInfo currentSchemaInfo = getCurrentSchemaInfo();
                final List<String> newSearchPath;
                if (currentSchemaInfo.searchPathEquals(schemaInfoAfterLastChange)) {
                    // assume no changes
                    if (schema.equals(lastSchemaChange)) return;

                    // modify schema by replacing previous first schema with new first schema
                    newSearchPath = new ArrayList<>(lastSearchPath);
                    if (!newSearchPath.set(0, schema).equals(lastSchemaChange)) {
                        // TODO SQLstate, externalize?
                        throw new SQLException(("Expected first item in lastSearchPath to be '%s', but "
                                + "lastSearchPath was '%s'; this is probably a bug in Jaybird")
                                .formatted(lastSchemaChange, lastSearchPath));
                    }
                } else {
                    List<String> originalSearchPath = currentSchemaInfo.toSearchPathList();
                    if (lastSchemaChange == null
                            && !originalSearchPath.isEmpty() && schema.equals(originalSearchPath.get(0))) {
                        // Initial search path already has the specified schema first, don't change anything
                        return;
                    }
                    newSearchPath = new ArrayList<>(originalSearchPath.size() + 1);
                    newSearchPath.add(schema);
                    newSearchPath.addAll(originalSearchPath);
                }

                setSearchPath0(SearchPathHelper.toSearchPath(newSearchPath, connection.getQuoteStrategy()));
                schemaInfoAfterLastChange = getCurrentSchemaInfo();
                lastSearchPath = List.copyOf(newSearchPath);
                lastSchemaChange = schema;
            }
        }

        @Override
        void setSearchPath(String searchPath) throws SQLException {
            if (isNullOrBlank(searchPath)) {
                // TODO externalize?
                throw new SQLDataException("search path must have at least one schema",
                        SQLStateConstants.SQL_STATE_INVALID_USE_NULL);
            }
            setSearchPath0(searchPath);
            schemaInfoAfterLastChange = getCurrentSchemaInfo();
            lastSearchPath = schemaInfoAfterLastChange.toSearchPathList();
            // given this change was no explicit call to setSchema, clear it
            lastSchemaChange = null;
        }

        private void setSearchPath0(String searchPath) throws SQLException {
            getStatement().execute("set search_path to " + searchPath);
        }
    }

    /**
     * Implementation for Firebird 5.0 and older, which do not support schemas.
     */
    static final class NoSchemaSupport extends SchemaChanger {

        private static final SchemaChanger INSTANCE = new NoSchemaSupport();

        @Override
        void setSchema(String schema) {
            // do nothing (not even validate the name)
        }

        @Override
        void setSearchPath(String searchPath) throws SQLException {
            throw new FBDriverNotCapableException("Schema support required for setSearchPath");
        }

        @Override
        SchemaInfo getCurrentSchemaInfo() {
            return SchemaInfo.NULL_INSTANCE;
        }

        static SchemaChanger getInstance() {
            return INSTANCE;
        }

    }

}

// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.OdsVersion;
import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.jaybird.util.UncheckedSQLException;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.jdbc.metadata.GetProcedures;
import org.firebirdsql.jdbc.metadata.MetadataPattern;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.Logger.Level.TRACE;
import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jaybird.util.ConditionalHelpers.firstNonNull;

/**
 * Factory to retrieve metadata on stored procedures in a Firebird database.
 */
@NullMarked
final class StoredProcedureMetaDataFactory {

    private StoredProcedureMetaDataFactory() {
        // no instances
    }

    /**
     * Retrieve a {@link StoredProcedureMetaData} object for a connection.
     *
     * @param connection
     *         connection for which data is to be retrieved
     * @return {@link StoredProcedureMetaData} for the current connection
     * @throws SQLException
     *         if an exception occurs while retrieving metadata
     */
    static StoredProcedureMetaData getInstance(FBConnection connection) throws SQLException {
        if (connection.isIgnoreProcedureType()) {
            return new DummyStoredProcedureMetaData();
        }
        FirebirdDatabaseMetaData metaData = connection.getMetaData();
        OdsVersion odsVersion = metaData.getOdsVersion();
        if (odsVersion.isEqualOrAbove(14)) {
            // Schema support (Firebird 6 or higher)
            return new SchemaAwareStoredProcedureMetaData(connection);
        } if (!odsVersion.isEqualOrAbove(11, 1)) {
            // No selectability info (Firebird 2.0 or earlier database)
            return new DummyStoredProcedureMetaData();
        }
        // Fallback to package-aware support (Firebird 2.1 or higher databases)
        return new PackageAwareStoredProcedureMetaData(connection);
    }

}

/**
 * Implementation of {@link StoredProcedureMetaData} that is schema-aware and package-aware. It can resolve scope
 * ambiguity and find procedures on the search path.
 */
@NullMarked
final class SchemaAwareStoredProcedureMetaData implements StoredProcedureMetaData {

    /**
     * Resolve ambiguous procedure references, by first looking for schema+name, and then searching on the search path
     * for package+name.
     * <p>
     * This emulates the resolution rules that Firebird itself also uses if no scope-specifier is used.
     * </p>
     */
    private static final String FIND_AMBIGUOUS_PROCEDURE = """
            with SEARCH_PATH as (
              select row_number() over() as PRIO, NAME as SCHEMA_NAME
              from SYSTEM.RDB$SQL.PARSE_UNQUALIFIED_NAMES(rdb$get_context('SYSTEM', 'SEARCH_PATH'))
            )
            select
              0 as PRIO,
              sp.RDB$SCHEMA_NAME as SCHEMA_NAME,
              sp.RDB$PACKAGE_NAME as PACKAGE_NAME,
              sp.RDB$PROCEDURE_NAME as PROCEDURE_NAME,
              sp.RDB$PROCEDURE_TYPE as PROCEDURE_TYPE
            from RDB$PROCEDURES sp
            where sp.RDB$SCHEMA_NAME = ? and sp.RDB$PACKAGE_NAME is null and sp.RDB$PROCEDURE_NAME = ?
            union all
            select
              s.PRIO,
              sp.RDB$SCHEMA_NAME,
              sp.RDB$PACKAGE_NAME,
              sp.RDB$PROCEDURE_NAME,
              sp.RDB$PROCEDURE_TYPE
            from SEARCH_PATH s
            inner join RDB$PROCEDURES sp
              on sp.RDB$SCHEMA_NAME = s.SCHEMA_NAME
            where sp.RDB$PACKAGE_NAME = ? and sp.RDB$PROCEDURE_NAME = ?
            order by 1
            fetch first row only""";

    /**
     * Find a procedure on the search path, by package (optional) and name.
     */
    private static final String FIND_SCHEMA_PROCEDURE = """
            with SEARCH_PATH as (
              select row_number() over() as PRIO, NAME as SCHEMA_NAME
              from SYSTEM.RDB$SQL.PARSE_UNQUALIFIED_NAMES(rdb$get_context('SYSTEM', 'SEARCH_PATH'))
            )
            select
              s.PRIO,
              sp.RDB$SCHEMA_NAME as SCHEMA_NAME,
              sp.RDB$PACKAGE_NAME as PACKAGE_NAME,
              sp.RDB$PROCEDURE_NAME as PROCEDURE_NAME,
              sp.RDB$PROCEDURE_TYPE as PROCEDURE_TYPE
            from SEARCH_PATH s
            inner join RDB$PROCEDURES sp
              on sp.RDB$SCHEMA_NAME = s.SCHEMA_NAME
            where sp.RDB$PACKAGE_NAME is not distinct from nullif(?, '') and sp.RDB$PROCEDURE_NAME = ?
            order by 1
            fetch first row only""";

    private static final System.Logger LOG = System.getLogger(SchemaAwareStoredProcedureMetaData.class.getName());

    private final PackageAwareStoredProcedureMetaData packageAwareStoredProcedureMetaData;
    private final DbMetadataMediator metadataMediator;

    SchemaAwareStoredProcedureMetaData(FBConnection connection) throws SQLException {
        packageAwareStoredProcedureMetaData = new PackageAwareStoredProcedureMetaData(connection);
        FBDatabaseMetaData dbmd = connection.getMetaData().unwrap(FBDatabaseMetaData.class);
        metadataMediator = dbmd.getDbMetadataMediator();
    }

    @Override
    public void updateSelectability(FBProcedureCall procedureCall) throws SQLException {
        // We can't cache for not fully qualified procedures, as their resolution depends on the search path.
        if (procedureCall.isAmbiguousScope()) {
            findAmbiguousProcedure(procedureCall);
        } else if (procedureCall.getSchema() == null) {
            findProcedureOnSearchPath(procedureCall);
        } else {
            // Already fully qualified, delegate to package-aware implementation
            packageAwareStoredProcedureMetaData.updateSelectability(procedureCall);
        }
    }

    private void findAmbiguousProcedure(FBProcedureCall procedureCall) throws SQLException {
        // Resolve ambiguity; the query emulates Firebird's resolution rules by first trying to resolve based on
        // schema, and then on packages on the search path
        String schemaOrPackage = requireNonNull(procedureCall.getSchema(), "schema");
        String procedureName = requireNonNull(procedureCall.getName(), "name");
        var parameters = List.of(schemaOrPackage, procedureName, schemaOrPackage, procedureName);
        try (ResultSet rs = metadataMediator.performMetaDataQuery(
                new MetadataQuery(FIND_AMBIGUOUS_PROCEDURE, parameters))) {
            if (rs.next()) {
                procedureCall.setSchema(rs.getString("SCHEMA_NAME"));
                procedureCall.setPackage(firstNonNull(rs.getString("PACKAGE_NAME"), FBProcedureCall.NO_PACKAGE));
                procedureCall.setAmbiguousScope(false);
                procedureCall.setSelectable(
                        rs.getInt("PROCEDURE_TYPE") == FirebirdDatabaseMetaData.jbProcedureTypeSelectable);
            } else {
                LOG.log(TRACE, "Could not find procedure for schema or package ''{0}'' and procedure name ''{1}''",
                        schemaOrPackage, procedureName);
            }
        }
    }

    private void findProcedureOnSearchPath(FBProcedureCall procedureCall) throws SQLException {
        // Find the (not-packaged or packaged) procedure on the search path
        String pkg = requireNonNull(procedureCall.getPackage(), "package");
        String procedureName = requireNonNull(procedureCall.getName(), "name");
        var parameters = List.of(pkg, procedureName);
        try (ResultSet rs = metadataMediator.performMetaDataQuery(
                new MetadataQuery(FIND_SCHEMA_PROCEDURE, parameters))) {
            if (rs.next()) {
                procedureCall.setSchema(rs.getString("SCHEMA_NAME"));
                procedureCall.setSelectable(
                        rs.getInt("PROCEDURE_TYPE") == FirebirdDatabaseMetaData.jbProcedureTypeSelectable);
            } else {
                LOG.log(TRACE,
                        "Could not find procedure on search path: package ''{0}'' and procedure name ''{1}''",
                        pkg, procedureName);
            }
        }
    }

}

/**
 * Implementation of {@link StoredProcedureMetaData} that can resolve selectability, and is package-aware.
 * <p>
 * This implementation works for Firebird 2.1 &mdash; Firebird 5.0, and also works for Firebird 6.0+ if the stored
 * procedure itself is already fully qualified.
 * </p>
 *
 * @since 7
 */
@NullMarked
final class PackageAwareStoredProcedureMetaData implements StoredProcedureMetaData {

    // Thread-safety assumption: access is always under lock on the connection by the caller.
    // We assume that selectability of a procedure is stable for the lifetime of the connection; i.e. it won't be
    // recreated to change from selectable to executable or vice versa.
    private final Map<ObjectReference, Boolean> procedureSelectability = new HashMap<>();
    private final GetProcedures getProcedures;

    PackageAwareStoredProcedureMetaData(FBConnection connection) throws SQLException {
        FBDatabaseMetaData dbmd = connection.getMetaData().unwrap(FBDatabaseMetaData.class);
        DbMetadataMediator mediator = new PackageAwareDbMetadataMediator(dbmd.getDbMetadataMediator());
        getProcedures = GetProcedures.create(mediator);
    }

    @Override
    public void updateSelectability(FBProcedureCall procedureCall) throws SQLException {
        requireFullyQualified(procedureCall);
        try {
            ObjectReference procedureReference = procedureCall.deriveObjectReference();
            Boolean selectable = procedureSelectability.computeIfAbsent(procedureReference,
                    ignored -> isSelectable(procedureCall));
            if (selectable == null) {
                // Procedure does not exist. Remove from map, so if it's created later, we can detect selectability.
                procedureSelectability.remove(procedureReference);
            } else {
                // Store or overwrite object reference to make it permanent
                procedureCall.setObjectReference(procedureReference);
                procedureCall.setSelectable(selectable);
            }
        } catch (UncheckedSQLException e) {
            throw e.getCause();
        }
    }

    private static void requireFullyQualified(FBProcedureCall procedureCall) throws SQLException {
        if (procedureCall.isAmbiguousScope() || procedureCall.getSchema() == null || procedureCall.getPackage() == null
                || procedureCall.getName() == null) {
            throw new SQLNonTransientException("The procedure was not full qualified; this likely indicates a bug in "
                    + "Jaybird. As a workaround, fully qualify the procedure, and/or use the scope specifier (%SCHEMA "
                    + "or %PACKAGE).");
        }
    }

    /**
     * Determines selectability of {@code procedureCall}.
     *
     * @param procedureCall
     *         procedure call
     * @return {@code true} if selectable, {@code false} if executable or selectability is unknown, {@code null} if the
     * procedure was not found
     * @throws UncheckedSQLException
     *         with errors for querying database metadata
     */
    private @Nullable Boolean isSelectable(FBProcedureCall procedureCall) {
        try (ResultSet procedures = getProcedures.getProcedures(
                /* not escaped: not a pattern */
                procedureCall.getPackage(),
                MetadataPattern.escapeWildcards(procedureCall.getSchema()),
                MetadataPattern.escapeWildcards(procedureCall.getName()))) {
            if (!procedures.next()) return null;
            return procedures.getInt("JB_PROCEDURE_TYPE") == FirebirdDatabaseMetaData.jbProcedureTypeSelectable;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    /**
     * Implementation of {@link DbMetadataMediator} that ensures {@link DbMetadataMediator#isUseCatalogAsPackage()} is
     * reported as {@code true}.
     * <p>
     * This implementation does not support calls to {@link DbMetadataMediator#getMetaData()}. All other methods are
     * delegated to a wrapped {@link DbMetadataMediator} instance.
     * </p>
     */
    private static final class PackageAwareDbMetadataMediator extends DbMetadataMediator {

        private final DbMetadataMediator mediator;

        PackageAwareDbMetadataMediator(DbMetadataMediator mediator) {
            if (mediator instanceof PackageAwareDbMetadataMediator) {
                throw new IllegalArgumentException("provided mediator instance is an instance of this class");
            }
            this.mediator = mediator;
        }

        @Override
        public FirebirdSupportInfo getFirebirdSupportInfo() {
            return mediator.getFirebirdSupportInfo();
        }

        @Override
        public ResultSet performMetaDataQuery(MetadataQuery metadataQuery) throws SQLException {
            return mediator.performMetaDataQuery(metadataQuery);
        }

        /**
         * {@inheritDoc}
         *
         * @throws UnsupportedOperationException
         *         always, as this implementation should not be used in this manner
         */
        @Override
        public FBDatabaseMetaData getMetaData() {
            // Instead of returning a (possibly) not-package-aware instance, or having to subclass FBDatabaseMetaData,
            // we disallow calling this method.
            throw new UnsupportedOperationException("getMetaData() should not be called on this mediator instance");
        }

        @Override
        @NullUnmarked
        public GDSType getGDSType() {
            return mediator.getGDSType();
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code true}
         */
        @Override
        public boolean isUseCatalogAsPackage() {
            return true;
        }

        @Override
        public Collection<String> getClientInfoPropertyNames() {
            return mediator.getClientInfoPropertyNames();
        }

        @Override
        public OdsVersion getOdsVersion() {
            return mediator.getOdsVersion();
        }

    }

}

/**
 * Implementation of {@link StoredProcedureMetaData} for Firebird ODS versions that don't have selectability information
 * (i.e. ODS 11.0 or older).
 *
 * @since 7
 */
@NullMarked
final class DummyStoredProcedureMetaData implements StoredProcedureMetaData {

    @Override
    public void updateSelectability(FBProcedureCall procedureCall) {
        // No selectability information, nothing to do
    }

}

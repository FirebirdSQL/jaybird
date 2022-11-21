/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.FBDatabaseMetaData.GLOBAL_TEMPORARY;
import static org.firebirdsql.jdbc.FBDatabaseMetaData.SYSTEM_TABLE;
import static org.firebirdsql.jdbc.FBDatabaseMetaData.TABLE;
import static org.firebirdsql.jdbc.FBDatabaseMetaData.VIEW;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])},
 * {@link java.sql.DatabaseMetaData#getTableTypes()} and
 * {@link org.firebirdsql.jdbc.FirebirdDatabaseMetaData#getTableTypeNames()}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public abstract class GetTables extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(11, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", "TABLES").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLES").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "TABLES").addField()
            .at(3).simple(SQL_VARYING, 20, "TABLE_TYPE", "TABLES").addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(4).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", "TABLES").addField()
            .at(5).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TYPE_CAT", "TABLES").addField()
            .at(6).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "TABLES").addField()
            .at(7).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TYPE_NAME", "TABLES").addField()
            .at(8).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "SELF_REFERENCING_COL_NAME", "TABLES").addField()
            .at(9).simple(SQL_VARYING | 1, 10, "REF_GENERATION", "TABLES").addField()
            // Jaybird extensions
            .at(10).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "OWNER_NAME", "TABLES").addField()
            .toRowDescriptor();

    private static final RowDescriptor ROW_DESCRIPTOR_TABLE_TYPES =
            new RowDescriptorBuilder(1, DbMetadataMediator.datatypeCoder)
                    .at(0).simple(SQL_VARYING, 31, "TABLE_TYPE", "TABLETYPES").addField()
                    .toRowDescriptor();

    private GetTables(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see java.sql.DatabaseMetaData#getTables(String, String, String, String[])
     */
    public final ResultSet getTables(String tableNamePattern, String[] types) throws SQLException {
        if ("".equals(tableNamePattern) || types != null && types.length == 0) {
            // Matching table name not possible
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetTablesQuery(tableNamePattern, toTypesSet(types));
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(2).setString(rs.getString("TABLE_NAME"))
                .at(3).setString(rs.getString("TABLE_TYPE"))
                .at(4).setString(rs.getString("REMARKS"))
                .at(10).setString(rs.getString("OWNER_NAME"))
                .toRowValue(true);
    }

    public final ResultSet getTableTypes() throws SQLException {
        RowValueBuilder valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR_TABLE_TYPES);
        Set<String> tableTypes = allTableTypes();
        List<RowValue> rows = new ArrayList<>(tableTypes.size());
        for (String tableType : tableTypes) {
            valueBuilder
                    .at(0).setString(tableType);
            rows.add(valueBuilder.toRowValue(false));
        }
        return new FBResultSet(ROW_DESCRIPTOR_TABLE_TYPES, rows);
    }

    public final String[] getTableTypeNames() {
        return allTableTypes().toArray(new String[0]);
    }

    private Set<String> toTypesSet(String[] types) {
        return types != null ? new HashSet<>(Arrays.asList(types)) : allTableTypes();
    }

    abstract MetadataQuery createGetTablesQuery(String tableNamePattern, Set<String> types);

    /**
     * All supported table types.
     * <p>
     * Implementation expects a set sorted in ascending order (e.g. backed by a {@code LinkedHashSet}).
     * </p>
     *
     * @return supported table types
     */
    abstract Set<String> allTableTypes();

    public static GetTables create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(2, 5)) {
            return FB2_5.createInstance(mediator);
        } else {
            return FB2_1.createInstance(mediator);
        }
    }

    private static final class FB2_1 extends GetTables {

        private static final String TABLE_COLUMNS_SYSTEM_2_1 =
                formatTableQuery(SYSTEM_TABLE, "RDB$SYSTEM_FLAG = 1 and rdb$view_blr is null");
        private static final String TABLE_COLUMNS_NORMAL_2_1 =
                formatTableQuery(TABLE, "RDB$SYSTEM_FLAG = 0 and rdb$view_blr is null");
        private static final String TABLE_COLUMNS_VIEW_2_1 = formatTableQuery(VIEW, "rdb$view_blr is not null");
        private static final String GET_TABLE_ORDER_BY_2_1 = "\norder by 2, 1";

        private static final Map<String, String> QUERY_PER_TYPE;
        static {
            Map<String, String> queryPerType = new LinkedHashMap<>((int) (1 + 3/0.75f));
            queryPerType.put(SYSTEM_TABLE, TABLE_COLUMNS_SYSTEM_2_1);
            queryPerType.put(TABLE, TABLE_COLUMNS_NORMAL_2_1);
            queryPerType.put(VIEW, TABLE_COLUMNS_VIEW_2_1);
            QUERY_PER_TYPE = unmodifiableMap(queryPerType);
        }

        /**
         * All table types supported for Firebird 2.1 and lower
         */
        private static final Set<String> ALL_TYPES_2_1 = unmodifiableSet(new LinkedHashSet<>(
                Arrays.asList(SYSTEM_TABLE, TABLE, VIEW)));

        private FB2_1(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetTables createInstance(DbMetadataMediator mediator) {
            return new FB2_1(mediator);
        }

        @Override
        MetadataQuery createGetTablesQuery(String tableNamePattern, Set<String> types) {
            Clause tableNameClause = new Clause("RDB$RELATION_NAME", tableNamePattern);
            List<Clause> clauses = new ArrayList<>(types.size());
            StringBuilder queryBuilder = new StringBuilder(2000);
            String tableNameCondition = tableNameClause.getCondition("\nand ", "");
            QUERY_PER_TYPE.entrySet().stream()
                    .filter(typeAndQuery -> types.contains(typeAndQuery.getKey()))
                    .map(Map.Entry::getValue)
                    .forEach(query -> {
                        if (queryBuilder.length() > 0) {
                            queryBuilder.append("\nunion all\n");
                        }
                        queryBuilder.append(query).append(tableNameCondition);
                        clauses.add(tableNameClause);
                    });
            queryBuilder.append(GET_TABLE_ORDER_BY_2_1);
            return new MetadataQuery(queryBuilder.toString(), Clause.parameters(clauses));
        }

        @Override
        Set<String> allTableTypes() {
            return ALL_TYPES_2_1;
        }

        private static String formatTableQuery(String tableType, String condition) {
            //@formatter:off
            return String.format(
                    "select\n"
                    + "  RDB$RELATION_NAME as TABLE_NAME,\n"
                    + "  cast('%s' as varchar(31)) as TABLE_TYPE,\n"
                    + "  RDB$DESCRIPTION as REMARKS,\n"
                    + "  RDB$OWNER_NAME as OWNER_NAME\n"
                    + "from RDB$RELATIONS\n"
                    + "where %s",
                    tableType, condition);
            //@formatter:on
        }
    }

    private static final class FB2_5 extends GetTables {

        private static final String GET_TABLE_ORDER_BY_2_5 = "\norder by 2, 1";

        //@formatter:off
        private static final String LEGACY_IS_TABLE = "rdb$relation_type is null and rdb$view_blr is null";
        private static final String LEGACY_IS_VIEW = "rdb$relation_type is null and rdb$view_blr is not null";

        private static final String TABLE_COLUMNS_2_5 =
                "select\n"
                + "  trim(trailing from RDB$RELATION_NAME) as TABLE_NAME,\n"
                + "  trim(trailing from case"
                + "    when rdb$relation_type = 0 or " + LEGACY_IS_TABLE + " then case when RDB$SYSTEM_FLAG = 1 then '" + SYSTEM_TABLE + "' else '" + TABLE + "' end\n"
                + "    when rdb$relation_type = 1 or " + LEGACY_IS_VIEW + " then '" + VIEW + "'\n"
                + "    when rdb$relation_type = 2 then '" + TABLE + "'\n" // external table; assume as normal table
                + "    when rdb$relation_type = 3 then '" + SYSTEM_TABLE + "'\n" // virtual (monitoring) table: assume system
                + "    when rdb$relation_type in (4, 5) then '" + GLOBAL_TEMPORARY + "'\n"
                + "  end) as TABLE_TYPE,\n"
                + "  RDB$DESCRIPTION as REMARKS,\n"
                + "  trim(trailing from RDB$OWNER_NAME) as OWNER_NAME\n"
                + "from RDB$RELATIONS";
        //@formatter:on

        /**
         * All table types supported for Firebird 2.5 and higher
         */
        private static final Set<String> ALL_TYPES_2_5 = unmodifiableSet(new LinkedHashSet<>(
                Arrays.asList(GLOBAL_TEMPORARY, SYSTEM_TABLE, TABLE, VIEW)));

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetTables createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createGetTablesQuery(String tableNamePattern, Set<String> types) {
            Clause tableNameClause = new Clause("RDB$RELATION_NAME", tableNamePattern);

            StringBuilder queryBuilder = new StringBuilder(1000).append(TABLE_COLUMNS_2_5);
            List<String> params;
            if (tableNameClause.hasCondition()) {
                queryBuilder.append("\nwhere ").append(tableNameClause.getCondition(false));
                params = Clause.parameters(tableNameClause);
            } else {
                params = Collections.emptyList();
            }

            if (!types.containsAll(ALL_TYPES_2_5)) {
                // Only construct conditions when we don't query for all
                StringBuilder typeCondition = new StringBuilder(120);
                if (types.contains(SYSTEM_TABLE) && types.contains(TABLE)) {
                    typeCondition.append("(rdb$relation_type in (0, 2, 3) or " + LEGACY_IS_TABLE + ")");
                } else if (types.contains(SYSTEM_TABLE)) {
                    // We assume that external tables are never system and that virtual tables are always system
                    typeCondition.append("(rdb$relation_type in (0, 3) or " + LEGACY_IS_TABLE + ") and rdb$system_flag = 1");
                } else if (types.contains(TABLE)) {
                    // We assume that external tables are never system and that virtual tables are always system
                    typeCondition.append("(rdb$relation_type in (0, 2) or " + LEGACY_IS_TABLE + ") and rdb$system_flag = 0");
                }

                if (types.contains(VIEW)) {
                    if (typeCondition.length() > 0) {
                        typeCondition.append(" or ");
                    }
                    // We assume (but don't check) that views are never system
                    typeCondition.append("(rdb$relation_type = 1 or " + LEGACY_IS_VIEW + ")");
                }

                if (types.contains(GLOBAL_TEMPORARY)) {
                    if (typeCondition.length() > 0) {
                        typeCondition.append(" or ");
                    }
                    typeCondition.append("rdb$relation_type in (4, 5)");
                }

                if (typeCondition.length() == 0) {
                    // Requested types are unknown, query nothing
                    typeCondition.append("1 = 0");
                }

                if (tableNameClause.hasCondition()) {
                    queryBuilder.append("\nand (").append(typeCondition).append(")");
                } else {
                    queryBuilder.append("\nwhere ").append(typeCondition);
                }
            }
            queryBuilder.append(GET_TABLE_ORDER_BY_2_5);

            return new MetadataQuery(queryBuilder.toString(), params);
        }

        @Override
        Set<String> allTableTypes() {
            return ALL_TYPES_2_5;
        }
    }
}
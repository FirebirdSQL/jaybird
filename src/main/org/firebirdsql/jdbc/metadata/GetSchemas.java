// SPDX-FileCopyrightText: Copyright 2001-2025 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getSchemas(String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract class GetSchemas extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(2)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLESCHEMAS").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CATALOG", "TABLESCHEMAS").addField()
            .toRowDescriptor();

    private GetSchemas(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        if (!isNullOrEmpty(catalog) || "".equals(schemaPattern)) {
            // matching schema name not possible
            return createEmpty();
        }
        MetadataQuery metadataQuery = createGetSchemasQuery(schemaPattern);
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(0).setString(rs.getString("TABLE_SCHEM"))
                .toRowValue(true);
    }

    abstract MetadataQuery createGetSchemasQuery(String schemaPattern);

    @SuppressWarnings("unused")
    public static GetSchemas create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(6)) {
            return FB6.createInstance(mediator);
        } else {
            return FB5.createInstance(mediator);
        }
    }

    /**
     * Implementation for Firebird 5.0 and older.
     */
    private static final class FB5 extends GetSchemas {

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetSchemas createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
            return createEmpty();
        }

        @Override
        MetadataQuery createGetSchemasQuery(String schemaPattern) {
            throw new UnsupportedOperationException("This method should not get called for Firebird 5.0 and older");
        }

    }

    /**
     * Implementation for Firebird 6.0 and higher.
     */
    private static final class FB6 extends GetSchemas {

        private static final String GET_SCHEMAS_FRAGMENT_6 = """
                select RDB$SCHEMA_NAME as TABLE_SCHEM
                from "SYSTEM".RDB$SCHEMAS
                """;

        private static final String GET_SCHEMAS_ORDER_BY_6 = "\norder by RDB$SCHEMA_NAME";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetSchemas createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetSchemasQuery(String schemaPattern) {
            var schemaClause = new Clause("RDB$SCHEMA_NAME", schemaPattern);
            String sql = GET_SCHEMAS_FRAGMENT_6
                    + (schemaClause.hasCondition() ? "\nwhere " + schemaClause.getCondition(false) : "")
                    + GET_SCHEMAS_ORDER_BY_6;
            return new MetadataQuery(sql, Clause.parameters(schemaClause));
        }

    }

}

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
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.DatabaseMetaData.procedureNoResult;
import static java.sql.DatabaseMetaData.procedureReturnsResult;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.NameHelper.toSpecificName;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getProcedures(String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public abstract class GetProcedures extends AbstractMetadataMethod {

    private static final String PROCEDURES = "PROCEDURES";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(9)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PROCEDURE_CAT", PROCEDURES).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PROCEDURE_SCHEM", "ROCEDURES").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_NAME", PROCEDURES).addField()
            .at(3).simple(SQL_VARYING, 31, "FUTURE1", PROCEDURES).addField()
            .at(4).simple(SQL_VARYING, 31, "FUTURE2", PROCEDURES).addField()
            .at(5).simple(SQL_VARYING, 31, "FUTURE3", PROCEDURES).addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(6).simple(SQL_VARYING, Integer.MAX_VALUE, "REMARKS", PROCEDURES).addField()
            .at(7).simple(SQL_SHORT, 0, "PROCEDURE_TYPE", PROCEDURES).addField()
            // space for quoted package name, ".", quoted procedure name (assuming no double quotes in name)
            .at(8).simple(SQL_VARYING, 2 * OBJECT_NAME_LENGTH + 5, "SPECIFIC_NAME", PROCEDURES).addField()
            .toRowDescriptor();

    private GetProcedures(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see DatabaseMetaData#getProcedures(String, String, String)
     */
    public final ResultSet getProcedures(String catalog, String procedureNamePattern) throws SQLException {
        if ("".equals(procedureNamePattern)) {
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetProceduresQuery(catalog, procedureNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        String catalog = rs.getString("PROCEDURE_CAT");
        String procedureName = rs.getString("PROCEDURE_NAME");
        return valueBuilder
                .at(0).setString(catalog)
                .at(2).setString(procedureName)
                .at(6).setString(rs.getString("REMARKS"))
                .at(7).setShort(rs.getShort("PROCEDURE_TYPE") == 0 ? procedureNoResult : procedureReturnsResult)
                .at(8).setString(toSpecificName(catalog, procedureName))
                .toRowValue(true);
    }

    abstract MetadataQuery createGetProceduresQuery(String catalog, String procedureNamePattern);

    public static GetProcedures create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(3)) {
            if (mediator.isUseCatalogAsPackage()) {
                return FB3CatalogAsPackage.createInstance(mediator);
            }
            return FB3.createInstance(mediator);
        } else {
            return FB2_5.createInstance(mediator);
        }
    }

    private static final class FB2_5 extends GetProcedures {

        private static final String GET_PROCEDURES_FRAGMENT_2_5 = """
                select
                  null as PROCEDURE_CAT,
                  RDB$PROCEDURE_NAME as PROCEDURE_NAME,
                  RDB$DESCRIPTION as REMARKS,
                  RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE
                from RDB$PROCEDURES""";

        private static final String GET_PROCEDURES_ORDER_BY_2_5 = "\norder by RDB$PROCEDURE_NAME";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedures createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createGetProceduresQuery(String catalog, String procedureNamePattern) {
            Clause procedureNameClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
            String queryText = GET_PROCEDURES_FRAGMENT_2_5
                    + procedureNameClause.getCondition("\nwhere ", "")
                    + GET_PROCEDURES_ORDER_BY_2_5;
            return new MetadataQuery(queryText, Clause.parameters(procedureNameClause));
        }
    }

    private static final class FB3 extends GetProcedures {

        private static final String GET_PROCEDURES_FRAGMENT_3 = """
                select
                  null as PROCEDURE_CAT,
                  trim(trailing from RDB$PROCEDURE_NAME) as PROCEDURE_NAME,
                  RDB$DESCRIPTION as REMARKS,
                  RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE
                from RDB$PROCEDURES
                where RDB$PACKAGE_NAME is null""";

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_PROCEDURES_ORDER_BY_3 = "\norder by RDB$PACKAGE_NAME, RDB$PROCEDURE_NAME";

        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedures createInstance(DbMetadataMediator mediator) {
            return new FB3(mediator);
        }

        @Override
        MetadataQuery createGetProceduresQuery(String catalog, String procedureNamePattern) {
            Clause procedureNameClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
            String queryText = GET_PROCEDURES_FRAGMENT_3
                    + procedureNameClause.getCondition("\nand ", "")
                    + GET_PROCEDURES_ORDER_BY_3;
            return new MetadataQuery(queryText, Clause.parameters(procedureNameClause));
        }
    }

    private static final class FB3CatalogAsPackage extends GetProcedures {

        private static final String GET_PROCEDURES_FRAGMENT_3_W_PKG = """
                select
                  coalesce(trim(trailing from RDB$PACKAGE_NAME), '') as PROCEDURE_CAT,
                  trim(trailing from RDB$PROCEDURE_NAME) as PROCEDURE_NAME,
                  RDB$DESCRIPTION as REMARKS,
                  RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE
                from RDB$PROCEDURES""";

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_PROCEDURES_ORDER_BY_3_W_PKG =
                "\norder by RDB$PACKAGE_NAME nulls first, RDB$PROCEDURE_NAME";

        private FB3CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedures createInstance(DbMetadataMediator mediator) {
            return new FB3CatalogAsPackage(mediator);
        }

        @Override
        MetadataQuery createGetProceduresQuery(String catalog, String procedureNamePattern) {
            var clauses = new ArrayList<Clause>(2);
            if (catalog != null) {
                // To quote from the JDBC API: "" retrieves those without a catalog; null means that the catalog name
                // should not be used to narrow the search
                if (catalog.isEmpty()) {
                    clauses.add(Clause.isNullClause("RDB$PACKAGE_NAME"));
                } else {
                    // Exact matches only
                    clauses.add(Clause.equalsClause("RDB$PACKAGE_NAME", catalog));
                }
            }
            clauses.add(new Clause("RDB$PROCEDURE_NAME", procedureNamePattern));
            //@formatter:off
            String sql = GET_PROCEDURES_FRAGMENT_3_W_PKG
                    + (Clause.anyCondition(clauses)
                    ? "\nwhere " + Clause.conjunction(clauses)
                    : "")
                    + GET_PROCEDURES_ORDER_BY_3_W_PKG;
            //@formatter:on
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }
    }
}

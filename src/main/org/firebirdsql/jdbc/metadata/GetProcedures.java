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
import org.firebirdsql.gds.ng.fields.RowValueBuilder;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getProcedures(String, String, String)}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public abstract class GetProcedures {

    private static final RowDescriptor ROW_DESCRIPTOR =
            new RowDescriptorBuilder(9, DbMetadataMediator.datatypeCoder)
                    .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_CAT", "PROCEDURES").addField()
                    .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_SCHEM", "ROCEDURES").addField()
                    .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_NAME", "PROCEDURES").addField()
                    .at(3).simple(SQL_VARYING, 31, "FUTURE1", "PROCEDURES").addField()
                    .at(4).simple(SQL_VARYING, 31, "FUTURE2", "PROCEDURES").addField()
                    .at(5).simple(SQL_VARYING, 31, "FUTURE3", "PROCEDURES").addField()
                    // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
                    .at(6).simple(SQL_VARYING, Integer.MAX_VALUE, "REMARKS", "PROCEDURES").addField()
                    .at(7).simple(SQL_SHORT, 0, "PROCEDURE_TYPE", "PROCEDURES").addField()
                    .at(8).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "PROCEDURES").addField()
                    .toRowDescriptor();

    private final DbMetadataMediator mediator;

    private GetProcedures(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    @SuppressWarnings("unused")
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        if ("".equals(procedureNamePattern)) {
            return new FBResultSet(ROW_DESCRIPTOR, Collections.emptyList());
        }

        MetadataQuery metadataQuery = createGetProceduresQuery(procedureNamePattern);
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            if (!rs.next()) {
                return new FBResultSet(ROW_DESCRIPTOR, Collections.emptyList());
            }

            byte[] procedureNoResult = mediator.createShort(DatabaseMetaData.procedureNoResult);
            byte[] procedureReturnsResult = mediator.createShort(DatabaseMetaData.procedureReturnsResult);

            List<RowValue> rows = new ArrayList<>();
            RowValueBuilder valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
            do {
                byte[] procedureNameBytes = mediator.createString(rs.getString("PROCEDURE_NAME"));
                rows.add(valueBuilder
                        .at(2).set(procedureNameBytes)
                        .at(6).set(mediator.createString(rs.getString("REMARKS")))
                        .at(7).set(rs.getShort("PROCEDURE_TYPE") == 0 ? procedureNoResult : procedureReturnsResult)
                        .at(8).set(procedureNameBytes)
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(ROW_DESCRIPTOR, rows);
        }
    }

    abstract MetadataQuery createGetProceduresQuery(String procedureNamePattern);

    public static GetProcedures create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        if (firebirdSupportInfo.isVersionEqualOrAbove(3, 0)) {
            return new FB3(mediator);
        } else {
            return new FB2_5(mediator);
        }
    }

    private static final class FB2_5 extends GetProcedures {

        //@formatter:off
        private static final String GET_PROCEDURES_FRAGMENT_2_5 =
                "select\n"
                + "  RDB$PROCEDURE_NAME as PROCEDURE_NAME,\n"
                + "  RDB$DESCRIPTION as REMARKS,\n"
                + "  RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE\n"
                + "from RDB$PROCEDURES\n";
        //@formatter:on
        private static final String GET_PROCEDURES_ORDER_BY_2_5 =
                "order by RDB$PROCEDURE_NAME";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        @Override
        MetadataQuery createGetProceduresQuery(String procedureNamePattern) {
            Clause procedureNameClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
            String queryText = GET_PROCEDURES_FRAGMENT_2_5
                    + procedureNameClause.getCondition("where ", "\n")
                    + GET_PROCEDURES_ORDER_BY_2_5;
            return new MetadataQuery(queryText, Clause.parameters(procedureNameClause));
        }
    }

    private static final class FB3 extends GetProcedures {

        //@formatter:off
        private static final String GET_PROCEDURES_FRAGMENT_3 =
                "select\n"
                + "  trim(trailing from RDB$PROCEDURE_NAME) as PROCEDURE_NAME,\n"
                + "  RDB$DESCRIPTION as REMARKS,\n"
                + "  RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE\n"
                + "from RDB$PROCEDURES\n"
                + "where RDB$PACKAGE_NAME is null\n";
        //@formatter:on

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_PROCEDURES_ORDER_BY_3 =
                "order by RDB$PACKAGE_NAME, RDB$PROCEDURE_NAME";

        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        @Override
        MetadataQuery createGetProceduresQuery(String procedureNamePattern) {
            Clause procedureNameClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
            String queryText = GET_PROCEDURES_FRAGMENT_3
                    + procedureNameClause.getCondition("and ", "\n")
                    + GET_PROCEDURES_ORDER_BY_3;
            return new MetadataQuery(queryText, Clause.parameters(procedureNameClause));
        }
    }
}

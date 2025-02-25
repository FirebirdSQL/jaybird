// SPDX-FileCopyrightText: Copyright 2001-2023 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link DatabaseMetaData#getCatalogs()}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public sealed class GetCatalogs extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(1)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "TABLECATALOGS").addField()
            .toRowDescriptor();

    private GetCatalogs(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public ResultSet getCatalogs() throws SQLException {
        return createEmpty();
    }

    public static GetCatalogs create(DbMetadataMediator mediator) {
        if (mediator.isUseCatalogAsPackage()) {
            return CatalogAsPackage.createInstance(mediator);
        } else {
            return new GetCatalogs(mediator);
        }
    }

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        throw new AssertionError("should not get called");
    }

    private static final class CatalogAsPackage extends GetCatalogs {

        private CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetCatalogs createInstance(DbMetadataMediator mediator) {
            return new CatalogAsPackage(mediator);
        }

        @Override
        public ResultSet getCatalogs() throws SQLException {
            var metadataQuery = new MetadataQuery("""
                    select trim(trailing from RDB$PACKAGE_NAME) as PACKAGE_NAME
                    from RDB$PACKAGES
                    order by RDB$PACKAGE_NAME""", List.of());
            return createMetaDataResultSet(metadataQuery);
        }

        @Override
        RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
            return valueBuilder
                    .at(0).setString(rs.getString("PACKAGE_NAME"))
                    .toRowValue(false);
        }
    }
}

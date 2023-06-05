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
import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;

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

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(1, DbMetadataMediator.datatypeCoder)
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

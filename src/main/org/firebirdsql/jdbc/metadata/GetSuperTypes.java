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
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getSuperTypes(String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetSuperTypes {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(6, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", "SUPERTYPES").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "SUPERTYPES").addField()
            .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", "SUPERTYPES").addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTYPE_CAT", "SUPERTYPES").addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTYPE_SCHEM", "SUPERTYPES").addField()
            .at(5).simple(SQL_VARYING, 31, "SUPERTYPE_NAME", "SUPERTYPES").addField()
            .toRowDescriptor();

    private GetSuperTypes() {
    }

    public ResultSet getSuperTypes() throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }

    @SuppressWarnings("unused")
    public static GetSuperTypes create(DbMetadataMediator mediator) {
        return new GetSuperTypes();
    }
}

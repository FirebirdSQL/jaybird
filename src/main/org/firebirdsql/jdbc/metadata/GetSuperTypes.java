// SPDX-FileCopyrightText: Copyright 2001-2024 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.DbMetadataMediator;
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

    private static final String SUPERTYPES = "SUPERTYPES";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(6)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", SUPERTYPES).addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", SUPERTYPES).addField()
            .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", SUPERTYPES).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTYPE_CAT", SUPERTYPES).addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTYPE_SCHEM", SUPERTYPES).addField()
            .at(5).simple(SQL_VARYING, 31, "SUPERTYPE_NAME", SUPERTYPES).addField()
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

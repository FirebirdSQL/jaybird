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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;

/**
 * Provides the implementation of {@link DatabaseMetaData#getClientInfoProperties()}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class GetClientInfoProperties {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(4, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, 31, "NAME", "CLIENTINFO").addField()
            .at(1).simple(SQL_LONG, 4, "MAX_LEN", "CLIENTINFO").addField()
            .at(2).simple(SQL_VARYING, 31, "DEFAULT", "CLIENTINFO").addField()
            .at(3).simple(SQL_VARYING, 31, "DESCRIPTION", "CLIENTINFO").addField()
            .toRowDescriptor();

    private GetClientInfoProperties() {
    }

    public ResultSet getClientInfoProperties() throws SQLException {
        // TODO Return context info?
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }

    @SuppressWarnings("unused")
    public static GetClientInfoProperties create(DbMetadataMediator mediator) {
        return new GetClientInfoProperties();
    }
}

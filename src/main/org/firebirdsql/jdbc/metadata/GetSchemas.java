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

import org.firebirdsql.gds.impl.oo.OOGDSFactoryPlugin;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getSchemas(String, String)}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class GetSchemas {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(2, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLESCHEMAS").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CATALOG", "TABLESCHEMAS").addField()
            .toRowDescriptor();

    private GetSchemas() {
    }

    public ResultSet getSchemas(String schemaPattern) throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }

    @SuppressWarnings("unused")
    public static GetSchemas create(DbMetadataMediator mediator) {
        if (OOGDSFactoryPlugin.TYPE_NAME.equals(mediator.getGDSType().toString())) {
            return new OO();
        }
        return new GetSchemas();
    }
    
    private static final class OO extends GetSchemas {

        private static final String DEFAULT_SCHEMA = "DEFAULT";

        @Override
        public ResultSet getSchemas(String schemaPattern) throws SQLException {
            MetadataPatternMatcher matcher = MetadataPattern.compile(schemaPattern).toMetadataPatternMatcher();
            List<RowValue> rows = matcher.matches(DEFAULT_SCHEMA)
                    ? singletonList(RowValue.of(
                            ROW_DESCRIPTOR, DbMetadataMediator.datatypeCoder.encodeString(DEFAULT_SCHEMA), null))
                    : emptyList();
            return new FBResultSet(ROW_DESCRIPTOR, rows);
        }
    }
}

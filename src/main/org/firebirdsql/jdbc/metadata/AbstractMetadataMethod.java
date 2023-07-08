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
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Common implementation for metadata method implementations.
 *
 * @author Mark Rotteveel
 * @since 5
 */
abstract class AbstractMetadataMethod {

    private final RowDescriptor rowDescriptor;
    protected final DbMetadataMediator mediator;

    AbstractMetadataMethod(RowDescriptor rowDescriptor, DbMetadataMediator mediator) {
        this.rowDescriptor = rowDescriptor;
        this.mediator = mediator;
    }

    /**
     * Create a metadata result set for the (primary) row descriptor.
     *
     * @param metadataQuery
     *         Metadata query to execute
     * @return Result set
     * @throws SQLException
     *         For errors executing the query or retrieving data from the result set
     */
    final ResultSet createMetaDataResultSet(MetadataQuery metadataQuery) throws SQLException {
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            if (!rs.next()) {
                return createEmpty();
            }
            List<RowValue> rowValues = new ArrayList<>();
            RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rowValues.add(
                        createMetadataRow(rs, valueBuilder));
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rowValues);
        }
    }

    abstract RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException;

    /**
     * Creates an empty result set with the (primary) row descriptor.
     *
     * @return empty result set
     */
    final ResultSet createEmpty() throws SQLException {
        return new FBResultSet(rowDescriptor, emptyList());
    }

    /**
     * Extracts the default value as stored in {@code RDB$DEFAULT_SOURCE}.
     *
     * @param defaultDefinition default definition string
     * @return default value ({@code null} if there is no default, a default of {@code NULL} is represented with
     * the string {@code "NULL"})
     */
    static String extractDefault(String defaultDefinition) {
        if (defaultDefinition == null || defaultDefinition.isEmpty()) {
            return null;
        }
        if (defaultDefinition.length() > 7) {
            String prefix = defaultDefinition.substring(0, 7);
            if (prefix.equalsIgnoreCase("DEFAULT")) {
                String defaultValue = defaultDefinition.substring(7).trim();
                // make sure `default null` returns string "NULL", as that is required by the JDBC spec
                return defaultValue.equalsIgnoreCase("NULL") ? "NULL" : defaultValue;
            }
        }
        return defaultDefinition.trim();
    }
}

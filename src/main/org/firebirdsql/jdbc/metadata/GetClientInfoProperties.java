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
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;

/**
 * Provides the implementation of {@link DatabaseMetaData#getClientInfoProperties()}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetClientInfoProperties {

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(4)
            .at(0).simple(SQL_VARYING, 80, "NAME", "CLIENTINFO").addField()
            .at(1).simple(SQL_LONG, 0, "MAX_LEN", "CLIENTINFO").addField()
            .at(2).simple(SQL_VARYING | 1, 31, "DEFAULT_VALUE", "CLIENTINFO").addField()
            .at(3).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "DESCRIPTION", "CLIENTINFO").addField()
            .toRowDescriptor();

    private final DbMetadataMediator mediator;

    private GetClientInfoProperties(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    public ResultSet getClientInfoProperties() throws SQLException {
        List<RowValue> rows;
        if (mediator.getFirebirdSupportInfo().supportsGetSetContext()) {
            var valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
            rows = mediator.getClientInfoPropertyNames().stream().sorted()
                    .map(name -> {
                        valueBuilder
                                .at(0).setString(name)
                                .at(1).setInt(32765)
                                .at(2).set(null)
                                .at(3).setString(getDescription(name));
                        return valueBuilder.toRowValue(false);
                    }).toList();
        } else {
            rows = emptyList();
        }
        return new FBResultSet(ROW_DESCRIPTOR, rows);
    }

    private static String getDescription(String name) {
        return switch(name) {
        case "ApplicationName" ->
            "Application name; ApplicationName in context USER_SESSION; if that property is not set, then the value of "
            + "CLIENT_PROCESS in context SYSTEM is returned";
        case "ClientUser" ->
                "The name of the user that the application using the connection is performing work for. This may not "
                + "be the same as the user name that was used in establishing the connection; ClientUser in context "
                + "USER_SESSION (no default or fallback value)";
        case "ClientHostname" ->
            "The hostname of the computer the application using the connection is running on; ClientHostname in "
            + "context USER_SESSION (no default or fallback value)";
        default -> null;
        };
    }

    public static GetClientInfoProperties create(DbMetadataMediator mediator) {
        return new GetClientInfoProperties(mediator);
    }
}

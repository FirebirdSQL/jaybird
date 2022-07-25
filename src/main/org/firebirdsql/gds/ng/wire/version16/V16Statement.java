/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.version13.V13Statement;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public class V16Statement extends V13Statement {

    /**
     * Creates a new instance of V16Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V16Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    protected void sendExecute(int operation, RowValue parameters) throws IOException, SQLException {
        super.sendExecute(operation, parameters);
        // timeout is an unsigned 32 bit int
        getXdrOut().writeInt((int) getAllowedTimeout()); // p_sqldata_timeout
    }
}

/*
 *
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;


public class FBResultSet extends AbstractResultSet {

    public FBResultSet(GDSHelper gdsHelper, AbstractStatement fbStatement,
            AbstractIscStmtHandle stmt, ResultSetListener listener,
            boolean metaDataQuery, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        
        super(gdsHelper, fbStatement, stmt, listener, metaDataQuery, rsType,
                rsConcurrency, rsHoldability, cached);
    }

    public FBResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
        super(xsqlvars, rows);
    }

}
